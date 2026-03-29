/*******************************************************************************
 ** Unit tests for all WMS widget renderers.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.StatisticsData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WidgetRendererTest extends BaseTest
{

   /*******************************************************************************
    ** Helper: create a RenderWidgetInput with optional warehouse filter.
    *******************************************************************************/
   private RenderWidgetInput createInput(Integer warehouseId)
   {
      RenderWidgetInput input = new RenderWidgetInput();
      Map<String, String> queryParams = new HashMap<>();
      if(warehouseId != null)
      {
         queryParams.put("warehouseId", String.valueOf(warehouseId));
      }
      input.setQueryParams(queryParams);
      return (input);
   }



   ///////////////////////////////////////////////////////////////////////////
   // TaskQueueSummaryRenderer                                              //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test TaskQueueSummaryRenderer with data.
    *******************************************************************************/
   @Test
   void testTaskQueueSummaryRenderer_withTasks_returnsMultiStatistics() throws QException
   {
      Integer warehouseId = insertWarehouse();

      insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());
      insertTask(warehouseId, TaskType.PICK.getId(), TaskStatus.ASSIGNED.getId());

      RenderWidgetOutput output = new TaskQueueSummaryRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(MultiStatisticsData.class);
   }



   /*******************************************************************************
    ** Test TaskQueueSummaryRenderer with no data returns valid output.
    *******************************************************************************/
   @Test
   void testTaskQueueSummaryRenderer_noData_returnsEmptyWidget() throws QException
   {
      RenderWidgetOutput output = new TaskQueueSummaryRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(MultiStatisticsData.class);
   }



   /*******************************************************************************
    ** Test TaskQueueSummaryRenderer with warehouse filter.
    *******************************************************************************/
   @Test
   void testTaskQueueSummaryRenderer_withWarehouseFilter_filtersResults() throws QException
   {
      Integer wh1 = insertWarehouse("WH1", "WH1");
      Integer wh2 = insertWarehouse("WH2", "WH2");

      insertTask(wh1, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());
      insertTask(wh2, TaskType.PICK.getId(), TaskStatus.PENDING.getId());

      RenderWidgetOutput output = new TaskQueueSummaryRenderer().render(createInput(wh1));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(MultiStatisticsData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // InventorySummaryRenderer                                              //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test InventorySummaryRenderer with inventory data.
    *******************************************************************************/
   @Test
   void testInventorySummaryRenderer_withData_returnsStatistics() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      RenderWidgetOutput output = new InventorySummaryRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(StatisticsData.class);
   }



   /*******************************************************************************
    ** Test InventorySummaryRenderer with empty data.
    *******************************************************************************/
   @Test
   void testInventorySummaryRenderer_noData_returnsValidWidget() throws QException
   {
      insertWarehouse();
      insertLocation(1);

      RenderWidgetOutput output = new InventorySummaryRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(StatisticsData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // WorkerProductivityRenderer                                            //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test WorkerProductivityRenderer with completed tasks.
    *******************************************************************************/
   @Test
   void testWorkerProductivityRenderer_withData_returnsTableData() throws QException
   {
      Integer warehouseId = insertWarehouse();

      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)
         .withQuantityCompleted(new BigDecimal("20"))
         .withCompletedBy("worker1")
         .withCompletedDate(Instant.now())));

      RenderWidgetOutput output = new WorkerProductivityRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   /*******************************************************************************
    ** Test WorkerProductivityRenderer with no data.
    *******************************************************************************/
   @Test
   void testWorkerProductivityRenderer_noData_returnsEmptyTable() throws QException
   {
      RenderWidgetOutput output = new WorkerProductivityRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // ActiveWorkersRenderer                                                 //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test ActiveWorkersRenderer with active workers.
    *******************************************************************************/
   @Test
   void testActiveWorkersRenderer_withData_returnsTableData() throws QException
   {
      Integer warehouseId = insertWarehouse();

      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withAssignedTo("worker1")
         .withStartedDate(Instant.now())));

      RenderWidgetOutput output = new ActiveWorkersRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   /*******************************************************************************
    ** Test ActiveWorkersRenderer with no active workers.
    *******************************************************************************/
   @Test
   void testActiveWorkersRenderer_noData_returnsEmptyTable() throws QException
   {
      RenderWidgetOutput output = new ActiveWorkersRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // TaskAgingRenderer                                                     //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test TaskAgingRenderer with tasks of varying ages covering all buckets.
    *******************************************************************************/
   @Test
   void testTaskAgingRenderer_withData_returnsChartData() throws QException
   {
      Integer warehouseId = insertWarehouse();

      ///////////////////////////////////////////////////////////////////
      // 0-15 min bucket                                               //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)
         .withCreateDate(Instant.now())));

      ///////////////////////////////////////////////////////////////////
      // 15-30 min bucket                                              //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)
         .withCreateDate(Instant.now().minusSeconds(20 * 60))));

      ///////////////////////////////////////////////////////////////////
      // 30-60 min bucket                                              //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withCreateDate(Instant.now().minusSeconds(45 * 60))));

      ///////////////////////////////////////////////////////////////////
      // 1-4h bucket                                                   //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)
         .withCreateDate(Instant.now().minusSeconds(2 * 3600))));

      ///////////////////////////////////////////////////////////////////
      // 4h+ bucket                                                    //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withCreateDate(Instant.now().minusSeconds(5 * 3600))));

      ///////////////////////////////////////////////////////////////////
      // Null createDate (should go to 4h+ bucket)                     //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(5)));

      RenderWidgetOutput output = new TaskAgingRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(ChartData.class);
   }



   /*******************************************************************************
    ** Test TaskAgingRenderer with empty data.
    *******************************************************************************/
   @Test
   void testTaskAgingRenderer_noData_returnsValidChart() throws QException
   {
      RenderWidgetOutput output = new TaskAgingRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(ChartData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // LowStockAlertsRenderer                                                //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test LowStockAlertsRenderer with low stock items.
    *******************************************************************************/
   @Test
   void testLowStockAlertsRenderer_withLowStock_returnsTableData() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // Create an item with a reorder point                           //
      ///////////////////////////////////////////////////////////////////
      QRecord itemRecord = new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME).withRecordEntity(new WmsItem()
         .withSku("LOW-SKU")
         .withName("Low Stock Item")
         .withReorderPoint(100)
         .withIsActive(true)))
      .getRecords().get(0);

      Integer itemId = itemRecord.getValueInteger("id");
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("10"));

      RenderWidgetOutput output = new LowStockAlertsRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   /*******************************************************************************
    ** Test LowStockAlertsRenderer with no items.
    *******************************************************************************/
   @Test
   void testLowStockAlertsRenderer_noData_returnsEmptyTable() throws QException
   {
      RenderWidgetOutput output = new LowStockAlertsRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(TableData.class);
   }



   ///////////////////////////////////////////////////////////////////////////
   // InventoryAccuracyRenderer                                             //
   ///////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Test InventoryAccuracyRenderer with completed count tasks.
    *******************************************************************************/
   @Test
   void testInventoryAccuracyRenderer_withCompletedCounts_returnsStatistics() throws QException
   {
      Integer warehouseId = insertWarehouse();

      ///////////////////////////////////////////////////////////////////
      // Two accurate and one inaccurate count                         //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.COUNT.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)
         .withExpectedQuantity(new BigDecimal("50"))
         .withCountedQuantity(new BigDecimal("50"))));

      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.COUNT.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)
         .withExpectedQuantity(new BigDecimal("30"))
         .withCountedQuantity(new BigDecimal("30"))));

      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.COUNT.getId())
         .withTaskStatusId(TaskStatus.COMPLETED.getId())
         .withPriority(5)
         .withExpectedQuantity(new BigDecimal("40"))
         .withCountedQuantity(new BigDecimal("35"))));

      RenderWidgetOutput output = new InventoryAccuracyRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(StatisticsData.class);
   }



   /*******************************************************************************
    ** Test InventoryAccuracyRenderer with no count tasks.
    *******************************************************************************/
   @Test
   void testInventoryAccuracyRenderer_noData_returnsNAWidget() throws QException
   {
      RenderWidgetOutput output = new InventoryAccuracyRenderer().render(createInput(null));
      assertThat(output).isNotNull();
      assertThat(output.getWidgetData()).isInstanceOf(StatisticsData.class);
   }
}
