/*******************************************************************************
 ** Unit tests for WmsCartonType entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsCartonTypeTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsCartonType.TABLE_NAME).isEqualTo("wmsCartonType");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsCartonType entity = new WmsCartonType();
      WmsCartonType result = entity
         .withId(1)
         .withName("Large Box")
         .withLengthIn(new BigDecimal("24"))
         .withWidthIn(new BigDecimal("18"))
         .withHeightIn(new BigDecimal("12"))
         .withMaxWeightLbs(new BigDecimal("50"))
         .withIsActive(true);

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsCartonType original = new WmsCartonType()
         .withId(42)
         .withName("Medium Box")
         .withIsActive(true);

      QRecord record = original.toQRecord();
      WmsCartonType restored = new WmsCartonType(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getName()).isEqualTo("Medium Box");
      assertThat(restored.getIsActive()).isTrue();
   }



   @Test
   void testInsertViaHelper_defaultCartonType_returnsNonNullId() throws Exception
   {
      Integer cartonTypeId = insertCartonType();
      assertThat(cartonTypeId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsCartonType.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
