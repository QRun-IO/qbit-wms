/*******************************************************************************
 ** Backend step for CancelTask.  Sets the task status to CANCELLED and
 ** performs type-specific rollback.  For PICK tasks, reverses quantity
 ** allocation.  For COUNT tasks, marks the cycle_count_line as cancelled.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CancelTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CancelTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");
      String cancellationReason = input.getValueString("cancellationReason");

      if(taskId == null)
      {
         throw new QUserFacingException("Task ID is required.");
      }

      GetOutput getOutput = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      QRecord task = getOutput.getRecord();
      if(task == null)
      {
         throw new QUserFacingException("Task not found: " + taskId);
      }

      Integer currentStatusId = task.getValueInteger("taskStatusId");
      TaskStatus currentStatus = TaskStatus.getById(currentStatusId);
      if(currentStatus == TaskStatus.COMPLETED || currentStatus == TaskStatus.CANCELLED)
      {
         throw new QUserFacingException("Task cannot be cancelled in its current status: "
            + (currentStatus != null ? currentStatus.getLabel() : "Unknown"));
      }

      /////////////////////////////////////////////////////////////////////////
      // Type-specific rollback                                              //
      /////////////////////////////////////////////////////////////////////////
      Integer taskTypeId = task.getValueInteger("taskTypeId");
      TaskType taskType = TaskType.getById(taskTypeId);

      if(taskType == TaskType.PICK)
      {
         handlePickCancellation(task);
      }
      else if(taskType == TaskType.COUNT)
      {
         handleCountCancellation(task);
      }

      /////////////////////////////////////////////////////////////////////////
      // Update the task status to CANCELLED                                 //
      /////////////////////////////////////////////////////////////////////////
      String existingNotes = task.getValueString("notes");
      String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "")
         + "CANCELLED: " + (cancellationReason != null ? cancellationReason : "No reason given");

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("taskStatusId", TaskStatus.CANCELLED.getPossibleValueId())
         .withValue("shortReason", cancellationReason)
         .withValue("notes", updatedNotes)));

      LOG.info("Task cancelled",
         logPair("taskId", taskId),
         logPair("taskType", taskType != null ? taskType.getLabel() : "Unknown"),
         logPair("reason", cancellationReason));

      output.addValue("resultMessage", "Task " + taskId + " has been cancelled.");
   }



   /*******************************************************************************
    ** Reverse allocation for a cancelled PICK task.
    *******************************************************************************/
   private void handlePickCancellation(QRecord task) throws QException
   {
      Integer itemId = task.getValueInteger("itemId");
      Integer sourceLocationId = task.getValueInteger("sourceLocationId");
      BigDecimal quantityRequested = task.getValueBigDecimal("quantityRequested");

      if(itemId != null && sourceLocationId != null && quantityRequested != null)
      {
         //////////////////////////////////////////////////////////////////
         // Find the inventory record and reverse the allocation         //
         //////////////////////////////////////////////////////////////////
         QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
               .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, sourceLocationId))));

         for(QRecord inv : invQuery.getRecords())
         {
            BigDecimal allocated = inv.getValueBigDecimal("quantityAllocated");
            if(allocated != null && allocated.compareTo(BigDecimal.ZERO) > 0)
            {
               BigDecimal newAllocated = allocated.subtract(quantityRequested);
               if(newAllocated.compareTo(BigDecimal.ZERO) < 0)
               {
                  newAllocated = BigDecimal.ZERO;
               }

               BigDecimal qoh = inv.getValueBigDecimal("quantityOnHand");
               BigDecimal onHold = inv.getValueBigDecimal("quantityOnHold");
               if(qoh == null) qoh = BigDecimal.ZERO;
               if(onHold == null) onHold = BigDecimal.ZERO;

               new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", inv.getValueInteger("id"))
                  .withValue("quantityAllocated", newAllocated)
                  .withValue("quantityAvailable", qoh.subtract(newAllocated).subtract(onHold))));

               LOG.info("Reversed allocation for cancelled PICK",
                  logPair("taskId", task.getValueInteger("id")),
                  logPair("inventoryId", inv.getValueInteger("id")),
                  logPair("deallocated", quantityRequested));
               break;
            }
         }
      }
   }



   /*******************************************************************************
    ** Mark cycle count line as cancelled for a cancelled COUNT task.
    *******************************************************************************/
   private void handleCountCancellation(QRecord task) throws QException
   {
      Integer taskId = task.getValueInteger("id");

      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskId", QCriteriaOperator.EQUALS, taskId))));

      for(QRecord line : lineQuery.getRecords())
      {
         new UpdateAction().execute(new UpdateInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", line.getValueInteger("id"))
            .withValue("status", "PENDING")));
      }
   }
}
