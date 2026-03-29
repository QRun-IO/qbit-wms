/*******************************************************************************
 ** Unit tests for WmsPurchaseOrderLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsPurchaseOrderLineTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsPurchaseOrderLine.TABLE_NAME).isEqualTo("wmsPurchaseOrderLine");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsPurchaseOrderLine entity = new WmsPurchaseOrderLine();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getPurchaseOrderId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsPurchaseOrderLine entity = new WmsPurchaseOrderLine();
      WmsPurchaseOrderLine result = entity
         .withId(1)
         .withPurchaseOrderId(10)
         .withItemId(20)
         .withExpectedQuantity(100)
         .withReceivedQuantity(50)
         .withUomId(3)
         .withLineNumber(1)
         .withStatusId(2)
         .withOverReceiveTolerancePct(new BigDecimal("10.0"))
         .withUnderReceiveTolerancePct(new BigDecimal("5.0"))
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

      WmsPurchaseOrderLine original = new WmsPurchaseOrderLine()
         .withId(42)
         .withPurchaseOrderId(10)
         .withItemId(20)
         .withExpectedQuantity(100)
         .withReceivedQuantity(50)
         .withUomId(3)
         .withLineNumber(1)
         .withStatusId(2)
         .withOverReceiveTolerancePct(new BigDecimal("10.0"))
         .withUnderReceiveTolerancePct(new BigDecimal("5.0"))
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsPurchaseOrderLine restored = new WmsPurchaseOrderLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getPurchaseOrderId()).isEqualTo(10);
      assertThat(restored.getItemId()).isEqualTo(20);
      assertThat(restored.getExpectedQuantity()).isEqualTo(100);
      assertThat(restored.getReceivedQuantity()).isEqualTo(50);
      assertThat(restored.getLineNumber()).isEqualTo(1);
      assertThat(restored.getStatusId()).isEqualTo(2);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsPurchaseOrderLine entity = new WmsPurchaseOrderLine(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getPurchaseOrderId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultLine_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId);
      Integer itemId = insertItem();
      Integer lineId = insertPurchaseOrderLine(poId, itemId);
      assertThat(lineId).isNotNull().isPositive();
   }
}
