/*******************************************************************************
 ** Unit tests for {@link StaleTaskCheckStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
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


class StaleTaskCheckStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that stale ASSIGNED tasks are unassigned back to PENDING.
    *******************************************************************************/
   @Test
   void testRun_staleAssignedTask_unassignedToPending() throws QException
   {
      Integer warehouseId = insertWarehouse();

      ///////////////////////////////////////////////////////////////////
      // Create a task assigned 60 minutes ago (stale threshold = 30)  //
      ///////////////////////////////////////////////////////////////////
      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withAssignedTo("worker1")
         .withAssignedDate(Instant.now().minus(60, ChronoUnit.MINUTES))))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      new StaleTaskCheckStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      assertThat(task.getValueString("assignedTo")).isNull();
      assertThat(task.getValueString("notes")).contains("AUTO-UNASSIGNED");
   }



   /*******************************************************************************
    ** Test that non-stale ASSIGNED tasks are ignored.
    *******************************************************************************/
   @Test
   void testRun_recentAssignedTask_ignored() throws QException
   {
      Integer warehouseId = insertWarehouse();

      ///////////////////////////////////////////////////////////////////
      // Create a task assigned 5 minutes ago (not stale)              //
      ///////////////////////////////////////////////////////////////////
      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withAssignedTo("worker1")
         .withAssignedDate(Instant.now().minus(5, ChronoUnit.MINUTES))))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      new StaleTaskCheckStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ASSIGNED.getPossibleValueId());
      assertThat(task.getValueString("assignedTo")).isEqualTo("worker1");
   }



   /*******************************************************************************
    ** Test that PENDING tasks are not affected by stale check.
    *******************************************************************************/
   @Test
   void testRun_pendingTasks_ignored() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      new StaleTaskCheckStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
   }
}
