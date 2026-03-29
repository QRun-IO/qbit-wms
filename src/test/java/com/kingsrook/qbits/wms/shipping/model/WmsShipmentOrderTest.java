/*******************************************************************************
 ** Unit tests for WmsShipmentOrder entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsShipmentOrderTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsShipmentOrder.TABLE_NAME).isEqualTo("wmsShipmentOrder");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsShipmentOrder entity = new WmsShipmentOrder()
         .withId(1)
         .withShipmentId(10)
         .withOrderId(20);

      assertThat(entity.getShipmentId()).isEqualTo(10);
      assertThat(entity.getOrderId()).isEqualTo(20);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsShipmentOrder original = new WmsShipmentOrder()
         .withId(42)
         .withShipmentId(10)
         .withOrderId(20);

      QRecord record = original.toQRecord();
      WmsShipmentOrder restored = new WmsShipmentOrder(record);

      assertThat(restored.getShipmentId()).isEqualTo(10);
      assertThat(restored.getOrderId()).isEqualTo(20);
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsShipmentOrder.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
