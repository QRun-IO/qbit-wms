/*******************************************************************************
 ** Backend step for ReprioritizeTask.  Updates the priority on a task.
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
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReprioritizeTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReprioritizeTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");
      Integer newPriority = input.getValueInteger("newPriority");

      if(taskId == null)
      {
         throw new QUserFacingException("Task ID is required.");
      }
      if(newPriority == null || newPriority < 1 || newPriority > 9)
      {
         throw new QUserFacingException("Priority must be between 1 and 9.");
      }

      GetOutput getOutput = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      QRecord task = getOutput.getRecord();
      if(task == null)
      {
         throw new QUserFacingException("Task not found: " + taskId);
      }

      Integer previousPriority = task.getValueInteger("priority");

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("priority", newPriority)));

      LOG.info("Task reprioritized",
         logPair("taskId", taskId),
         logPair("previousPriority", previousPriority),
         logPair("newPriority", newPriority));

      output.addValue("resultMessage", "Task " + taskId + " priority changed from " + previousPriority + " to " + newPriority + ".");
   }
}
