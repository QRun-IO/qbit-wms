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
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qbits.wms.core.enums.AsnStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.enums.EquipmentType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.LocationType;
import com.kingsrook.qbits.wms.core.enums.LpnStatus;
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptType;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.ZoneType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLicensePlate;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsVendor;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;
import com.kingsrook.qbits.wms.receiving.model.WmsAsn;
import com.kingsrook.qbits.wms.receiving.model.WmsAsnLine;
import com.kingsrook.qbits.wms.receiving.model.WmsPurchaseOrder;
import com.kingsrook.qbits.wms.receiving.model.WmsPurchaseOrderLine;
import com.kingsrook.qbits.wms.receiving.model.WmsPutawayRule;
import com.kingsrook.qbits.wms.receiving.model.WmsReceipt;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
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

      qInstance.setAuthentication(new QAuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS));

      qInstance.addBackend(new QBackendMetaData()
         .withName(BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class));

      WmsQBitConfig config = new WmsQBitConfig()
         .withBackendName(BACKEND_NAME);

      MetaDataProducerMultiOutput multiOutput = new WmsQBitProducer()
         .withQBitConfig(config)
         .produce(qInstance);

      multiOutput.addSelfToInstance(qInstance);

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



   /*******************************************************************************
    ** Insert a vendor and return its id.
    *******************************************************************************/
   public static Integer insertVendor() throws QException
   {
      return insertVendor("Test Vendor", "VEND01");
   }



   /*******************************************************************************
    ** Insert a vendor with the given name and code and return its id.
    *******************************************************************************/
   public static Integer insertVendor(String name, String code) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsVendor.TABLE_NAME)
            .withRecordEntity(new WmsVendor()
               .withName(name)
               .withCode(code)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert vendor");
      return (id);
   }



   /*******************************************************************************
    ** Insert a purchase order and return its id.
    *******************************************************************************/
   public static Integer insertPurchaseOrder(Integer warehouseId, Integer vendorId) throws QException
   {
      return insertPurchaseOrder(warehouseId, vendorId, "PO-001");
   }



   /*******************************************************************************
    ** Insert a purchase order with the given PO number and return its id.
    *******************************************************************************/
   public static Integer insertPurchaseOrder(Integer warehouseId, Integer vendorId, String poNumber) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsPurchaseOrder.TABLE_NAME)
            .withRecordEntity(new WmsPurchaseOrder()
               .withWarehouseId(warehouseId)
               .withVendorId(vendorId)
               .withPoNumber(poNumber)
               .withStatusId(PurchaseOrderStatus.OPEN.getPossibleValueId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert purchase order");
      return (id);
   }



   /*******************************************************************************
    ** Insert a purchase order line and return its id.
    *******************************************************************************/
   public static Integer insertPurchaseOrderLine(Integer purchaseOrderId, Integer itemId) throws QException
   {
      return insertPurchaseOrderLine(purchaseOrderId, itemId, 100);
   }



   /*******************************************************************************
    ** Insert a purchase order line with the given expected quantity and return
    ** its id.
    *******************************************************************************/
   public static Integer insertPurchaseOrderLine(Integer purchaseOrderId, Integer itemId, Integer expectedQuantity) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsPurchaseOrderLine.TABLE_NAME)
            .withRecordEntity(new WmsPurchaseOrderLine()
               .withPurchaseOrderId(purchaseOrderId)
               .withItemId(itemId)
               .withExpectedQuantity(expectedQuantity)
               .withReceivedQuantity(0)
               .withStatusId(PurchaseOrderStatus.OPEN.getPossibleValueId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert purchase order line");
      return (id);
   }



   /*******************************************************************************
    ** Insert a receipt and return its id.
    *******************************************************************************/
   public static Integer insertReceipt(Integer warehouseId) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsReceipt.TABLE_NAME)
            .withRecordEntity(new WmsReceipt()
               .withWarehouseId(warehouseId)
               .withReceiptNumber("RCV-" + System.nanoTime())
               .withReceiptTypeId(ReceiptType.BLIND.getPossibleValueId())
               .withStatusId(ReceiptStatus.IN_PROGRESS.getPossibleValueId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert receipt");
      return (id);
   }



   /*******************************************************************************
    ** Insert a receipt line and return its id.
    *******************************************************************************/
   public static Integer insertReceiptLine(Integer receiptId, Integer itemId) throws QException
   {
      return insertReceiptLine(receiptId, itemId, 10);
   }



   /*******************************************************************************
    ** Insert a receipt line with the given quantity and return its id.
    *******************************************************************************/
   public static Integer insertReceiptLine(Integer receiptId, Integer itemId, Integer quantity) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsReceiptLine.TABLE_NAME)
            .withRecordEntity(new WmsReceiptLine()
               .withReceiptId(receiptId)
               .withItemId(itemId)
               .withQuantityReceived(quantity)
               .withStatusId(ReceiptStatus.IN_PROGRESS.getPossibleValueId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert receipt line");
      return (id);
   }



   /*******************************************************************************
    ** Insert an ASN and return its id.
    *******************************************************************************/
   public static Integer insertAsn(Integer purchaseOrderId) throws QException
   {
      return insertAsn(purchaseOrderId, "ASN-001");
   }



   /*******************************************************************************
    ** Insert an ASN with the given number and return its id.
    *******************************************************************************/
   public static Integer insertAsn(Integer purchaseOrderId, String asnNumber) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsAsn.TABLE_NAME)
            .withRecordEntity(new WmsAsn()
               .withPurchaseOrderId(purchaseOrderId)
               .withAsnNumber(asnNumber)
               .withStatusId(AsnStatus.PENDING.getPossibleValueId())))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert ASN");
      return (id);
   }



   /*******************************************************************************
    ** Insert a putaway rule and return its id.
    *******************************************************************************/
   public static Integer insertPutawayRule(Integer warehouseId, Integer targetZoneId) throws QException
   {
      return insertPutawayRule(warehouseId, targetZoneId, "Default Rule");
   }



   /*******************************************************************************
    ** Insert a putaway rule with the given name and return its id.
    *******************************************************************************/
   public static Integer insertPutawayRule(Integer warehouseId, Integer targetZoneId, String ruleName) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsPutawayRule.TABLE_NAME)
            .withRecordEntity(new WmsPutawayRule()
               .withWarehouseId(warehouseId)
               .withTargetZoneId(targetZoneId)
               .withRuleName(ruleName)
               .withPriority(5)
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert putaway rule");
      return (id);
   }



   /*******************************************************************************
    ** Insert an item with barcode fields populated (for receiving process tests).
    *******************************************************************************/
   public static Integer insertItemWithBarcode(String sku, String name, String barcodeUpc) throws QException
   {
      Integer id = new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME)
            .withRecordEntity(new WmsItem()
               .withSku(sku)
               .withName(name)
               .withBarcodeUpc(barcodeUpc)
               .withIsActive(true)))
         .getRecords().get(0).getValueInteger("id");
      assertNotNull(id, "Failed to insert item with barcode");
      return (id);
   }
}
