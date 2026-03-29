/*******************************************************************************
 ** Unit tests for WmsOrderLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsOrderLineTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsOrderLine.TABLE_NAME).isEqualTo("wmsOrderLine");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsOrderLine entity = new WmsOrderLine();
      WmsOrderLine result = entity
         .withId(1)
         .withOrderId(10)
         .withItemId(20)
         .withQuantityOrdered(100)
         .withQuantityAllocated(50)
         .withQuantityPicked(25)
         .withQuantityPacked(0)
         .withQuantityShipped(0)
         .withQuantityBackordered(0)
         .withLineNumber(1)
         .withStatusId(2);

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsOrderLine original = new WmsOrderLine()
         .withId(42)
         .withOrderId(10)
         .withItemId(20)
         .withQuantityOrdered(100)
         .withQuantityAllocated(50)
         .withLineNumber(3);

      QRecord record = original.toQRecord();
      WmsOrderLine restored = new WmsOrderLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getOrderId()).isEqualTo(10);
      assertThat(restored.getItemId()).isEqualTo(20);
      assertThat(restored.getQuantityOrdered()).isEqualTo(100);
      assertThat(restored.getQuantityAllocated()).isEqualTo(50);
      assertThat(restored.getLineNumber()).isEqualTo(3);
   }



   @Test
   void testInsertViaHelper_defaultOrderLine_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId);
      assertThat(lineId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsOrderLine.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
