/*******************************************************************************
 ** Unit tests for {@link GetNextTaskStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
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


class GetNextTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that the highest priority PENDING task is returned.
    *******************************************************************************/
   @Test
   void testRun_multiplePendingTasks_returnsHighestPriority() throws QException
   {
      Integer warehouseId = insertWarehouse();

      ///////////////////////////////////////////////////////////////////
      // Insert two tasks: priority 5 and priority 1 (lower = higher) //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)));

      QRecord highPriorityTask = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(1)))
      .getRecords().get(0);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("workerUserId", "worker1");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new GetNextTaskStep().run(input, output);

      assertThat(output.getValue("taskId")).isEqualTo(highPriorityTask.getValueInteger("id"));
      assertThat(output.getValueString("resultMessage")).isEqualTo("Task assigned successfully.");
   }



   /*******************************************************************************
    ** Test that zone filter narrows results.
    *******************************************************************************/
   @Test
   void testRun_withZoneFilter_respectsZone() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer zone1 = insertZone(warehouseId, "Zone A", "ZA");
      Integer zone2 = insertZone(warehouseId, "Zone B", "ZB");

      ///////////////////////////////////////////////////////////////////
      // Task in zone1                                                 //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)
         .withZoneId(zone1)));

      ///////////////////////////////////////////////////////////////////
      // Task in zone2                                                 //
      ///////////////////////////////////////////////////////////////////
      QRecord zone2Task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)
         .withZoneId(zone2)))
      .getRecords().get(0);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("workerUserId", "worker1");
      input.addValue("zoneId", zone2);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new GetNextTaskStep().run(input, output);

      assertThat(output.getValue("taskId")).isEqualTo(zone2Task.getValueInteger("id"));
   }



   /*******************************************************************************
    ** Test that the task is assigned to the requesting user.
    *******************************************************************************/
   @Test
   void testRun_taskAssigned_assignedToWorker() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("workerUserId", "worker42");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new GetNextTaskStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify the task was assigned in the database                  //
      ///////////////////////////////////////////////////////////////////
      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ASSIGNED.getPossibleValueId());
      assertThat(task.getValueString("assignedTo")).isEqualTo("worker42");
   }



   /*******************************************************************************
    ** Test returns nothing when no tasks are available.
    *******************************************************************************/
   @Test
   void testRun_noTasks_returnsNoTasksMessage() throws QException
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("workerUserId", "worker1");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new GetNextTaskStep().run(input, output);

      assertThat(output.getValueString("resultMessage")).contains("No tasks available");
      assertThat(output.getValue("taskId")).isNull();
   }
}
