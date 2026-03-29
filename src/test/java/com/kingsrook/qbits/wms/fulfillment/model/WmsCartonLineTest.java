/*******************************************************************************
 ** Unit tests for WmsCartonLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsCartonLineTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsCartonLine.TABLE_NAME).isEqualTo("wmsCartonLine");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsCartonLine entity = new WmsCartonLine();
      WmsCartonLine result = entity
         .withId(1)
         .withCartonId(10)
         .withItemId(20)
         .withQuantity(5)
         .withLotNumber("LOT-001");

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsCartonLine original = new WmsCartonLine()
         .withId(42)
         .withCartonId(10)
         .withItemId(20)
         .withQuantity(5)
         .withLotNumber("LOT-ABC");

      QRecord record = original.toQRecord();
      WmsCartonLine restored = new WmsCartonLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getCartonId()).isEqualTo(10);
      assertThat(restored.getItemId()).isEqualTo(20);
      assertThat(restored.getQuantity()).isEqualTo(5);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-ABC");
   }



   @Test
   void testInsertViaHelper_defaultCartonLine_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);
      Integer cartonId = insertCarton(orderId);
      Integer itemId = insertItem();
      Integer lineId = insertCartonLine(cartonId, itemId, 5);
      assertThat(lineId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsCartonLine.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
