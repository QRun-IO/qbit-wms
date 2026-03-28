/*******************************************************************************
 ** Unit tests for WmsVendor entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsVendorTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsVendor.TABLE_NAME).isEqualTo("wmsVendor");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsVendor entity = new WmsVendor();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsVendor entity = new WmsVendor();
      WmsVendor result = entity
         .withId(1)
         .withCode("V01")
         .withName("Vendor One")
         .withContactName("Bob")
         .withContactEmail("bob@vendor.com")
         .withContactPhone("555-0300")
         .withDefaultLeadTimeDays(14)
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsVendor original = new WmsVendor()
         .withId(5)
         .withCode("VND")
         .withName("Test Vendor")
         .withContactName("Alice")
         .withContactEmail("alice@vendor.com")
         .withContactPhone("555-0400")
         .withDefaultLeadTimeDays(7)
         .withIsActive(false)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsVendor restored = new WmsVendor(record);

      assertThat(restored.getId()).isEqualTo(5);
      assertThat(restored.getCode()).isEqualTo("VND");
      assertThat(restored.getName()).isEqualTo("Test Vendor");
      assertThat(restored.getContactName()).isEqualTo("Alice");
      assertThat(restored.getContactEmail()).isEqualTo("alice@vendor.com");
      assertThat(restored.getContactPhone()).isEqualTo("555-0400");
      assertThat(restored.getDefaultLeadTimeDays()).isEqualTo(7);
      assertThat(restored.getIsActive()).isFalse();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsVendor entity = new WmsVendor(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getCode()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getContactName()).isNull();
      assertThat(entity.getContactEmail()).isNull();
      assertThat(entity.getContactPhone()).isNull();
      assertThat(entity.getDefaultLeadTimeDays()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsVendor.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
