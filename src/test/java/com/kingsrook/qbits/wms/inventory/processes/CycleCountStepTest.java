/*******************************************************************************
 ** Unit tests for {@link CycleCountStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CycleCountStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that cycle count creates lines for all inventory at the warehouse.
    *******************************************************************************/
   @Test
   void testRun_inventoryExists_createsLinesAndTasks() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId1 = insertItem("SKU-A", "Item A");
      Integer itemId2 = insertItem("SKU-B", "Item B");
      Integer loc1 = insertLocation(warehouseId, null, "LOC-A");
      Integer loc2 = insertLocation(warehouseId, null, "LOC-B");

      insertInventory(warehouseId, itemId1, loc1, new BigDecimal("100"));
      insertInventory(warehouseId, itemId2, loc2, new BigDecimal("50"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      input.addValue("countTypeId", CycleCountType.FULL.getId());
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CycleCountStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify cycle count was created                                //
      ///////////////////////////////////////////////////////////////////
      QueryOutput ccQuery = new QueryAction().execute(new QueryInput(WmsCycleCount.TABLE_NAME));
      assertThat(ccQuery.getRecords()).hasSize(1);

      ///////////////////////////////////////////////////////////////////
      // Verify 2 cycle count lines were created                       //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      assertThat(lineQuery.getRecords()).hasSize(2);

      ///////////////////////////////////////////////////////////////////
      // Verify 2 COUNT tasks were generated                           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(taskQuery.getRecords()).hasSize(2);
      for(var task : taskQuery.getRecords())
      {
         assertThat(task.getValueInteger("taskTypeId")).isEqualTo(TaskType.COUNT.getPossibleValueId());
         assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      }

      assertThat(output.getValue("lineCount")).isEqualTo(2);
      assertThat(output.getValue("taskCount")).isEqualTo(2);
      assertThat(output.getValueString("resultMessage")).contains("created successfully");
   }



   /*******************************************************************************
    ** Test that no inventory at the warehouse throws exception.
    *******************************************************************************/
   @Test
   void testRun_noInventory_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      input.addValue("countTypeId", CycleCountType.FULL.getId());
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CycleCountStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("No inventory found");
   }



   /*******************************************************************************
    ** Test that missing warehouse throws exception.
    *******************************************************************************/
   @Test
   void testRun_missingWarehouse_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("countTypeId", CycleCountType.FULL.getId());
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CycleCountStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Warehouse is required");
   }
}
