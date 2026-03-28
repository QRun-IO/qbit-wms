/*******************************************************************************
 ** Full integration test for the WMS QBit.
 ** Exercises registration, insert, query, and cross-entity operations
 ** using the in-memory backend.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsItemCategory;
import com.kingsrook.qbits.wms.core.model.WmsLicensePlate;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsTaskTypeConfig;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;
import com.kingsrook.qbits.wms.core.model.WmsVendor;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsQBitIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** Test full lifecycle: warehouse -> zone -> location -> item -> inventory.
    *******************************************************************************/
   @Test
   void testFullLifecycle_warehouseToInventory_allInsertSucceed() throws QException
   {
      Integer warehouseId = insertWarehouse("Integration WH", "IWH1");
      Integer zoneId = insertZone(warehouseId, "Int Zone", "IZ01");
      Integer locationId = insertLocation(warehouseId, zoneId, "INT-LOC-001");
      Integer itemId = insertItem("INT-SKU", "Integration Item");

      Integer inventoryId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100.0"));
      assertThat(inventoryId).isNotNull();

      ////////////////////////////////////////////
      // Query back the inventory and verify    //
      ////////////////////////////////////////////
      QueryOutput output = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsInventory inv = new WmsInventory(output.getRecords().get(0));
      assertThat(inv.getWarehouseId()).isEqualTo(warehouseId);
      assertThat(inv.getItemId()).isEqualTo(itemId);
      assertThat(inv.getLocationId()).isEqualTo(locationId);
      assertThat(inv.getQuantityOnHand()).isEqualByComparingTo(new BigDecimal("100.0"));
      assertThat(inv.getInventoryStatusId()).isEqualTo(InventoryStatus.AVAILABLE.getId());
   }



   /*******************************************************************************
    ** Test that all 16 core tables are queryable (even if empty).
    *******************************************************************************/
   @Test
   void testAllTablesQueryable_emptyTables_noErrors() throws QException
   {
      String[] tableNames = {
         WmsWarehouse.TABLE_NAME,
         WmsZone.TABLE_NAME,
         WmsLocation.TABLE_NAME,
         WmsClient.TABLE_NAME,
         WmsVendor.TABLE_NAME,
         WmsItemCategory.TABLE_NAME,
         WmsItem.TABLE_NAME,
         WmsUnitOfMeasure.TABLE_NAME,
         WmsLicensePlate.TABLE_NAME,
         WmsInventory.TABLE_NAME,
         WmsInventoryTransaction.TABLE_NAME,
         WmsInventoryHold.TABLE_NAME,
         WmsTask.TABLE_NAME,
         WmsTaskTypeConfig.TABLE_NAME,
         WmsCycleCount.TABLE_NAME,
         WmsCycleCountLine.TABLE_NAME
      };

      for(String tableName : tableNames)
      {
         QueryOutput output = new QueryAction().execute(new QueryInput(tableName));
         assertThat(output).as("Query should succeed for table: " + tableName).isNotNull();
         assertThat(output.getRecords()).as("Records list for table: " + tableName).isNotNull();
      }
   }



   /*******************************************************************************
    ** Test creating a MOVE task with source and destination locations.
    *******************************************************************************/
   @Test
   void testCreateMoveTask_withLocations_taskCreatedSuccessfully() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId);
      Integer sourceLoc = insertLocation(warehouseId, zoneId, "SRC-001");
      Integer destLoc = insertLocation(warehouseId, zoneId, "DST-001");
      Integer itemId = insertItem();

      /////////////////////////////
      // Insert a move task      //
      /////////////////////////////
      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME)
            .withRecordEntity(new WmsTask()
               .withWarehouseId(warehouseId)
               .withTaskTypeId(TaskType.MOVE.getId())
               .withTaskStatusId(TaskStatus.PENDING.getId())
               .withPriority(50)
               .withItemId(itemId)
               .withSourceLocationId(sourceLoc)
               .withDestinationLocationId(destLoc)
               .withQuantityRequested(new BigDecimal("25.0"))))
         .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");
      assertThat(taskId).isNotNull();

      ///////////////////////////////////
      // Query back and verify fields  //
      ///////////////////////////////////
      QueryOutput output = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      WmsTask task = new WmsTask(output.getRecords().get(0));
      assertThat(task.getTaskTypeId()).isEqualTo(TaskType.MOVE.getId());
      assertThat(task.getTaskStatusId()).isEqualTo(TaskStatus.PENDING.getId());
      assertThat(task.getSourceLocationId()).isEqualTo(sourceLoc);
      assertThat(task.getDestinationLocationId()).isEqualTo(destLoc);
      assertThat(task.getQuantityRequested()).isEqualByComparingTo(new BigDecimal("25.0"));
   }



   /*******************************************************************************
    ** Test creating an inventory transaction (the ledger).
    *******************************************************************************/
   @Test
   void testCreateInventoryTransaction_moveType_recordInserted() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer fromLoc = insertLocation(warehouseId, null, "FROM-001");
      Integer toLoc = insertLocation(warehouseId, null, "TO-001");

      new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME)
         .withRecordEntity(new WmsInventoryTransaction()
            .withWarehouseId(warehouseId)
            .withItemId(itemId)
            .withTransactionTypeId(TransactionType.MOVE.getId())
            .withFromLocationId(fromLoc)
            .withToLocationId(toLoc)
            .withQuantity(new BigDecimal("10.0"))
            .withPerformedBy("testuser")
            .withPerformedDate(Instant.now())));

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsInventoryTransaction txn = new WmsInventoryTransaction(output.getRecords().get(0));
      assertThat(txn.getTransactionTypeId()).isEqualTo(TransactionType.MOVE.getId());
      assertThat(txn.getFromLocationId()).isEqualTo(fromLoc);
      assertThat(txn.getToLocationId()).isEqualTo(toLoc);
   }



   /*******************************************************************************
    ** Test creating a cycle count with count lines.
    *******************************************************************************/
   @Test
   void testCreateCycleCountWithLines_fullInsert_bothRecordsExist() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);
      Integer itemId = insertItem();

      Integer ccId = insertCycleCount(warehouseId);

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME)
         .withRecordEntity(new WmsCycleCountLine()
            .withCycleCountId(ccId)
            .withLocationId(locationId)
            .withItemId(itemId)
            .withExpectedQuantity(new BigDecimal("50.0"))
            .withStatus("PENDING")));

      QueryOutput lineOutput = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      assertThat(lineOutput.getRecords()).hasSize(1);

      WmsCycleCountLine line = new WmsCycleCountLine(lineOutput.getRecords().get(0));
      assertThat(line.getCycleCountId()).isEqualTo(ccId);
      assertThat(line.getLocationId()).isEqualTo(locationId);
      assertThat(line.getItemId()).isEqualTo(itemId);
   }



   /*******************************************************************************
    ** Test inserting a client and querying it back.
    *******************************************************************************/
   @Test
   void testInsertClient_queryBack_fieldsMatch() throws QException
   {
      new InsertAction().execute(new InsertInput(WmsClient.TABLE_NAME)
         .withRecordEntity(new WmsClient()
            .withName("Test Client")
            .withCode("TC01")
            .withContactName("Contact")
            .withContactEmail("c@test.com")
            .withIsActive(true)));

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsClient.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsClient client = new WmsClient(output.getRecords().get(0));
      assertThat(client.getName()).isEqualTo("Test Client");
      assertThat(client.getCode()).isEqualTo("TC01");
   }



   /*******************************************************************************
    ** Test inserting a vendor and querying it back.
    *******************************************************************************/
   @Test
   void testInsertVendor_queryBack_fieldsMatch() throws QException
   {
      new InsertAction().execute(new InsertInput(WmsVendor.TABLE_NAME)
         .withRecordEntity(new WmsVendor()
            .withCode("V01")
            .withName("Vendor One")
            .withContactName("VContact")
            .withDefaultLeadTimeDays(14)
            .withIsActive(true)));

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsVendor.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsVendor vendor = new WmsVendor(output.getRecords().get(0));
      assertThat(vendor.getName()).isEqualTo("Vendor One");
      assertThat(vendor.getDefaultLeadTimeDays()).isEqualTo(14);
   }



   /*******************************************************************************
    ** Test inserting a license plate with location reference.
    *******************************************************************************/
   @Test
   void testInsertLicensePlate_withLocation_queryBack() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);
      Integer lpnId = insertLicensePlate(warehouseId, "LPN-INT-001");

      assertThat(lpnId).isNotNull();

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsLicensePlate.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);
      assertThat(output.getRecords().get(0).getValueString("lpnBarcode")).isEqualTo("LPN-INT-001");
   }



   /*******************************************************************************
    ** Test inserting inventory hold and querying it back.
    *******************************************************************************/
   @Test
   void testInsertInventoryHold_queryBack_fieldsMatch() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();

      new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME)
         .withRecordEntity(new WmsInventoryHold()
            .withWarehouseId(warehouseId)
            .withItemId(itemId)
            .withHoldTypeId(1)
            .withReason("QC Hold")
            .withPlacedBy("admin")
            .withPlacedDate(Instant.now())
            .withStatus("ACTIVE")));

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsInventoryHold.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsInventoryHold hold = new WmsInventoryHold(output.getRecords().get(0));
      assertThat(hold.getReason()).isEqualTo("QC Hold");
      assertThat(hold.getStatus()).isEqualTo("ACTIVE");
   }



   /*******************************************************************************
    ** Test multiple inserts into the same table.
    *******************************************************************************/
   @Test
   void testMultipleInserts_warehouses_allQueryable() throws QException
   {
      insertWarehouse("Warehouse A", "WA");
      insertWarehouse("Warehouse B", "WB");
      insertWarehouse("Warehouse C", "WC");

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsWarehouse.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(3);
   }



   /*******************************************************************************
    ** Test that the QInstance passes validation.
    *******************************************************************************/
   @Test
   void testQInstanceValidation_passes()
   {
      QInstance qInstance = QContext.getQInstance();
      assertThat(qInstance).isNotNull();
      assertThat(qInstance.getTables()).isNotEmpty();
      assertThat(qInstance.getPossibleValueSources()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test task type config insert and query.
    *******************************************************************************/
   @Test
   void testInsertTaskTypeConfig_queryBack_fieldsMatch() throws QException
   {
      new InsertAction().execute(new InsertInput(WmsTaskTypeConfig.TABLE_NAME)
         .withRecordEntity(new WmsTaskTypeConfig()
            .withTaskTypeId(TaskType.PICK.getId())
            .withDefaultPriority(50)
            .withAutoAssignEnabled(true)
            .withScanSourceLocation(true)
            .withScanDestinationLocation(false)
            .withScanItem(true)
            .withEscalationMinutes(30)
            .withDescription("Pick config")));

      QueryOutput output = new QueryAction().execute(new QueryInput(WmsTaskTypeConfig.TABLE_NAME));
      assertThat(output.getRecords()).hasSize(1);

      WmsTaskTypeConfig config = new WmsTaskTypeConfig(output.getRecords().get(0));
      assertThat(config.getTaskTypeId()).isEqualTo(TaskType.PICK.getId());
      assertThat(config.getAutoAssignEnabled()).isTrue();
   }
}
