/*******************************************************************************
 ** Backend step for HoldTask.  Sets the task status to ON_HOLD and records
 ** the hold reason in the notes field.
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


public class HoldTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(HoldTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");
      String holdReason = input.getValueString("holdReason");

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
      if(currentStatus == TaskStatus.COMPLETED || currentStatus == TaskStatus.CANCELLED || currentStatus == TaskStatus.ON_HOLD)
      {
         throw new QUserFacingException("Task cannot be put on hold in its current status: "
            + (currentStatus != null ? currentStatus.getLabel() : "Unknown"));
      }

      String existingNotes = task.getValueString("notes");
      String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "")
         + "ON_HOLD: " + (holdReason != null ? holdReason : "No reason given");

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("taskStatusId", TaskStatus.ON_HOLD.getPossibleValueId())
         .withValue("notes", updatedNotes)));

      LOG.info("Task put on hold", logPair("taskId", taskId), logPair("reason", holdReason));
      output.addValue("resultMessage", "Task " + taskId + " has been put on hold.");
   }
}
