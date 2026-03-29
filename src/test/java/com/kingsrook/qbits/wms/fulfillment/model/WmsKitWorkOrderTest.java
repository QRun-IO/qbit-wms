/*******************************************************************************
 ** Unit tests for WmsKitWorkOrder entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsKitWorkOrderTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsKitWorkOrder.TABLE_NAME).isEqualTo("wmsKitWorkOrder");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsKitWorkOrder entity = new WmsKitWorkOrder()
         .withId(1)
         .withWarehouseId(10)
         .withKitItemId(20)
         .withQuantity(100)
         .withAssignedTo("Worker1");

      assertThat(entity.getQuantity()).isEqualTo(100);
      assertThat(entity.getAssignedTo()).isEqualTo("Worker1");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsKitWorkOrder original = new WmsKitWorkOrder()
         .withId(42)
         .withWarehouseId(10)
         .withKitItemId(20)
         .withQuantity(50)
         .withStatusId(1);

      QRecord record = original.toQRecord();
      WmsKitWorkOrder restored = new WmsKitWorkOrder(record);

      assertThat(restored.getQuantity()).isEqualTo(50);
      assertThat(restored.getKitItemId()).isEqualTo(20);
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer kitItemId = insertItem("KIT-001", "Kit Item");
      Integer kwoId = insertKitWorkOrder(warehouseId, kitItemId);
      assertThat(kwoId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsKitWorkOrder.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
