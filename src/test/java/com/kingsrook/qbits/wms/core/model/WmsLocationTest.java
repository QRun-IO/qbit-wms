/*******************************************************************************
 ** Unit tests for WmsLocation entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsLocationTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsLocation.TABLE_NAME).isEqualTo("wmsLocation");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsLocation entity = new WmsLocation();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getBarcode()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsLocation entity = new WmsLocation();
      WmsLocation result = entity
         .withId(1)
         .withWarehouseId(2)
         .withZoneId(3)
         .withBarcode("A-01-01")
         .withLabel("Aisle A, Rack 01, Shelf 01")
         .withAisle("A")
         .withRack("01")
         .withShelf("01")
         .withPosition("L")
         .withLocationTypeId(1)
         .withMaxWeightLbs(new BigDecimal("500.0"))
         .withMaxVolumeCubicFt(new BigDecimal("10.0"))
         .withCurrentWeightLbs(new BigDecimal("100.0"))
         .withCurrentVolumeCubicFt(new BigDecimal("2.0"))
         .withIsMixedSkuAllowed(true)
         .withPickSequence(42)
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsLocation original = new WmsLocation()
         .withId(99)
         .withWarehouseId(2)
         .withZoneId(3)
         .withBarcode("B-02-03")
         .withLabel("Location B-02-03")
         .withAisle("B")
         .withRack("02")
         .withShelf("03")
         .withPosition("R")
         .withLocationTypeId(2)
         .withMaxWeightLbs(new BigDecimal("1000.0"))
         .withMaxVolumeCubicFt(new BigDecimal("20.0"))
         .withCurrentWeightLbs(new BigDecimal("250.0"))
         .withCurrentVolumeCubicFt(new BigDecimal("5.0"))
         .withIsMixedSkuAllowed(false)
         .withPickSequence(15)
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsLocation restored = new WmsLocation(record);

      assertThat(restored.getId()).isEqualTo(99);
      assertThat(restored.getWarehouseId()).isEqualTo(2);
      assertThat(restored.getZoneId()).isEqualTo(3);
      assertThat(restored.getBarcode()).isEqualTo("B-02-03");
      assertThat(restored.getLabel()).isEqualTo("Location B-02-03");
      assertThat(restored.getAisle()).isEqualTo("B");
      assertThat(restored.getRack()).isEqualTo("02");
      assertThat(restored.getShelf()).isEqualTo("03");
      assertThat(restored.getPosition()).isEqualTo("R");
      assertThat(restored.getLocationTypeId()).isEqualTo(2);
      assertThat(restored.getMaxWeightLbs()).isEqualByComparingTo(new BigDecimal("1000.0"));
      assertThat(restored.getMaxVolumeCubicFt()).isEqualByComparingTo(new BigDecimal("20.0"));
      assertThat(restored.getCurrentWeightLbs()).isEqualByComparingTo(new BigDecimal("250.0"));
      assertThat(restored.getCurrentVolumeCubicFt()).isEqualByComparingTo(new BigDecimal("5.0"));
      assertThat(restored.getIsMixedSkuAllowed()).isFalse();
      assertThat(restored.getPickSequence()).isEqualTo(15);
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsLocation entity = new WmsLocation(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getZoneId()).isNull();
      assertThat(entity.getBarcode()).isNull();
      assertThat(entity.getLabel()).isNull();
      assertThat(entity.getAisle()).isNull();
      assertThat(entity.getRack()).isNull();
      assertThat(entity.getShelf()).isNull();
      assertThat(entity.getPosition()).isNull();
      assertThat(entity.getLocationTypeId()).isNull();
      assertThat(entity.getMaxWeightLbs()).isNull();
      assertThat(entity.getMaxVolumeCubicFt()).isNull();
      assertThat(entity.getCurrentWeightLbs()).isNull();
      assertThat(entity.getCurrentVolumeCubicFt()).isNull();
      assertThat(entity.getIsMixedSkuAllowed()).isNull();
      assertThat(entity.getPickSequence()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_defaultLocation_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);
      assertThat(locationId).isNotNull().isPositive();
   }



   @Test
   void testInsertViaHelper_customLocation_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId);
      Integer locationId = insertLocation(warehouseId, zoneId, "LOC-CUSTOM-001");
      assertThat(locationId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaData_hasSections()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsLocation.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getIcon()).isNotNull();
   }
}
