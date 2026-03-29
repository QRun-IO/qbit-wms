/*******************************************************************************
 ** Unit tests for WmsDockAppointment entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsDockAppointmentTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsDockAppointment.TABLE_NAME).isEqualTo("wmsDockAppointment");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsDockAppointment entity = new WmsDockAppointment()
         .withId(1)
         .withWarehouseId(10)
         .withCarrierName("UPS")
         .withStatusId(1);

      assertThat(entity.getCarrierName()).isEqualTo("UPS");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsDockAppointment original = new WmsDockAppointment()
         .withId(42)
         .withWarehouseId(10)
         .withCarrierName("FEDEX")
         .withReferenceType("SHIPMENT")
         .withReferenceId(100);

      QRecord record = original.toQRecord();
      WmsDockAppointment restored = new WmsDockAppointment(record);

      assertThat(restored.getCarrierName()).isEqualTo("FEDEX");
      assertThat(restored.getReferenceType()).isEqualTo("SHIPMENT");
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer apptId = insertDockAppointment(warehouseId);
      assertThat(apptId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsDockAppointment.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
