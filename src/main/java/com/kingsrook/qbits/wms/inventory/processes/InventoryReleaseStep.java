/*******************************************************************************
 ** Backend step for InventoryRelease.  Updates the wms_inventory_hold record
 ** to RELEASED status, creates a RELEASE inventory transaction, and restores
 ** matching wms_inventory records to AVAILABLE status.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
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


public class InventoryReleaseStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(InventoryReleaseStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer holdId = input.getValueInteger("holdId");

      if(holdId == null)
      {
         throw new QUserFacingException("Hold ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load the hold record                                                //
      /////////////////////////////////////////////////////////////////////////
      GetOutput holdOutput = new GetAction().execute(new GetInput(WmsInventoryHold.TABLE_NAME).withPrimaryKey(holdId));
      QRecord hold = holdOutput.getRecord();
      if(hold == null)
      {
         throw new QUserFacingException("Hold not found: " + holdId);
      }

      String holdStatus = hold.getValueString("status");
      if(!"ACTIVE".equals(holdStatus))
      {
         throw new QUserFacingException("Only ACTIVE holds can be released. Current status: " + holdStatus);
      }

      Integer itemId = hold.getValueInteger("itemId");
      String lotNumber = hold.getValueString("lotNumber");
      Integer locationId = hold.getValueInteger("locationId");
      Integer warehouseId = hold.getValueInteger("warehouseId");
      Integer clientId = hold.getValueInteger("clientId");

      String releasedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         releasedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Update the hold record to RELEASED                                  //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", holdId)
         .withValue("status", "RELEASED")
         .withValue("releasedBy", releasedBy)
         .withValue("releasedDate", Instant.now())));

      /////////////////////////////////////////////////////////////////////////
      // Find matching ON_HOLD inventory records                             //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
         .withCriteria(new QFilterCriteria("inventoryStatusId", QCriteriaOperator.EQUALS, InventoryStatus.ON_HOLD.getPossibleValueId()));

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

      /////////////////////////////////////////////////////////////////////////
      // Release each matching inventory record                              //
      /////////////////////////////////////////////////////////////////////////
      Integer affectedCount = 0;
      for(QRecord inv : invRecords)
      {
         BigDecimal onHold = inv.getValueBigDecimal("quantityOnHold");
         if(onHold == null || onHold.compareTo(BigDecimal.ZERO) <= 0)
         {
            continue;
         }

         //////////////////////////////////////////////////////////////////
         // Step 1: Create RELEASE transaction (perpetual inventory)     //
         //////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", inv.getValueInteger("warehouseId"))
            .withValue("clientId", inv.getValueInteger("clientId"))
            .withValue("itemId", itemId)
            .withValue("transactionTypeId", TransactionType.RELEASE.getPossibleValueId())
            .withValue("fromLocationId", inv.getValueInteger("locationId"))
            .withValue("quantity", onHold)
            .withValue("lotNumber", inv.getValueString("lotNumber"))
            .withValue("reasonCode", "Hold released")
            .withValue("performedBy", releasedBy)
            .withValue("performedDate", Instant.now())
            .withValue("notes", "Hold " + holdId + " released")));

         //////////////////////////////////////////////////////////////////
         // Step 2: Restore inventory to AVAILABLE                       //
         //////////////////////////////////////////////////////////////////
         BigDecimal qoh = inv.getValueBigDecimal("quantityOnHand");
         BigDecimal allocated = inv.getValueBigDecimal("quantityAllocated");
         if(qoh == null) qoh = BigDecimal.ZERO;
         if(allocated == null) allocated = BigDecimal.ZERO;

         BigDecimal newAvailable = qoh.subtract(allocated);

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", inv.getValueInteger("id"))
            .withValue("inventoryStatusId", InventoryStatus.AVAILABLE.getPossibleValueId())
            .withValue("quantityOnHold", BigDecimal.ZERO)
            .withValue("quantityAvailable", newAvailable)
            .withValue("holdReason", null)));

         affectedCount++;
      }

      LOG.info("Inventory hold released",
         logPair("holdId", holdId),
         logPair("itemId", itemId),
         logPair("affectedRecords", affectedCount));

      output.addValue("resultMessage", "Hold released successfully.");
      output.addValue("affectedRecords", affectedCount);
   }
}
