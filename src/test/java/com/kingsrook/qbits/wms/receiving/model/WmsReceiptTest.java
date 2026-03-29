/*******************************************************************************
 ** Unit tests for WmsReceipt entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReceiptTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReceipt.TABLE_NAME).isEqualTo("wmsReceipt");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsReceipt entity = new WmsReceipt();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getReceiptNumber()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReceipt entity = new WmsReceipt();
      WmsReceipt result = entity
         .withId(1)
         .withWarehouseId(10)
         .withClientId(20)
         .withPurchaseOrderId(30)
         .withReceiptNumber("RCV-001")
         .withReceiptTypeId(1)
         .withStatusId(1)
         .withReceivedBy("testuser")
         .withReceivedDate(Instant.now())
         .withCarrierName("UPS")
         .withTrailerNumber("T123")
         .withSealNumber("S456")
         .withNotes("Test receipt")
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

      WmsReceipt original = new WmsReceipt()
         .withId(42)
         .withWarehouseId(10)
         .withClientId(20)
         .withPurchaseOrderId(30)
         .withReceiptNumber("RCV-100")
         .withReceiptTypeId(1)
         .withStatusId(2)
         .withReceivedBy("testuser")
         .withReceivedDate(now)
         .withCarrierName("FedEx")
         .withTrailerNumber("T789")
         .withSealNumber("S012")
         .withNotes("Round trip")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsReceipt restored = new WmsReceipt(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getClientId()).isEqualTo(20);
      assertThat(restored.getPurchaseOrderId()).isEqualTo(30);
      assertThat(restored.getReceiptNumber()).isEqualTo("RCV-100");
      assertThat(restored.getReceiptTypeId()).isEqualTo(1);
      assertThat(restored.getStatusId()).isEqualTo(2);
      assertThat(restored.getReceivedBy()).isEqualTo("testuser");
      assertThat(restored.getCarrierName()).isEqualTo("FedEx");
      assertThat(restored.getTrailerNumber()).isEqualTo("T789");
      assertThat(restored.getSealNumber()).isEqualTo("S012");
      assertThat(restored.getNotes()).isEqualTo("Round trip");
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsReceipt entity = new WmsReceipt(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getReceiptNumber()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultReceipt_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer receiptId = insertReceipt(warehouseId);
      assertThat(receiptId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsReceipt.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
