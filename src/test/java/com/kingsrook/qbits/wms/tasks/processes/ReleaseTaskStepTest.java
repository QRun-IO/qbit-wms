/*******************************************************************************
 ** Unit tests for {@link ReleaseTaskStep}.
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


class ReleaseTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test releasing an ON_HOLD task back to PENDING.
    *******************************************************************************/
   @Test
   void testRun_onHoldTask_releasedToPending() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.ON_HOLD.getId())
         .withPriority(5)
         .withAssignedTo("worker1")))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ReleaseTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      assertThat(task.getValueString("assignedTo")).isNull();
      assertThat(output.getValueString("resultMessage")).contains("released from hold");
   }



   /*******************************************************************************
    ** Test that releasing a non-ON_HOLD task throws exception.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReleaseTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Only ON_HOLD tasks");
   }
}
