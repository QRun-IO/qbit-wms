/*******************************************************************************
 ** Unit tests for WmsAsnLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsAsnLineTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsAsnLine.TABLE_NAME).isEqualTo("wmsAsnLine");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsAsnLine entity = new WmsAsnLine();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getAsnId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsAsnLine entity = new WmsAsnLine();
      WmsAsnLine result = entity
         .withId(1)
         .withAsnId(10)
         .withItemId(20)
         .withExpectedQuantity(100)
         .withUomId(3)
         .withLotNumber("LOT-001")
         .withExpirationDate(LocalDate.now())
         .withLpnBarcode("LPN-001")
         .withLineNumber(1)
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
      LocalDate today = LocalDate.now();

      WmsAsnLine original = new WmsAsnLine()
         .withId(42)
         .withAsnId(10)
         .withItemId(20)
         .withExpectedQuantity(100)
         .withUomId(3)
         .withLotNumber("LOT-001")
         .withExpirationDate(today)
         .withLpnBarcode("LPN-001")
         .withLineNumber(1)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsAsnLine restored = new WmsAsnLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getAsnId()).isEqualTo(10);
      assertThat(restored.getItemId()).isEqualTo(20);
      assertThat(restored.getExpectedQuantity()).isEqualTo(100);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-001");
      assertThat(restored.getExpirationDate()).isEqualTo(today);
      assertThat(restored.getLpnBarcode()).isEqualTo("LPN-001");
      assertThat(restored.getLineNumber()).isEqualTo(1);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsAsnLine entity = new WmsAsnLine(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getAsnId()).isNull();
   }
}
