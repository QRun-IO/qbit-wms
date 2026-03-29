/*******************************************************************************
 ** Unit tests for WmsCarton entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsCartonTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsCarton.TABLE_NAME).isEqualTo("wmsCarton");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsCarton entity = new WmsCarton();
      WmsCarton result = entity
         .withId(1)
         .withOrderId(10)
         .withCartonNumber("CTN-001")
         .withStatusId(1)
         .withWeightLbs(new BigDecimal("5.5"))
         .withTrackingNumber("1Z999AA10123456784");

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsCarton original = new WmsCarton()
         .withId(42)
         .withOrderId(10)
         .withCartonNumber("CTN-200")
         .withStatusId(2)
         .withTrackingNumber("TRACK123");

      QRecord record = original.toQRecord();
      WmsCarton restored = new WmsCarton(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getOrderId()).isEqualTo(10);
      assertThat(restored.getCartonNumber()).isEqualTo("CTN-200");
      assertThat(restored.getStatusId()).isEqualTo(2);
   }



   @Test
   void testInsertViaHelper_defaultCarton_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);
      Integer cartonId = insertCarton(orderId);
      assertThat(cartonId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsCarton.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
