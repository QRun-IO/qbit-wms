/*******************************************************************************
 ** Backend step for ApproveCountVariance.  For approved lines, creates a COUNT
 ** inventory transaction and adjusts inventory.  For rejected lines, creates a
 ** new COUNT task for recount.  When all lines reach terminal status, marks the
 ** cycle count as COMPLETED.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.tasks.completion.AbstractTaskCompletionHandler;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ApproveCountVarianceStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ApproveCountVarianceStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer cycleCountId = input.getValueInteger("cycleCountId");
      Boolean approveAll = input.getValueBoolean("approveAll");
      String rejectLineIdsStr = input.getValueString("rejectLineIds");

      if(cycleCountId == null)
      {
         throw new QUserFacingException("Cycle Count ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Parse rejected line IDs                                             //
      /////////////////////////////////////////////////////////////////////////
      Set<Integer> rejectLineIds = new HashSet<>();
      if(rejectLineIdsStr != null && !rejectLineIdsStr.isBlank())
      {
         for(String idStr : rejectLineIdsStr.split(","))
         {
            try
            {
               rejectLineIds.add(Integer.parseInt(idStr.trim()));
            }
            catch(NumberFormatException e)
            {
               // skip invalid entries
            }
         }
      }

      String approvedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         approvedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Get all cycle count lines with COUNTED status (have variance)       //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("cycleCountId", QCriteriaOperator.EQUALS, cycleCountId))
            .withCriteria(new QFilterCriteria("status", QCriteriaOperator.IN, List.of("COUNTED", "RECOUNTED")))));

      List<QRecord> lines = lineQuery.getRecords();
      Integer approvedCount = 0;
      Integer rejectedCount = 0;

      for(QRecord line : lines)
      {
         Integer lineId = line.getValueInteger("id");
         BigDecimal variance = line.getValueBigDecimal("variance");

         if(rejectLineIds.contains(lineId))
         {
            //////////////////////////////////////////////////////////////////
            // Reject: create recount task                                  //
            //////////////////////////////////////////////////////////////////
            createRecountTask(line, cycleCountId);

            new UpdateAction().execute(new UpdateInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", lineId)
               .withValue("status", "PENDING")));

            rejectedCount++;
         }
         else if(Boolean.TRUE.equals(approveAll) || !rejectLineIds.contains(lineId))
         {
            //////////////////////////////////////////////////////////////////
            // Approve: adjust inventory                                    //
            //////////////////////////////////////////////////////////////////
            if(variance != null && variance.compareTo(BigDecimal.ZERO) != 0)
            {
               adjustInventoryForVariance(line, variance, approvedBy);
            }

            new UpdateAction().execute(new UpdateInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", lineId)
               .withValue("status", "APPROVED")
               .withValue("varianceApproved", true)
               .withValue("approvedBy", approvedBy)));

            approvedCount++;
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Check if the cycle count is now complete                            //
      /////////////////////////////////////////////////////////////////////////
      checkCycleCountCompletion(cycleCountId);

      LOG.info("Count variance review complete",
         logPair("cycleCountId", cycleCountId),
         logPair("approved", approvedCount),
         logPair("rejected", rejectedCount));

      output.addValue("resultMessage", "Variance review complete.");
      output.addValue("approvedCount", approvedCount);
      output.addValue("rejectedCount", rejectedCount);
   }



   /*******************************************************************************
    ** Create a new COUNT task for a rejected line that needs recounting.
    *******************************************************************************/
   private void createRecountTask(QRecord line, Integer cycleCountId) throws QException
   {
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
         .withValue("priority", 3)
         .withValue("itemId", line.getValueInteger("itemId"))
         .withValue("sourceLocationId", line.getValueInteger("locationId"))
         .withValue("cycleCountId", cycleCountId)
         .withValue("expectedQuantity", line.getValueBigDecimal("expectedQuantity"))
         .withValue("isBlindCount", false)
         .withValue("recountRequired", true)
         .withValue("notes", "Recount requested by supervisor for line " + line.getValueInteger("id"))));
   }



   /*******************************************************************************
    ** Apply the variance as an inventory adjustment.  Creates a COUNT transaction
    ** then updates inventory quantity.
    *******************************************************************************/
   private void adjustInventoryForVariance(QRecord line, BigDecimal variance, String approvedBy) throws QException
   {
      Integer itemId = line.getValueInteger("itemId");
      Integer locationId = line.getValueInteger("locationId");

      /////////////////////////////////////////////////////////////////////////
      // Look up the inventory record to get warehouse/client                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId))));

      List<QRecord> invRecords = invQuery.getRecords();
      Integer warehouseId = null;
      Integer clientId = null;
      if(!invRecords.isEmpty())
      {
         warehouseId = invRecords.get(0).getValueInteger("warehouseId");
         clientId = invRecords.get(0).getValueInteger("clientId");
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create COUNT transaction (perpetual inventory)              //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("transactionTypeId", TransactionType.COUNT.getPossibleValueId())
         .withValue("fromLocationId", locationId)
         .withValue("toLocationId", locationId)
         .withValue("quantity", variance)
         .withValue("lotNumber", line.getValueString("lotNumber"))
         .withValue("reasonCode", "Count variance approved")
         .withValue("performedBy", approvedBy)
         .withValue("performedDate", Instant.now())
         .withValue("notes", "Cycle count variance adjustment")));

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Update inventory quantity                                   //
      /////////////////////////////////////////////////////////////////////////
      if(!invRecords.isEmpty())
      {
         QRecord inv = invRecords.get(0);
         BigDecimal currentQoh = inv.getValueBigDecimal("quantityOnHand");
         if(currentQoh == null) currentQoh = BigDecimal.ZERO;

         BigDecimal newQoh = currentQoh.add(variance);
         BigDecimal allocated = inv.getValueBigDecimal("quantityAllocated");
         BigDecimal onHold = inv.getValueBigDecimal("quantityOnHold");
         if(allocated == null) allocated = BigDecimal.ZERO;
         if(onHold == null) onHold = BigDecimal.ZERO;

         BigDecimal newAvailable = newQoh.subtract(allocated).subtract(onHold);

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", inv.getValueInteger("id"))
            .withValue("quantityOnHand", newQoh)
            .withValue("quantityAvailable", newAvailable)
            .withValue("lastCountDate", Instant.now())));
      }
   }



   /*******************************************************************************
    ** Check if all cycle count lines have reached terminal status.
    *******************************************************************************/
   private void checkCycleCountCompletion(Integer cycleCountId) throws QException
   {
      QueryOutput allLinesQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("cycleCountId", QCriteriaOperator.EQUALS, cycleCountId))));

      boolean allTerminal = true;
      for(QRecord line : allLinesQuery.getRecords())
      {
         String status = line.getValueString("status");
         if(!"APPROVED".equals(status) && !"ADJUSTED".equals(status))
         {
            allTerminal = false;
            break;
         }
      }

      if(allTerminal)
      {
         new UpdateAction().execute(new UpdateInput(WmsCycleCount.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", cycleCountId)
            .withValue("cycleCountStatusId", CycleCountStatus.COMPLETED.getPossibleValueId())
            .withValue("completedDate", Instant.now())));
      }
   }
}
