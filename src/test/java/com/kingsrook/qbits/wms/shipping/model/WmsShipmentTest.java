/*******************************************************************************
 ** Unit tests for WmsShipment entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsShipmentTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsShipment.TABLE_NAME).isEqualTo("wmsShipment");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsShipment entity = new WmsShipment();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getShipmentNumber()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsShipment entity = new WmsShipment();
      WmsShipment result = entity
         .withId(1)
         .withWarehouseId(10)
         .withShipmentNumber("SHIP-001")
         .withCarrier("UPS")
         .withTrackingNumber("1Z999AA10123456784")
         .withStatusId(2);

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test QRecord round-trip preserves values.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsShipment original = new WmsShipment()
         .withId(42)
         .withWarehouseId(10)
         .withShipmentNumber("SHIP-001")
         .withCarrier("FEDEX")
         .withServiceLevel("GROUND")
         .withTrackingNumber("TRK123")
         .withStatusId(2);

      QRecord record = original.toQRecord();
      WmsShipment restored = new WmsShipment(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getShipmentNumber()).isEqualTo("SHIP-001");
      assertThat(restored.getCarrier()).isEqualTo("FEDEX");
      assertThat(restored.getTrackingNumber()).isEqualTo("TRK123");
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultShipment_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer shipmentId = insertShipment(warehouseId);
      assertThat(shipmentId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test table metadata has sections.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsShipment.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
