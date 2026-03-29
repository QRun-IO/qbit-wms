/*******************************************************************************
 ** Tests for WmsBillingRateCard entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.BillingRateCardStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsBillingRateCardTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsBillingRateCard.TABLE_NAME).isEqualTo("wmsBillingRateCard");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsBillingRateCard entity = new WmsBillingRateCard();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getEffectiveDate()).isNull();
      assertThat(entity.getExpirationDate()).isNull();
      assertThat(entity.getStatusId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsBillingRateCard entity = new WmsBillingRateCard();
      Instant now = Instant.now();
      WmsBillingRateCard result = entity
         .withId(1)
         .withClientId(2)
         .withName("Standard Rate Card")
         .withEffectiveDate(LocalDate.of(2025, 1, 1))
         .withExpirationDate(LocalDate.of(2025, 12, 31))
         .withStatusId(BillingRateCardStatus.ACTIVE.getPossibleValueId())
         .withCreateDate(now)
         .withModifyDate(now);

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test all fields round-trip through QRecord.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();
      LocalDate effective = LocalDate.of(2025, 1, 1);
      LocalDate expiration = LocalDate.of(2025, 12, 31);

      WmsBillingRateCard original = new WmsBillingRateCard()
         .withId(42)
         .withClientId(3)
         .withName("Premium Rate Card")
         .withEffectiveDate(effective)
         .withExpirationDate(expiration)
         .withStatusId(BillingRateCardStatus.ACTIVE.getPossibleValueId())
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsBillingRateCard restored = new WmsBillingRateCard(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getClientId()).isEqualTo(3);
      assertThat(restored.getName()).isEqualTo("Premium Rate Card");
      assertThat(restored.getEffectiveDate()).isEqualTo(effective);
      assertThat(restored.getExpirationDate()).isEqualTo(expiration);
      assertThat(restored.getStatusId()).isEqualTo(BillingRateCardStatus.ACTIVE.getPossibleValueId());
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsBillingRateCard entity = new WmsBillingRateCard(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getEffectiveDate()).isNull();
      assertThat(entity.getExpirationDate()).isNull();
      assertThat(entity.getStatusId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test insert and retrieve via BaseTest helper.
    *******************************************************************************/
   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer rateCardId = insertBillingRateCard(clientId, "Test Rate Card");

      QRecord record = new GetAction().execute(new GetInput(WmsBillingRateCard.TABLE_NAME).withPrimaryKey(rateCardId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueString("name")).isEqualTo("Test Rate Card");
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsBillingRateCard entity = new WmsBillingRateCard();
      Instant now = Instant.now();
      LocalDate effective = LocalDate.of(2025, 6, 1);
      LocalDate expiration = LocalDate.of(2026, 5, 31);

      entity.setId(10);
      entity.setClientId(20);
      entity.setName("Setter Test");
      entity.setEffectiveDate(effective);
      entity.setExpirationDate(expiration);
      entity.setStatusId(1);
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(10);
      assertThat(entity.getClientId()).isEqualTo(20);
      assertThat(entity.getName()).isEqualTo("Setter Test");
      assertThat(entity.getEffectiveDate()).isEqualTo(effective);
      assertThat(entity.getExpirationDate()).isEqualTo(expiration);
      assertThat(entity.getStatusId()).isEqualTo(1);
      assertThat(entity.getCreateDate()).isEqualTo(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var qInstance = com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
      var table = qInstance.getTable(WmsBillingRateCard.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
