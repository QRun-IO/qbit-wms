/*******************************************************************************
 ** Sanity tests for BaseTest helper methods.
 ** Verifies that each helper inserts correctly and returns valid records.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLicensePlate;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class BaseTestSanityTest extends BaseTest
{

   /*******************************************************************************
    ** Test that defineQInstance creates a valid instance.
    *******************************************************************************/
   @Test
   void testDefineQInstance_createsValidInstance() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      assertThat(qInstance).isNotNull();
      assertThat(qInstance.getTable(WmsWarehouse.TABLE_NAME)).isNotNull();
      assertThat(qInstance.getTable(WmsTask.TABLE_NAME)).isNotNull();
      assertThat(qInstance.getTable(WmsInventory.TABLE_NAME)).isNotNull();
   }



   /*******************************************************************************
    ** Test insertWarehouse with defaults.
    *******************************************************************************/
   @Test
   void testInsertWarehouse_defaults_recordQueryable() throws QException
   {
      Integer id = insertWarehouse();
      assertThat(id).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsWarehouse.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsWarehouse wh = new WmsWarehouse(output.getRecords().get(0));
      assertThat(wh.getName()).isEqualTo("Test Warehouse");
      assertThat(wh.getCode()).isEqualTo("WH01");
      assertThat(wh.getIsActive()).isTrue();
   }



   /*******************************************************************************
    ** Test insertWarehouse with custom values.
    *******************************************************************************/
   @Test
   void testInsertWarehouse_customValues_returnsId() throws QException
   {
      Integer id = insertWarehouse("Custom Name", "CN01");
      assertThat(id).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsWarehouse.TABLE_NAME));
      WmsWarehouse wh = new WmsWarehouse(output.getRecords().get(0));
      assertThat(wh.getName()).isEqualTo("Custom Name");
      assertThat(wh.getCode()).isEqualTo("CN01");
   }



   /*******************************************************************************
    ** Test insertZone with defaults.
    *******************************************************************************/
   @Test
   void testInsertZone_defaults_recordQueryable() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId);
      assertThat(zoneId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsZone.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsZone zone = new WmsZone(output.getRecords().get(0));
      assertThat(zone.getName()).isEqualTo("Test Zone");
      assertThat(zone.getCode()).isEqualTo("Z01");
      assertThat(zone.getWarehouseId()).isEqualTo(warehouseId);
   }



   /*******************************************************************************
    ** Test insertLocation with defaults.
    *******************************************************************************/
   @Test
   void testInsertLocation_defaults_recordQueryable() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);
      assertThat(locationId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsLocation.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsLocation loc = new WmsLocation(output.getRecords().get(0));
      assertThat(loc.getBarcode()).isEqualTo("LOC-001");
      assertThat(loc.getWarehouseId()).isEqualTo(warehouseId);
   }



   /*******************************************************************************
    ** Test insertItem with defaults.
    *******************************************************************************/
   @Test
   void testInsertItem_defaults_recordQueryable() throws QException
   {
      Integer itemId = insertItem();
      assertThat(itemId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsItem.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsItem item = new WmsItem(output.getRecords().get(0));
      assertThat(item.getSku()).isEqualTo("TEST-SKU");
      assertThat(item.getName()).isEqualTo("Test Item");
   }



   /*******************************************************************************
    ** Test insertTask creates task with correct status.
    *******************************************************************************/
   @Test
   void testInsertTask_pendingStatus_correctFields() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.PUTAWAY.getId(), TaskStatus.PENDING.getId());
      assertThat(taskId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsTask task = new WmsTask(output.getRecords().get(0));
      assertThat(task.getWarehouseId()).isEqualTo(warehouseId);
      assertThat(task.getTaskTypeId()).isEqualTo(TaskType.PUTAWAY.getId());
      assertThat(task.getTaskStatusId()).isEqualTo(TaskStatus.PENDING.getId());
      assertThat(task.getPriority()).isEqualTo(50);
   }



   /*******************************************************************************
    ** Test insertInventory creates inventory with correct quantity.
    *******************************************************************************/
   @Test
   void testInsertInventory_withQuantity_correctFields() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer inventoryId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("75.0"));
      assertThat(inventoryId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      WmsInventory inv = new WmsInventory(output.getRecords().get(0));
      assertThat(inv.getQuantityOnHand()).isEqualByComparingTo(new BigDecimal("75.0"));
      assertThat(inv.getQuantityAvailable()).isEqualByComparingTo(new BigDecimal("75.0"));
      assertThat(inv.getInventoryStatusId()).isEqualTo(InventoryStatus.AVAILABLE.getId());
   }



   /*******************************************************************************
    ** Test insertLicensePlate helper.
    *******************************************************************************/
   @Test
   void testInsertLicensePlate_helper_returnsId() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer lpnId = insertLicensePlate(warehouseId, "LPN-SANITY");
      assertThat(lpnId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsLicensePlate.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);
      assertThat(output.getRecords().get(0).getValueString("lpnBarcode")).isEqualTo("LPN-SANITY");
   }



   /*******************************************************************************
    ** Test insertCycleCount helper.
    *******************************************************************************/
   @Test
   void testInsertCycleCount_helper_returnsId() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer ccId = insertCycleCount(warehouseId);
      assertThat(ccId).isNotNull().isPositive();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsCycleCount.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsCycleCount cc = new WmsCycleCount(output.getRecords().get(0));
      assertThat(cc.getCountTypeId()).isEqualTo(CycleCountType.FULL.getId());
      assertThat(cc.getCycleCountStatusId()).isEqualTo(CycleCountStatus.PLANNED.getId());
   }



   /*******************************************************************************
    ** Test BACKEND_NAME constant.
    *******************************************************************************/
   @Test
   void testBackendNameConstant_isMemory()
   {
      assertThat(BACKEND_NAME).isEqualTo("memory");
   }
}
