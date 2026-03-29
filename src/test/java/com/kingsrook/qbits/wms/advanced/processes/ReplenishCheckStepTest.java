/*******************************************************************************
 ** Unit tests for ReplenishCheckStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReplenishCheckStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that a replenishment rule with inventory below min creates a REPLENISH
    ** task to fill to max.
    *******************************************************************************/
   @Test
   void testRun_inventoryBelowMin_createsReplenishTask() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer pickLocationId = insertLocation(warehouseId, null, "PICK-A01");

      /////////////////////////////////////////////////////////////////////////
      // Inventory at 3, min=5, max=20 -> should create replenish task       //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, pickLocationId, new BigDecimal("3"));
      insertReplenishmentRule(warehouseId, itemId, pickLocationId, 5, 20);

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReplenishCheckStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify REPLENISH task was created                                   //
      /////////////////////////////////////////////////////////////////////////
      List<QRecord> tasks = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.REPLENISH.getPossibleValueId())))).getRecords();

      assertThat(tasks).hasSize(1);
      assertThat(tasks.get(0).getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());

      BigDecimal replenishQty = ValueUtils.getValueAsBigDecimal(tasks.get(0).getValue("quantityRequested"));
      assertThat(replenishQty.compareTo(new BigDecimal("17"))).isEqualTo(0);

      assertThat(output.getValueInteger("tasksCreated")).isEqualTo(1);
   }



   /*******************************************************************************
    ** Test that when inventory is above min, no task is created.
    *******************************************************************************/
   @Test
   void testRun_inventoryAboveMin_noTaskCreated() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer pickLocationId = insertLocation(warehouseId, null, "PICK-B01");

      /////////////////////////////////////////////////////////////////////////
      // Inventory at 10, min=5 -> no replenish needed                       //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, pickLocationId, new BigDecimal("10"));
      insertReplenishmentRule(warehouseId, itemId, pickLocationId, 5, 20);

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReplenishCheckStep().run(input, output);

      assertThat(output.getValueInteger("tasksCreated")).isEqualTo(0);
   }
}
