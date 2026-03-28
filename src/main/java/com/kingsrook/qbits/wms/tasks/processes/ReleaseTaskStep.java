/*******************************************************************************
 ** Backend step for ReleaseTask.  Sets an ON_HOLD task back to PENDING and
 ** clears the assignment so it can re-enter the queue.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReleaseTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReleaseTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");

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
      if(currentStatus != TaskStatus.ON_HOLD)
      {
         throw new QUserFacingException("Only ON_HOLD tasks can be released. Current status: "
            + (currentStatus != null ? currentStatus.getLabel() : "Unknown"));
      }

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
         .withValue("assignedTo", null)
         .withValue("assignedDate", null)));

      LOG.info("Task released from hold", logPair("taskId", taskId));
      output.addValue("resultMessage", "Task " + taskId + " has been released from hold and is back in the queue.");
   }
}
