/*******************************************************************************
 ** Unit tests for WmsCycleCountLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsCycleCountLineTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsCycleCountLine.TABLE_NAME).isEqualTo("wmsCycleCountLine");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsCycleCountLine entity = new WmsCycleCountLine();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getCycleCountId()).isNull();
      assertThat(entity.getLocationId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsCycleCountLine entity = new WmsCycleCountLine();
      WmsCycleCountLine result = entity
         .withId(1)
         .withCycleCountId(2)
         .withLocationId(3)
         .withItemId(4)
         .withLotNumber("LOT-CC")
         .withExpectedQuantity(new BigDecimal("100.0"))
         .withCountedQuantity(new BigDecimal("98.0"))
         .withVariance(new BigDecimal("-2.0"))
         .withVarianceApproved(true)
         .withApprovedBy("supervisor")
         .withStatus("COUNTED")
         .withTaskId(10)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsCycleCountLine original = new WmsCycleCountLine()
         .withId(7)
         .withCycleCountId(1)
         .withLocationId(2)
         .withItemId(3)
         .withLotNumber("LOT-RT")
         .withExpectedQuantity(new BigDecimal("50.0"))
         .withCountedQuantity(new BigDecimal("52.0"))
         .withVariance(new BigDecimal("2.0"))
         .withVarianceApproved(false)
         .withApprovedBy("mgr")
         .withStatus("PENDING")
         .withTaskId(20)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsCycleCountLine restored = new WmsCycleCountLine(record);

      assertThat(restored.getId()).isEqualTo(7);
      assertThat(restored.getCycleCountId()).isEqualTo(1);
      assertThat(restored.getLocationId()).isEqualTo(2);
      assertThat(restored.getItemId()).isEqualTo(3);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-RT");
      assertThat(restored.getExpectedQuantity()).isEqualByComparingTo(new BigDecimal("50.0"));
      assertThat(restored.getCountedQuantity()).isEqualByComparingTo(new BigDecimal("52.0"));
      assertThat(restored.getVariance()).isEqualByComparingTo(new BigDecimal("2.0"));
      assertThat(restored.getVarianceApproved()).isFalse();
      assertThat(restored.getApprovedBy()).isEqualTo("mgr");
      assertThat(restored.getStatus()).isEqualTo("PENDING");
      assertThat(restored.getTaskId()).isEqualTo(20);
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsCycleCountLine entity = new WmsCycleCountLine(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getCycleCountId()).isNull();
      assertThat(entity.getLocationId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getExpectedQuantity()).isNull();
      assertThat(entity.getCountedQuantity()).isNull();
      assertThat(entity.getVariance()).isNull();
      assertThat(entity.getVarianceApproved()).isNull();
      assertThat(entity.getApprovedBy()).isNull();
      assertThat(entity.getStatus()).isNull();
      assertThat(entity.getTaskId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsCycleCountLine.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
