/*******************************************************************************
 ** Unit tests for WmsReceiptLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReceiptLineTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReceiptLine.TABLE_NAME).isEqualTo("wmsReceiptLine");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsReceiptLine entity = new WmsReceiptLine();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReceiptLine entity = new WmsReceiptLine();
      WmsReceiptLine result = entity
         .withId(1)
         .withReceiptId(10)
         .withPurchaseOrderLineId(20)
         .withItemId(30)
         .withQuantityReceived(100)
         .withQuantityDamaged(5)
         .withQuantityRejected(2)
         .withUomId(3)
         .withLotNumber("LOT-001")
         .withSerialNumbers("SN1,SN2")
         .withExpirationDate(LocalDate.now())
         .withManufactureDate(LocalDate.now())
         .withConditionId(1)
         .withLpnId(50)
         .withQcStatusId(4)
         .withStatusId(1)
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
      LocalDate today = LocalDate.now();

      WmsReceiptLine original = new WmsReceiptLine()
         .withId(42)
         .withReceiptId(10)
         .withPurchaseOrderLineId(20)
         .withItemId(30)
         .withQuantityReceived(100)
         .withQuantityDamaged(5)
         .withQuantityRejected(2)
         .withLotNumber("LOT-001")
         .withSerialNumbers("SN1,SN2")
         .withExpirationDate(today)
         .withManufactureDate(today)
         .withConditionId(1)
         .withQcStatusId(4)
         .withStatusId(1)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsReceiptLine restored = new WmsReceiptLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getReceiptId()).isEqualTo(10);
      assertThat(restored.getPurchaseOrderLineId()).isEqualTo(20);
      assertThat(restored.getItemId()).isEqualTo(30);
      assertThat(restored.getQuantityReceived()).isEqualTo(100);
      assertThat(restored.getQuantityDamaged()).isEqualTo(5);
      assertThat(restored.getQuantityRejected()).isEqualTo(2);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-001");
      assertThat(restored.getSerialNumbers()).isEqualTo("SN1,SN2");
      assertThat(restored.getExpirationDate()).isEqualTo(today);
      assertThat(restored.getManufactureDate()).isEqualTo(today);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsReceiptLine entity = new WmsReceiptLine(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultReceiptLine_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer receiptId = insertReceipt(warehouseId);
      Integer itemId = insertItem();
      Integer lineId = insertReceiptLine(receiptId, itemId);
      assertThat(lineId).isNotNull().isPositive();
   }
}
