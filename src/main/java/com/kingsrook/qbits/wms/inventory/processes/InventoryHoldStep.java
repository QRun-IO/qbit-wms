/*******************************************************************************
 ** Backend step for InventoryHold.  Creates a wms_inventory_hold record,
 ** inserts a HOLD inventory transaction, and updates matching wms_inventory
 ** records to ON_HOLD status, moving quantity_on_hand to quantity_on_hold.
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
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class InventoryHoldStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(InventoryHoldStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer itemId = input.getValueInteger("itemId");
      String lotNumber = input.getValueString("lotNumber");
      Integer locationId = input.getValueInteger("locationId");
      Integer holdTypeId = input.getValueInteger("holdTypeId");
      String holdReason = input.getValueString("holdReason");

      if(itemId == null)
      {
         throw new QUserFacingException("Item is required.");
      }
      if(holdTypeId == null)
      {
         throw new QUserFacingException("Hold type is required.");
      }

      String placedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         placedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Find matching inventory records                                     //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId));

      if(lotNumber != null && !lotNumber.isBlank())
      {
         filter.withCriteria(new QFilterCriteria("lotNumber", QCriteriaOperator.EQUALS, lotNumber));
      }
      if(locationId != null)
      {
         filter.withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId));
      }

      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME).withFilter(filter));
      List<QRecord> invRecords = invQuery.getRecords();

      if(invRecords.isEmpty())
      {
         throw new QUserFacingException("No inventory found matching the specified criteria.");
      }

      Integer warehouseId = invRecords.get(0).getValueInteger("warehouseId");
      Integer clientId = invRecords.get(0).getValueInteger("clientId");

      /////////////////////////////////////////////////////////////////////////
      // Create the inventory hold record                                    //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("lotNumber", lotNumber)
         .withValue("locationId", locationId)
         .withValue("holdTypeId", holdTypeId)
         .withValue("reason", holdReason)
         .withValue("placedBy", placedBy)
         .withValue("placedDate", Instant.now())
         .withValue("status", "ACTIVE")));

      /////////////////////////////////////////////////////////////////////////
      // For each matching inventory record: create transaction, update hold //
      /////////////////////////////////////////////////////////////////////////
      Integer affectedCount = 0;
      for(QRecord inv : invRecords)
      {
         BigDecimal qoh = inv.getValueBigDecimal("quantityOnHand");
         if(qoh == null || qoh.compareTo(BigDecimal.ZERO) <= 0)
         {
            continue;
         }

         //////////////////////////////////////////////////////////////////
         // Step 1: Create HOLD transaction (perpetual inventory)        //
         //////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", inv.getValueInteger("warehouseId"))
            .withValue("clientId", inv.getValueInteger("clientId"))
            .withValue("itemId", itemId)
            .withValue("transactionTypeId", TransactionType.HOLD.getPossibleValueId())
            .withValue("fromLocationId", inv.getValueInteger("locationId"))
            .withValue("quantity", qoh)
            .withValue("lotNumber", inv.getValueString("lotNumber"))
            .withValue("reasonCode", holdReason)
            .withValue("performedBy", placedBy)
            .withValue("performedDate", Instant.now())
            .withValue("notes", "Inventory placed on hold")));

         //////////////////////////////////////////////////////////////////
         // Step 2: Update inventory record                              //
         //////////////////////////////////////////////////////////////////
         BigDecimal allocated = inv.getValueBigDecimal("quantityAllocated");
         if(allocated == null) allocated = BigDecimal.ZERO;

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", inv.getValueInteger("id"))
            .withValue("inventoryStatusId", InventoryStatus.ON_HOLD.getPossibleValueId())
            .withValue("quantityOnHold", qoh)
            .withValue("quantityAvailable", BigDecimal.ZERO)
            .withValue("holdReason", holdReason)));

         affectedCount++;
      }

      LOG.info("Inventory hold placed",
         logPair("itemId", itemId),
         logPair("holdType", holdTypeId),
         logPair("affectedRecords", affectedCount));

      output.addValue("resultMessage", "Inventory hold placed successfully.");
      output.addValue("affectedRecords", affectedCount);
   }
}
