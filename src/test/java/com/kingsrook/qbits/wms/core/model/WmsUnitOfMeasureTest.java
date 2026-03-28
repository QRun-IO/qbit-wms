/*******************************************************************************
 ** Unit tests for WmsUnitOfMeasure entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsUnitOfMeasureTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsUnitOfMeasure.TABLE_NAME).isEqualTo("wmsUnitOfMeasure");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsUnitOfMeasure entity = new WmsUnitOfMeasure();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsUnitOfMeasure entity = new WmsUnitOfMeasure();
      WmsUnitOfMeasure result = entity
         .withId(1)
         .withItemId(10)
         .withUomType("CASE")
         .withLabel("Case of 24")
         .withQuantityOfBase(24)
         .withBarcode("UOM-CASE-001")
         .withWeightLbs(new BigDecimal("30.0"))
         .withLengthIn(new BigDecimal("20.0"))
         .withWidthIn(new BigDecimal("15.0"))
         .withHeightIn(new BigDecimal("12.0"))
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsUnitOfMeasure original = new WmsUnitOfMeasure()
         .withId(5)
         .withItemId(20)
         .withUomType("PALLET")
         .withLabel("Full Pallet")
         .withQuantityOfBase(960)
         .withBarcode("UOM-PLT-001")
         .withWeightLbs(new BigDecimal("500.0"))
         .withLengthIn(new BigDecimal("48.0"))
         .withWidthIn(new BigDecimal("40.0"))
         .withHeightIn(new BigDecimal("60.0"))
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsUnitOfMeasure restored = new WmsUnitOfMeasure(record);

      assertThat(restored.getId()).isEqualTo(5);
      assertThat(restored.getItemId()).isEqualTo(20);
      assertThat(restored.getUomType()).isEqualTo("PALLET");
      assertThat(restored.getLabel()).isEqualTo("Full Pallet");
      assertThat(restored.getQuantityOfBase()).isEqualTo(960);
      assertThat(restored.getBarcode()).isEqualTo("UOM-PLT-001");
      assertThat(restored.getWeightLbs()).isEqualByComparingTo(new BigDecimal("500.0"));
      assertThat(restored.getLengthIn()).isEqualByComparingTo(new BigDecimal("48.0"));
      assertThat(restored.getWidthIn()).isEqualByComparingTo(new BigDecimal("40.0"));
      assertThat(restored.getHeightIn()).isEqualByComparingTo(new BigDecimal("60.0"));
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsUnitOfMeasure entity = new WmsUnitOfMeasure(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getUomType()).isNull();
      assertThat(entity.getLabel()).isNull();
      assertThat(entity.getQuantityOfBase()).isNull();
      assertThat(entity.getBarcode()).isNull();
      assertThat(entity.getWeightLbs()).isNull();
      assertThat(entity.getLengthIn()).isNull();
      assertThat(entity.getWidthIn()).isNull();
      assertThat(entity.getHeightIn()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsUnitOfMeasure.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
