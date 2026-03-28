/*******************************************************************************
 ** Unit tests for WmsInventory entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInventoryTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsInventory.TABLE_NAME).isEqualTo("wmsInventory");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsInventory entity = new WmsInventory();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsInventory entity = new WmsInventory();
      WmsInventory result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withItemId(4)
         .withLocationId(5)
         .withLotNumber("LOT-001")
         .withSerialNumber("SN-001")
         .withExpirationDate(LocalDate.of(2027, 1, 1))
         .withManufactureDate(LocalDate.of(2026, 1, 1))
         .withLpnId(6)
         .withQuantityOnHand(new BigDecimal("100.0"))
         .withQuantityAllocated(new BigDecimal("10.0"))
         .withQuantityAvailable(new BigDecimal("90.0"))
         .withQuantityOnHold(BigDecimal.ZERO)
         .withInventoryStatusId(1)
         .withHoldReason(null)
         .withReceiptId(7)
         .withReceivedDate(Instant.now())
         .withLastCountDate(Instant.now())
         .withCostPerUnit(new BigDecimal("9.99"))
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();
      LocalDate expDate = LocalDate.of(2027, 6, 15);
      LocalDate mfgDate = LocalDate.of(2026, 3, 1);

      WmsInventory original = new WmsInventory()
         .withId(42)
         .withWarehouseId(1)
         .withClientId(2)
         .withItemId(3)
         .withLocationId(4)
         .withLotNumber("LOT-A")
         .withSerialNumber("SN-A")
         .withExpirationDate(expDate)
         .withManufactureDate(mfgDate)
         .withLpnId(5)
         .withQuantityOnHand(new BigDecimal("50.0"))
         .withQuantityAllocated(new BigDecimal("5.0"))
         .withQuantityAvailable(new BigDecimal("45.0"))
         .withQuantityOnHold(BigDecimal.ZERO)
         .withInventoryStatusId(1)
         .withHoldReason("QC check")
         .withReceiptId(10)
         .withReceivedDate(now)
         .withLastCountDate(now)
         .withCostPerUnit(new BigDecimal("12.50"))
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsInventory restored = new WmsInventory(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getItemId()).isEqualTo(3);
      assertThat(restored.getLocationId()).isEqualTo(4);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-A");
      assertThat(restored.getSerialNumber()).isEqualTo("SN-A");
      assertThat(restored.getExpirationDate()).isEqualTo(expDate);
      assertThat(restored.getManufactureDate()).isEqualTo(mfgDate);
      assertThat(restored.getLpnId()).isEqualTo(5);
      assertThat(restored.getQuantityOnHand()).isEqualByComparingTo(new BigDecimal("50.0"));
      assertThat(restored.getQuantityAllocated()).isEqualByComparingTo(new BigDecimal("5.0"));
      assertThat(restored.getQuantityAvailable()).isEqualByComparingTo(new BigDecimal("45.0"));
      assertThat(restored.getQuantityOnHold()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(restored.getInventoryStatusId()).isEqualTo(1);
      assertThat(restored.getHoldReason()).isEqualTo("QC check");
      assertThat(restored.getReceiptId()).isEqualTo(10);
      assertThat(restored.getReceivedDate()).isEqualTo(now);
      assertThat(restored.getLastCountDate()).isEqualTo(now);
      assertThat(restored.getCostPerUnit()).isEqualByComparingTo(new BigDecimal("12.50"));
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsInventory entity = new WmsInventory(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getLocationId()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getSerialNumber()).isNull();
      assertThat(entity.getExpirationDate()).isNull();
      assertThat(entity.getManufactureDate()).isNull();
      assertThat(entity.getLpnId()).isNull();
      assertThat(entity.getQuantityOnHand()).isNull();
      assertThat(entity.getQuantityAllocated()).isNull();
      assertThat(entity.getQuantityAvailable()).isNull();
      assertThat(entity.getQuantityOnHold()).isNull();
      assertThat(entity.getInventoryStatusId()).isNull();
      assertThat(entity.getHoldReason()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getReceivedDate()).isNull();
      assertThat(entity.getLastCountDate()).isNull();
      assertThat(entity.getCostPerUnit()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer inventoryId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100.0"));
      assertThat(inventoryId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaData_hasSections()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsInventory.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
