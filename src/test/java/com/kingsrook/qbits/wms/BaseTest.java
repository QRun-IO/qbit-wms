/*******************************************************************************
 ** Base test class for WMS QBit tests.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.enums.EquipmentType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.LocationType;
import com.kingsrook.qbits.wms.core.enums.LpnStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.ZoneType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLicensePlate;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class BaseTest
{
   public static final String BACKEND_NAME = "memory";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void baseBeforeEach() throws Exception
   {
      QInstance qInstance = defineQInstance();
      new QInstanceValidator().validate(qInstance);
      QContext.init(qInstance, new QSession());

      MemoryRecordStore.fullReset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void baseAfterEach()
   {
      QContext.clear();
      MemoryRecordStore.fullReset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QInstance defineQInstance() throws QException
   {
      QInstance qInstance = new QInstance();

      qInstance.addBackend(new QBackendMetaData()
         .withName(BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class));

      WmsQBitConfig config = new WmsQBitConfig()
         .withBackendName(BACKEND_NAME);

      new WmsQBitProducer()
         .withQBitConfig(config)
         .produce(qInstance);

      return (qInstance);
   }



   /*******************************************************************************
    ** Insert a warehouse and return its id.
    *******************************************************************************/
   public static Integer insertWarehouse() throws QException
   {
      return insertWarehouse("Test Warehouse", "WH01");
   }



   /*******************************************************************************
    ** Insert a warehouse with the given name and code and return its id.
    *******************************************************************************/
   public static Integer insertWarehouse(String name, String code) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsWarehouse.TABLE_NAME)
            .withRecordEntity(new WmsWarehouse()
               .withName(name)
               .withCode(code)
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert warehouse");
      return (id);
   }



   /*******************************************************************************
    ** Insert a zone and return its id.
    *******************************************************************************/
   public static Integer insertZone(Integer warehouseId) throws QException
   {
      return insertZone(warehouseId, "Test Zone", "Z01");
   }



   /*******************************************************************************
    ** Insert a zone with the given name and code and return its id.
    *******************************************************************************/
   public static Integer insertZone(Integer warehouseId, String name, String code) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsZone.TABLE_NAME)
            .withRecordEntity(new WmsZone()
               .withWarehouseId(warehouseId)
               .withName(name)
               .withCode(code)
               .withZoneTypeId(ZoneType.BULK.getId())
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert zone");
      return (id);
   }



   /*******************************************************************************
    ** Insert a location and return its id.
    *******************************************************************************/
   public static Integer insertLocation(Integer warehouseId) throws QException
   {
      return insertLocation(warehouseId, null, "LOC-001");
   }



   /*******************************************************************************
    ** Insert a location with the given zone and barcode and return its id.
    *******************************************************************************/
   public static Integer insertLocation(Integer warehouseId, Integer zoneId, String barcode) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsLocation.TABLE_NAME)
            .withRecordEntity(new WmsLocation()
               .withWarehouseId(warehouseId)
               .withZoneId(zoneId)
               .withBarcode(barcode)
               .withLocationTypeId(LocationType.BIN.getId())
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert location");
      return (id);
   }



   /*******************************************************************************
    ** Insert an item and return its id.
    *******************************************************************************/
   public static Integer insertItem() throws QException
   {
      return insertItem("TEST-SKU", "Test Item");
   }



   /*******************************************************************************
    ** Insert an item with the given sku and name and return its id.
    *******************************************************************************/
   public static Integer insertItem(String sku, String name) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME)
            .withRecordEntity(new WmsItem()
               .withSku(sku)
               .withName(name)
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert item");
      return (id);
   }



   /*******************************************************************************
    ** Insert a task and return its id.
    *******************************************************************************/
   public static Integer insertTask(Integer warehouseId, Integer taskTypeId, Integer taskStatusId) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME)
            .withRecordEntity(new WmsTask()
               .withWarehouseId(warehouseId)
               .withTaskTypeId(taskTypeId)
               .withTaskStatusId(taskStatusId)
               .withPriority(50)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert task");
      return (id);
   }



   /*******************************************************************************
    ** Insert inventory and return its id.
    *******************************************************************************/
   public static Integer insertInventory(Integer warehouseId, Integer itemId, Integer locationId, BigDecimal quantity) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsInventory.TABLE_NAME)
            .withRecordEntity(new WmsInventory()
               .withWarehouseId(warehouseId)
               .withItemId(itemId)
               .withLocationId(locationId)
               .withQuantityOnHand(quantity)
               .withQuantityAvailable(quantity)
               .withInventoryStatusId(InventoryStatus.AVAILABLE.getId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert inventory");
      return (id);
   }



   /*******************************************************************************
    ** Insert a license plate and return its id.
    *******************************************************************************/
   public static Integer insertLicensePlate(Integer warehouseId, String lpnBarcode) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsLicensePlate.TABLE_NAME)
            .withRecordEntity(new WmsLicensePlate()
               .withWarehouseId(warehouseId)
               .withLpnBarcode(lpnBarcode)
               .withStatusId(LpnStatus.ACTIVE.getId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert license plate");
      return (id);
   }



   /*******************************************************************************
    ** Insert a cycle count and return its id.
    *******************************************************************************/
   public static Integer insertCycleCount(Integer warehouseId) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsCycleCount.TABLE_NAME)
            .withRecordEntity(new WmsCycleCount()
               .withWarehouseId(warehouseId)
               .withCountTypeId(CycleCountType.FULL.getId())
               .withCycleCountStatusId(CycleCountStatus.PLANNED.getId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert cycle count");
      return (id);
   }
}
