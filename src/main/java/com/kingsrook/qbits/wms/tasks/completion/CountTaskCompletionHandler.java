/*******************************************************************************
 ** Completion handler for COUNT tasks.  Computes the variance between the
 ** expected and counted quantities, updates the cycle count line, and
 ** optionally triggers a recount if the variance exceeds a threshold.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
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
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CountTaskCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(CountTaskCompletionHandler.class);

   /////////////////////////////////////////////////////////////////////////////
   // Variance percentage threshold that triggers an automatic recount.       //
   // Applies only when is_blind_count is true.                               //
   /////////////////////////////////////////////////////////////////////////////
   private static final BigDecimal RECOUNT_VARIANCE_THRESHOLD = new BigDecimal("10");

   /////////////////////////////////////////////////////////////////////////////
   // Priority boost applied to automatically generated recount tasks.        //
   /////////////////////////////////////////////////////////////////////////////
   private static final Integer RECOUNT_PRIORITY_BOOST = 2;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void handle(QRecord task) throws QException
   {
      Integer taskId = task.getValueInteger("id");
      BigDecimal expectedQuantity = task.getValueBigDecimal("expectedQuantity");
      BigDecimal countedQuantity = task.getValueBigDecimal("countedQuantity");
      Integer cycleCountId = task.getValueInteger("cycleCountId");

      LOG.info("Handling COUNT task completion",
         logPair("taskId", taskId),
         logPair("expectedQuantity", expectedQuantity),
         logPair("countedQuantity", countedQuantity));

      if(expectedQuantity == null)
      {
         expectedQuantity = BigDecimal.ZERO;
      }
      if(countedQuantity == null)
      {
         countedQuantity = BigDecimal.ZERO;
      }

      /////////////////////////////////////////////////////////////////////////
      // Compute variance and update the task record                         //
      /////////////////////////////////////////////////////////////////////////
      BigDecimal variance = countedQuantity.subtract(expectedQuantity);

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("variance", variance)));

      /////////////////////////////////////////////////////////////////////////
      // Update the corresponding cycle count line                           //
      /////////////////////////////////////////////////////////////////////////
      if(cycleCountId != null)
      {
         updateCycleCountLine(task, countedQuantity, variance);
      }

      /////////////////////////////////////////////////////////////////////////
      // Check if an automatic recount is needed                             //
      /////////////////////////////////////////////////////////////////////////
      Boolean isBlindCount = task.getValueBoolean("isBlindCount");
      if(Boolean.TRUE.equals(isBlindCount) && variance.abs().compareTo(BigDecimal.ZERO) > 0)
      {
         checkRecountThreshold(task, expectedQuantity, variance);
      }

      /////////////////////////////////////////////////////////////////////////
      // Check if the entire cycle count can be marked complete              //
      /////////////////////////////////////////////////////////////////////////
      if(cycleCountId != null)
      {
         checkCycleCountCompletion(cycleCountId);
      }
   }



   /*******************************************************************************
    ** Update the cycle count line with counted quantity and variance.
    *******************************************************************************/
   private void updateCycleCountLine(QRecord task, BigDecimal countedQuantity, BigDecimal variance) throws QException
   {
      Integer taskId = task.getValueInteger("id");

      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskId", QCriteriaOperator.EQUALS, taskId))));

      List<QRecord> lines = lineQuery.getRecords();
      if(lines.isEmpty())
      {
         LOG.warn("No cycle count line found for task", logPair("taskId", taskId));
         return;
      }

      QRecord line = lines.get(0);
      String newLineStatus;

      if(variance.compareTo(BigDecimal.ZERO) == 0)
      {
         /////////////////////////////////////////////////////////////////////
         // Zero variance -- auto-approve                                   //
         /////////////////////////////////////////////////////////////////////
         newLineStatus = "APPROVED";
      }
      else
      {
         /////////////////////////////////////////////////////////////////////
         // Non-zero variance -- awaits supervisor review                   //
         /////////////////////////////////////////////////////////////////////
         newLineStatus = "COUNTED";
      }

      new UpdateAction().execute(new UpdateInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", line.getValueInteger("id"))
         .withValue("countedQuantity", countedQuantity)
         .withValue("variance", variance)
         .withValue("status", newLineStatus)));
   }



   /*******************************************************************************
    ** If the variance exceeds the threshold on a blind count, auto-create a
    ** recount task at higher priority.
    *******************************************************************************/
   private void checkRecountThreshold(QRecord task, BigDecimal expectedQuantity, BigDecimal variance) throws QException
   {
      if(expectedQuantity.compareTo(BigDecimal.ZERO) == 0)
      {
         return;
      }

      BigDecimal variancePercent = variance.abs()
         .multiply(new BigDecimal("100"))
         .divide(expectedQuantity.abs(), 2, java.math.RoundingMode.HALF_UP);

      if(variancePercent.compareTo(RECOUNT_VARIANCE_THRESHOLD) > 0)
      {
         LOG.info("Variance exceeds threshold, creating recount task",
            logPair("taskId", task.getValueInteger("id")),
            logPair("variancePercent", variancePercent));

         Integer currentPriority = task.getValueInteger("priority");
         Integer recountPriority = (currentPriority != null ? currentPriority : 5) - RECOUNT_PRIORITY_BOOST;
         if(recountPriority < 1)
         {
            recountPriority = 1;
         }

         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", task.getValueInteger("warehouseId"))
            .withValue("clientId", task.getValueInteger("clientId"))
            .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", recountPriority)
            .withValue("itemId", task.getValueInteger("itemId"))
            .withValue("sourceLocationId", task.getValueInteger("sourceLocationId"))
            .withValue("zoneId", task.getValueInteger("zoneId"))
            .withValue("cycleCountId", task.getValueInteger("cycleCountId"))
            .withValue("expectedQuantity", task.getValueBigDecimal("expectedQuantity"))
            .withValue("isBlindCount", false)
            .withValue("recountRequired", true)
            .withValue("notes", "Auto-generated recount due to variance of " + variancePercent + "%")));
      }
   }



   /*******************************************************************************
    ** Check if all lines for a cycle count have reached a terminal status.
    ** If so, mark the cycle count as COMPLETED or PENDING_REVIEW.
    *******************************************************************************/
   private void checkCycleCountCompletion(Integer cycleCountId) throws QException
   {
      QueryOutput linesOutput = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("cycleCountId", QCriteriaOperator.EQUALS, cycleCountId))));

      List<QRecord> allLines = linesOutput.getRecords();
      if(allLines.isEmpty())
      {
         return;
      }

      boolean allTerminal = true;
      boolean anyNeedsReview = false;

      for(QRecord line : allLines)
      {
         String status = line.getValueString("status");
         if("APPROVED".equals(status) || "ADJUSTED".equals(status))
         {
            // terminal
         }
         else if("COUNTED".equals(status) || "RECOUNTED".equals(status))
         {
            anyNeedsReview = true;
            allTerminal = false;
         }
         else
         {
            // PENDING or other -- not terminal
            allTerminal = false;
         }
      }

      if(allTerminal)
      {
         new UpdateAction().execute(new UpdateInput(WmsCycleCount.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", cycleCountId)
            .withValue("cycleCountStatusId", CycleCountStatus.COMPLETED.getPossibleValueId())
            .withValue("completedDate", Instant.now())));
      }
      else if(anyNeedsReview)
      {
         new UpdateAction().execute(new UpdateInput(WmsCycleCount.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", cycleCountId)
            .withValue("cycleCountStatusId", CycleCountStatus.PENDING_REVIEW.getPossibleValueId())));
      }
   }
}
