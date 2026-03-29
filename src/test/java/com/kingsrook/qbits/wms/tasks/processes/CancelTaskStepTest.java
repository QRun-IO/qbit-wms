/*******************************************************************************
 ** Unit tests for {@link CancelTaskStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CancelTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test cancelling a PENDING task succeeds.
    *******************************************************************************/
   @Test
   void testRun_pendingTask_cancels() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("cancellationReason", "No longer needed");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CancelTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.CANCELLED.getPossibleValueId());
      assertThat(task.getValueString("notes")).contains("CANCELLED: No longer needed");
      assertThat(output.getValueString("resultMessage")).contains("cancelled");
   }



   /*******************************************************************************
    ** Test cancelling an ASSIGNED task succeeds.
    *******************************************************************************/
   @Test
   void testRun_assignedTask_cancels() throws QException
   {
      Integer warehouseId = insertWarehouse();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withAssignedTo("worker1")))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("cancellationReason", "Reassigned elsewhere");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CancelTaskStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.CANCELLED.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test cancelling a COMPLETED task throws exception.
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
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CancelTaskStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("cannot be cancelled");
   }



   /*******************************************************************************
    ** Test cancelling a COUNT task resets cycle count line to PENDING.
    *******************************************************************************/
   @Test
   void testRun_countTask_resetsCycleCountLine() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.COUNT.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withItemId(itemId)
         .withSourceLocationId(locationId)
         .withCycleCountId(cycleCountId)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("10"))
         .withValue("status", "COUNTED")
         .withValue("taskId", taskId)
      ));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("cancellationReason", "Bad count");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CancelTaskStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify the cycle count line was reset to PENDING              //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      assertThat(lineQuery.getRecords().get(0).getValueString("status")).isEqualTo("PENDING");
   }



   /*******************************************************************************
    ** Test cancelling a PICK task reverses allocation.
    *******************************************************************************/
   @Test
   void testRun_pickTask_reversesAllocation() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // Insert inventory with some allocated quantity                  //
      ///////////////////////////////////////////////////////////////////
      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("quantityAllocated", new BigDecimal("20"))
         .withValue("quantityAvailable", new BigDecimal("80"))));

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withItemId(itemId)
         .withSourceLocationId(locationId)
         .withQuantityRequested(new BigDecimal("20"))))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("cancellationReason", "Order cancelled");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CancelTaskStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify allocation was reversed                                //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueBigDecimal("quantityAllocated")).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(inv.getValueBigDecimal("quantityAvailable")).isEqualByComparingTo(new BigDecimal("100"));

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.CANCELLED.getPossibleValueId());
   }
}
