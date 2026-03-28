/*******************************************************************************
 ** Backend step for PauseTask.  Sets the task status to PAUSED and records
 ** partial progress notes.
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


public class PauseTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(PauseTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");
      String pauseReason = input.getValueString("pauseReason");

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
      if(currentStatus != TaskStatus.IN_PROGRESS && currentStatus != TaskStatus.ASSIGNED)
      {
         throw new QUserFacingException("Only IN_PROGRESS or ASSIGNED tasks can be paused. Current status: "
            + (currentStatus != null ? currentStatus.getLabel() : "Unknown"));
      }

      /////////////////////////////////////////////////////////////////////////
      // Append pause reason to existing notes                               //
      /////////////////////////////////////////////////////////////////////////
      String existingNotes = task.getValueString("notes");
      String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "") + "PAUSED: " + (pauseReason != null ? pauseReason : "No reason given");

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("taskStatusId", TaskStatus.PAUSED.getPossibleValueId())
         .withValue("notes", updatedNotes)));

      LOG.info("Task paused", logPair("taskId", taskId), logPair("reason", pauseReason));
      output.addValue("resultMessage", "Task " + taskId + " has been paused.");
   }
}
