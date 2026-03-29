/*******************************************************************************
 ** Unit tests for {@link HoldTaskStep}.
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


class HoldTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test putting a PENDING task ON_HOLD.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_putsOnHold() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.PICK.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("holdReason", "Waiting for materials");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new HoldTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ON_HOLD.getPossibleValueId());
      assertThat(task.getValueString("notes")).contains("ON_HOLD: Waiting for materials");
      assertThat(output.getValueString("resultMessage")).contains("on hold");
   }



   /*******************************************************************************
    ** Test that putting a COMPLETED task on hold throws exception.
    *******************************************************************************/
   @Test
   void testRun_completedTask_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("holdReason", "test");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new HoldTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("cannot be put on hold");
   }



   /*******************************************************************************
    ** Test that putting an already ON_HOLD task on hold throws exception.
    *******************************************************************************/
   @Test
   void testRun_alreadyOnHold_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.ON_HOLD.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new HoldTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("cannot be put on hold");
   }
}
