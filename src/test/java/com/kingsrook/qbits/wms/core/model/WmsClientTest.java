/*******************************************************************************
 ** Unit tests for WmsClient entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsClientTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsClient.TABLE_NAME).isEqualTo("wmsClient");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsClient entity = new WmsClient();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsClient entity = new WmsClient();
      WmsClient result = entity
         .withId(1)
         .withName("Acme Corp")
         .withCode("ACME")
         .withContactName("John Doe")
         .withContactEmail("john@acme.com")
         .withContactPhone("555-0100")
         .withBillingEmail("billing@acme.com")
         .withDefaultCarrier("UPS")
         .withDefaultServiceLevel("Ground")
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsClient original = new WmsClient()
         .withId(10)
         .withName("Test Client")
         .withCode("TC01")
         .withContactName("Jane Smith")
         .withContactEmail("jane@test.com")
         .withContactPhone("555-0200")
         .withBillingEmail("billing@test.com")
         .withDefaultCarrier("FedEx")
         .withDefaultServiceLevel("Express")
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsClient restored = new WmsClient(record);

      assertThat(restored.getId()).isEqualTo(10);
      assertThat(restored.getName()).isEqualTo("Test Client");
      assertThat(restored.getCode()).isEqualTo("TC01");
      assertThat(restored.getContactName()).isEqualTo("Jane Smith");
      assertThat(restored.getContactEmail()).isEqualTo("jane@test.com");
      assertThat(restored.getContactPhone()).isEqualTo("555-0200");
      assertThat(restored.getBillingEmail()).isEqualTo("billing@test.com");
      assertThat(restored.getDefaultCarrier()).isEqualTo("FedEx");
      assertThat(restored.getDefaultServiceLevel()).isEqualTo("Express");
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsClient entity = new WmsClient(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
      assertThat(entity.getContactName()).isNull();
      assertThat(entity.getContactEmail()).isNull();
      assertThat(entity.getContactPhone()).isNull();
      assertThat(entity.getBillingEmail()).isNull();
      assertThat(entity.getDefaultCarrier()).isNull();
      assertThat(entity.getDefaultServiceLevel()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsClient.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
