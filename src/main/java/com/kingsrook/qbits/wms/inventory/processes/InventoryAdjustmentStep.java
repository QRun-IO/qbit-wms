/*******************************************************************************
 ** Backend step for InventoryAdjustment.  Creates an ADJUST inventory
 ** transaction and then updates the wms_inventory quantity_on_hand, following
 ** the perpetual inventory principle.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.AdjustmentReasonCode;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class InventoryAdjustmentStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(InventoryAdjustmentStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer locationId = input.getValueInteger("locationId");
      Integer itemId = input.getValueInteger("itemId");
      BigDecimal newQuantity = input.getValueBigDecimal("newQuantity");
      Integer reasonCodeId = input.getValueInteger("reasonCodeId");
      String notes = input.getValueString("adjustmentNotes");

      if(locationId == null || itemId == null)
      {
         throw new QUserFacingException("Location and item are required.");
      }
      if(newQuantity == null)
      {
         throw new QUserFacingException("New quantity is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Look up existing inventory at this item + location                  //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId))));

      List<QRecord> invRecords = invQuery.getRecords();
      BigDecimal currentQuantity = BigDecimal.ZERO;
      QRecord existingInv = null;
      Integer warehouseId = null;
      Integer clientId = null;

      if(!invRecords.isEmpty())
      {
         existingInv = invRecords.get(0);
         currentQuantity = existingInv.getValueBigDecimal("quantityOnHand");
         if(currentQuantity == null)
         {
            currentQuantity = BigDecimal.ZERO;
         }
         warehouseId = existingInv.getValueInteger("warehouseId");
         clientId = existingInv.getValueInteger("clientId");
      }

      BigDecimal delta = newQuantity.subtract(currentQuantity);

      /////////////////////////////////////////////////////////////////////////
      // Resolve performer and reason code                                   //
      /////////////////////////////////////////////////////////////////////////
      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      AdjustmentReasonCode reasonCode = AdjustmentReasonCode.getById(reasonCodeId);
      String reasonCodeLabel = reasonCode != null ? reasonCode.getLabel() : null;

      LOG.info("Executing inventory adjustment",
         logPair("itemId", itemId),
         logPair("locationId", locationId),
         logPair("currentQuantity", currentQuantity),
         logPair("newQuantity", newQuantity),
         logPair("delta", delta),
         logPair("reasonCode", reasonCodeLabel));

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create inventory transaction (MUST come first)              //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("transactionTypeId", TransactionType.ADJUST.getPossibleValueId())
         .withValue("fromLocationId", locationId)
         .withValue("toLocationId", locationId)
         .withValue("quantity", delta)
         .withValue("reasonCode", reasonCodeLabel)
         .withValue("performedBy", performedBy)
         .withValue("performedDate", Instant.now())
         .withValue("notes", notes)));

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Update inventory record                                     //
      /////////////////////////////////////////////////////////////////////////
      if(existingInv != null)
      {
         BigDecimal allocated = existingInv.getValueBigDecimal("quantityAllocated");
         BigDecimal onHold = existingInv.getValueBigDecimal("quantityOnHold");
         if(allocated == null) allocated = BigDecimal.ZERO;
         if(onHold == null) onHold = BigDecimal.ZERO;

         BigDecimal newAvailable = newQuantity.subtract(allocated).subtract(onHold);

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", existingInv.getValueInteger("id"))
            .withValue("quantityOnHand", newQuantity)
            .withValue("quantityAvailable", newAvailable)));
      }
      else
      {
         //////////////////////////////////////////////////////////////////
         // No existing inventory record -- create one if adding stock   //
         //////////////////////////////////////////////////////////////////
         if(newQuantity.compareTo(BigDecimal.ZERO) > 0)
         {
            new InsertAction().execute(new InsertInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
               .withValue("warehouseId", warehouseId)
               .withValue("clientId", clientId)
               .withValue("itemId", itemId)
               .withValue("locationId", locationId)
               .withValue("quantityOnHand", newQuantity)
               .withValue("quantityAllocated", BigDecimal.ZERO)
               .withValue("quantityAvailable", newQuantity)
               .withValue("quantityOnHold", BigDecimal.ZERO)
               .withValue("inventoryStatusId", 1))); // AVAILABLE
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Populate output fields                                              //
      /////////////////////////////////////////////////////////////////////////
      output.addValue("resultMessage", "Inventory adjusted successfully.");
      output.addValue("adjustedItem", String.valueOf(itemId));
      output.addValue("adjustedLocation", String.valueOf(locationId));
      output.addValue("previousQuantity", currentQuantity);
      output.addValue("adjustedQuantity", newQuantity);
      output.addValue("delta", delta);
   }
}
