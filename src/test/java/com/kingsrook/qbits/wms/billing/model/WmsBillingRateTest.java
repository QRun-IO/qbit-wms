/*******************************************************************************
 ** Tests for WmsBillingRate entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsBillingRateTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsBillingRate.TABLE_NAME).isEqualTo("wmsBillingRate");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsBillingRate entity = new WmsBillingRate();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getRateCardId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getRate()).isNull();
      assertThat(entity.getMinimumCharge()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsBillingRate entity = new WmsBillingRate();
      Instant now = Instant.now();
      WmsBillingRate result = entity
         .withId(1)
         .withRateCardId(2)
         .withActivityTypeId(3)
         .withRate(new BigDecimal("1.50"))
         .withMinimumCharge(new BigDecimal("10.00"))
         .withNotes("Test notes")
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

      WmsBillingRate original = new WmsBillingRate()
         .withId(42)
         .withRateCardId(5)
         .withActivityTypeId(BillingActivityType.PICK_PER_UNIT.getPossibleValueId())
         .withRate(new BigDecimal("2.75"))
         .withMinimumCharge(new BigDecimal("25.00"))
         .withNotes("Per unit pick fee")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsBillingRate restored = new WmsBillingRate(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getRateCardId()).isEqualTo(5);
      assertThat(restored.getActivityTypeId()).isEqualTo(BillingActivityType.PICK_PER_UNIT.getPossibleValueId());
      assertThat(restored.getRate().compareTo(new BigDecimal("2.75"))).isEqualTo(0);
      assertThat(restored.getMinimumCharge().compareTo(new BigDecimal("25.00"))).isEqualTo(0);
      assertThat(restored.getNotes()).isEqualTo("Per unit pick fee");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsBillingRate entity = new WmsBillingRate(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getRateCardId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getRate()).isNull();
      assertThat(entity.getMinimumCharge()).isNull();
      assertThat(entity.getNotes()).isNull();
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
      Integer rateCardId = insertBillingRateCard(clientId);
      Integer rateId = insertBillingRate(rateCardId, BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("1.50"));

      QRecord record = new GetAction().execute(new GetInput(WmsBillingRate.TABLE_NAME).withPrimaryKey(rateId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("rateCardId")).isEqualTo(rateCardId);
      assertThat(record.getValueBigDecimal("rate").compareTo(new BigDecimal("1.50"))).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsBillingRate entity = new WmsBillingRate();
      Instant now = Instant.now();

      entity.setId(1);
      entity.setRateCardId(2);
      entity.setActivityTypeId(3);
      entity.setRate(new BigDecimal("4.00"));
      entity.setMinimumCharge(new BigDecimal("50.00"));
      entity.setNotes("Test");
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(1);
      assertThat(entity.getRateCardId()).isEqualTo(2);
      assertThat(entity.getActivityTypeId()).isEqualTo(3);
      assertThat(entity.getRate()).isEqualTo(new BigDecimal("4.00"));
      assertThat(entity.getMinimumCharge()).isEqualTo(new BigDecimal("50.00"));
      assertThat(entity.getNotes()).isEqualTo("Test");
      assertThat(entity.getCreateDate()).isEqualTo(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }
}
