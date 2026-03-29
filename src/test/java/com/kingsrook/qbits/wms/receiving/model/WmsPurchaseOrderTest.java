/*******************************************************************************
 ** Unit tests for WmsPurchaseOrder entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsPurchaseOrderTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsPurchaseOrder.TABLE_NAME).isEqualTo("wmsPurchaseOrder");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsPurchaseOrder entity = new WmsPurchaseOrder();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getPoNumber()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsPurchaseOrder entity = new WmsPurchaseOrder();
      WmsPurchaseOrder result = entity
         .withId(1)
         .withWarehouseId(10)
         .withClientId(20)
         .withVendorId(30)
         .withPoNumber("PO-100")
         .withStatusId(2)
         .withExpectedDeliveryDate(LocalDate.now())
         .withActualDeliveryDate(LocalDate.now())
         .withDockDoorId(5)
         .withNotes("Test notes")
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

      WmsPurchaseOrder original = new WmsPurchaseOrder()
         .withId(42)
         .withWarehouseId(10)
         .withClientId(20)
         .withVendorId(30)
         .withPoNumber("PO-200")
         .withStatusId(2)
         .withExpectedDeliveryDate(today)
         .withActualDeliveryDate(today)
         .withDockDoorId(5)
         .withNotes("Round trip test")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsPurchaseOrder restored = new WmsPurchaseOrder(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getClientId()).isEqualTo(20);
      assertThat(restored.getVendorId()).isEqualTo(30);
      assertThat(restored.getPoNumber()).isEqualTo("PO-200");
      assertThat(restored.getStatusId()).isEqualTo(2);
      assertThat(restored.getExpectedDeliveryDate()).isEqualTo(today);
      assertThat(restored.getActualDeliveryDate()).isEqualTo(today);
      assertThat(restored.getDockDoorId()).isEqualTo(5);
      assertThat(restored.getNotes()).isEqualTo("Round trip test");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsPurchaseOrder entity = new WmsPurchaseOrder(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getPoNumber()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getVendorId()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultPO_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId);
      assertThat(poId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsPurchaseOrder.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
