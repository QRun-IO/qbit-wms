/*******************************************************************************
 ** Unit tests for WmsAsn entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsAsnTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsAsn.TABLE_NAME).isEqualTo("wmsAsn");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsAsn entity = new WmsAsn();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getAsnNumber()).isNull();
      assertThat(entity.getPurchaseOrderId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsAsn entity = new WmsAsn();
      WmsAsn result = entity
         .withId(1)
         .withPurchaseOrderId(10)
         .withAsnNumber("ASN-100")
         .withCarrierName("UPS")
         .withTrackingNumber("1Z999AA1")
         .withExpectedArrivalDate(Instant.now())
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

      WmsAsn original = new WmsAsn()
         .withId(42)
         .withPurchaseOrderId(10)
         .withAsnNumber("ASN-200")
         .withCarrierName("FedEx")
         .withTrackingNumber("TRK-123")
         .withExpectedArrivalDate(now)
         .withStatusId(2)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsAsn restored = new WmsAsn(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getPurchaseOrderId()).isEqualTo(10);
      assertThat(restored.getAsnNumber()).isEqualTo("ASN-200");
      assertThat(restored.getCarrierName()).isEqualTo("FedEx");
      assertThat(restored.getTrackingNumber()).isEqualTo("TRK-123");
      assertThat(restored.getExpectedArrivalDate()).isEqualTo(now);
      assertThat(restored.getStatusId()).isEqualTo(2);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsAsn entity = new WmsAsn(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getAsnNumber()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultAsn_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId);
      Integer asnId = insertAsn(poId);
      assertThat(asnId).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsAsn.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
