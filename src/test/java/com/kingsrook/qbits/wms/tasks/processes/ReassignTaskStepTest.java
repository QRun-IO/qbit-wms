/*******************************************************************************
 ** Unit tests for {@link ReassignTaskStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ReassignTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test reassigning an ASSIGNED task to a new user.
    *******************************************************************************/
   @Test
   void testRun_assignedTask_reassignedToNewUser() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withAssignedTo("oldWorker")))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("newAssignee", "newWorker");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ReassignTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueString("assignedTo")).isEqualTo("newWorker");
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ASSIGNED.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("reassigned");
      assertThat(output.getValueString("resultMessage")).contains("oldWorker");
      assertThat(output.getValueString("resultMessage")).contains("newWorker");
   }



   /*******************************************************************************
    ** Test that reassignment clears the previous assignment properly.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_assignsNewUser() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("newAssignee", "newWorker");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ReassignTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueString("assignedTo")).isEqualTo("newWorker");
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ASSIGNED.getPossibleValueId());
      assertThat(task.getValue("assignedDate")).isNotNull();
   }



   /*******************************************************************************
    ** Test that reassigning a COMPLETED task throws exception.
    *******************************************************************************/
   @Test
   void testRun_completedTask_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("newAssignee", "someone");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReassignTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("cannot be reassigned");
   }
}
