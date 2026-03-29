/*******************************************************************************
 ** Unit tests for AutoAssignTasksStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
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


class AutoAssignTasksStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that a pending task gets assigned to an available worker.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_assignedToAvailableWorker() throws Exception
   {
      Integer warehouseId = insertWarehouse();

      /////////////////////////////////////////////////////////////////////////
      // Create a completed task with a known worker (makes worker known)    //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("assignedTo", "worker-a")));

      /////////////////////////////////////////////////////////////////////////
      // Create a pending task to be assigned                                //
      /////////////////////////////////////////////////////////////////////////
      Integer pendingTaskId = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
         .withValue("priority", 1))).getRecords().get(0).getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AutoAssignTasksStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify task was assigned                                            //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(pendingTaskId)).getRecord();
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.ASSIGNED.getPossibleValueId());
      assertThat(task.getValueString("assignedTo")).isEqualTo("worker-a");
      assertThat(output.getValueInteger("tasksAssigned")).isEqualTo(1);
   }



   /*******************************************************************************
    ** Test that when no workers are known, no tasks are assigned.
    *******************************************************************************/
   @Test
   void testRun_noKnownWorkers_noTasksAssigned() throws Exception
   {
      Integer warehouseId = insertWarehouse();

      /////////////////////////////////////////////////////////////////////////
      // Create a pending task but no previously assigned tasks              //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
         .withValue("priority", 1)));

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AutoAssignTasksStep().run(input, output);

      assertThat(output.getValueInteger("tasksAssigned")).isEqualTo(0);
      assertThat(output.getValueString("resultMessage")).contains("No known workers");
   }
}
