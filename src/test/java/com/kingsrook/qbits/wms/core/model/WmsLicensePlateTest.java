/*******************************************************************************
 ** Unit tests for WmsLicensePlate entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsLicensePlateTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsLicensePlate.TABLE_NAME).isEqualTo("wmsLicensePlate");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsLicensePlate entity = new WmsLicensePlate();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getLpnBarcode()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsLicensePlate entity = new WmsLicensePlate();
      WmsLicensePlate result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withLpnBarcode("LPN-001")
         .withStatusId(1)
         .withLocationId(5)
         .withReceiptId(10)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsLicensePlate original = new WmsLicensePlate()
         .withId(7)
         .withWarehouseId(1)
         .withClientId(2)
         .withLpnBarcode("LPN-TEST-001")
         .withStatusId(1)
         .withLocationId(3)
         .withReceiptId(4)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsLicensePlate restored = new WmsLicensePlate(record);

      assertThat(restored.getId()).isEqualTo(7);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getLpnBarcode()).isEqualTo("LPN-TEST-001");
      assertThat(restored.getStatusId()).isEqualTo(1);
      assertThat(restored.getLocationId()).isEqualTo(3);
      assertThat(restored.getReceiptId()).isEqualTo(4);
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsLicensePlate entity = new WmsLicensePlate(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getLpnBarcode()).isNull();
      assertThat(entity.getStatusId()).isNull();
      assertThat(entity.getLocationId()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer lpnId = insertLicensePlate(warehouseId, "LPN-HELPER-001");
      assertThat(lpnId).isNotNull().isPositive();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsLicensePlate.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
