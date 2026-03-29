/*******************************************************************************
 ** Unit tests for {@link ResumeTaskStep}.
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


class ResumeTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test resuming a PAUSED task transitions to IN_PROGRESS.
    *******************************************************************************/
   @Test
   void testRun_pausedTask_resumesToInProgress() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PAUSED.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ResumeTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.IN_PROGRESS.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("resumed");
   }



   /*******************************************************************************
    ** Test that resuming a non-PAUSED task is rejected.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ResumeTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Only PAUSED tasks");
   }
}
