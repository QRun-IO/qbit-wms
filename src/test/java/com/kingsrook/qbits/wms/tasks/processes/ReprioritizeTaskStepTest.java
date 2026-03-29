/*******************************************************************************
 ** Unit tests for {@link ReprioritizeTaskStep}.
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


class ReprioritizeTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that priority is changed successfully.
    *******************************************************************************/
   @Test
   void testRun_validPriority_changesPriority() throws QException
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
      input.addValue("newPriority", 1);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ReprioritizeTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("priority")).isEqualTo(1);
      assertThat(output.getValueString("resultMessage")).contains("priority changed");
   }



   /*******************************************************************************
    ** Test that priority outside 1-9 is rejected.
    *******************************************************************************/
   @Test
   void testRun_invalidPriority_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.PICK.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("newPriority", 10);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReprioritizeTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Priority must be between 1 and 9");
   }



   /*******************************************************************************
    ** Test that null priority is rejected.
    *******************************************************************************/
   @Test
   void testRun_nullPriority_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.PICK.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReprioritizeTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Priority must be between 1 and 9");
   }
}
