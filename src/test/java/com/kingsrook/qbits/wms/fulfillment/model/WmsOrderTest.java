/*******************************************************************************
 ** Unit tests for WmsOrder entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsOrderTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsOrder.TABLE_NAME).isEqualTo("wmsOrder");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsOrder entity = new WmsOrder();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getOrderNumber()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsOrder entity = new WmsOrder();
      WmsOrder result = entity
         .withId(1)
         .withWarehouseId(10)
         .withClientId(20)
         .withOrderNumber("ORD-001")
         .withStatusId(2)
         .withPriority(5)
         .withCarrier("FEDEX")
         .withServiceLevel("GROUND")
         .withShipToName("John Doe")
         .withOrderValue(new BigDecimal("99.99"))
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

      WmsOrder original = new WmsOrder()
         .withId(42)
         .withWarehouseId(10)
         .withClientId(20)
         .withOrderNumber("ORD-200")
         .withStatusId(2)
         .withPriority(3)
         .withCarrier("UPS")
         .withServiceLevel("EXPRESS")
         .withShipToName("Jane Doe")
         .withShipToCity("Dallas")
         .withShipToState("TX")
         .withShipToPostalCode("75001")
         .withShipToCountry("US")
         .withTotalLineCount(5)
         .withTotalUnitCount(50)
         .withOrderValue(new BigDecimal("199.99"))
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsOrder restored = new WmsOrder(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getClientId()).isEqualTo(20);
      assertThat(restored.getOrderNumber()).isEqualTo("ORD-200");
      assertThat(restored.getStatusId()).isEqualTo(2);
      assertThat(restored.getCarrier()).isEqualTo("UPS");
      assertThat(restored.getShipToName()).isEqualTo("Jane Doe");
      assertThat(restored.getTotalLineCount()).isEqualTo(5);
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultOrder_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);
      assertThat(orderId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsOrder.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
