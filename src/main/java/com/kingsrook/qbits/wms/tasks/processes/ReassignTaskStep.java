/*******************************************************************************
 ** Backend step for ReassignTask.  Changes the assignedTo on a task that is
 ** in PENDING, ASSIGNED, or IN_PROGRESS status.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.time.Instant;
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


public class ReassignTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReassignTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer taskId = input.getValueInteger("taskId");
      String newAssignee = input.getValueString("newAssignee");

      if(taskId == null)
      {
         throw new QUserFacingException("Task ID is required.");
      }
      if(newAssignee == null || newAssignee.isBlank())
      {
         throw new QUserFacingException("New assignee is required.");
      }

      GetOutput getOutput = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      QRecord task = getOutput.getRecord();
      if(task == null)
      {
         throw new QUserFacingException("Task not found: " + taskId);
      }

      Integer currentStatusId = task.getValueInteger("taskStatusId");
      TaskStatus currentStatus = TaskStatus.getById(currentStatusId);
      if(currentStatus != TaskStatus.PENDING && currentStatus != TaskStatus.ASSIGNED && currentStatus != TaskStatus.IN_PROGRESS)
      {
         throw new QUserFacingException("Task cannot be reassigned in its current status: "
            + (currentStatus != null ? currentStatus.getLabel() : "Unknown"));
      }

      String previousAssignee = task.getValueString("assignedTo");

      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("assignedTo", newAssignee)
         .withValue("assignedDate", Instant.now())
         .withValue("taskStatusId", TaskStatus.ASSIGNED.getPossibleValueId())));

      LOG.info("Task reassigned",
         logPair("taskId", taskId),
         logPair("previousAssignee", previousAssignee),
         logPair("newAssignee", newAssignee));

      output.addValue("resultMessage", "Task " + taskId + " reassigned from "
         + (previousAssignee != null ? previousAssignee : "unassigned") + " to " + newAssignee + ".");
   }
}
