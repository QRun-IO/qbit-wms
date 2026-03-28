/*******************************************************************************
 ** Unit tests for WmsZone entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsZoneTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsZone.TABLE_NAME).isEqualTo("wmsZone");
   }



   /*******************************************************************************
    ** Test default constructor.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsZone entity = new WmsZone();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsZone entity = new WmsZone();
      WmsZone result = entity
         .withId(1)
         .withWarehouseId(10)
         .withName("Zone A")
         .withCode("ZA")
         .withZoneTypeId(1)
         .withPickSequence(5)
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test all fields round-trip through QRecord.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsZone original = new WmsZone()
         .withId(7)
         .withWarehouseId(3)
         .withName("Bulk Zone")
         .withCode("BZ01")
         .withZoneTypeId(1)
         .withPickSequence(10)
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsZone restored = new WmsZone(record);

      assertThat(restored.getId()).isEqualTo(7);
      assertThat(restored.getWarehouseId()).isEqualTo(3);
      assertThat(restored.getName()).isEqualTo("Bulk Zone");
      assertThat(restored.getCode()).isEqualTo("BZ01");
      assertThat(restored.getZoneTypeId()).isEqualTo(1);
      assertThat(restored.getPickSequence()).isEqualTo(10);
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsZone entity = new WmsZone(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
      assertThat(entity.getZoneTypeId()).isNull();
      assertThat(entity.getPickSequence()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultZone_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId);
      assertThat(zoneId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test insert with custom values.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_customZone_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId, "Cold Zone", "CZ01");
      assertThat(zoneId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test table metadata has sections.
    *******************************************************************************/
   @Test
   void testTableMetaData_hasSections() throws Exception
   {
      var qInstance = com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
      var table = qInstance.getTable(WmsZone.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
