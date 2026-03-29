/*******************************************************************************
 ** Tests for WmsBillingActivity entity.
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


class WmsBillingActivityTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsBillingActivity.TABLE_NAME).isEqualTo("wmsBillingActivity");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsBillingActivity entity = new WmsBillingActivity();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getActivityDate()).isNull();
      assertThat(entity.getQuantity()).isNull();
      assertThat(entity.getReferenceType()).isNull();
      assertThat(entity.getReferenceId()).isNull();
      assertThat(entity.getRateId()).isNull();
      assertThat(entity.getUnitRate()).isNull();
      assertThat(entity.getTotalCharge()).isNull();
      assertThat(entity.getInvoiceId()).isNull();
      assertThat(entity.getIsBilled()).isNull();
      assertThat(entity.getTaskId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsBillingActivity entity = new WmsBillingActivity();
      Instant now = Instant.now();
      WmsBillingActivity result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withActivityTypeId(4)
         .withActivityDate(now)
         .withQuantity(new BigDecimal("10"))
         .withReferenceType("ORDER")
         .withReferenceId(5)
         .withRateId(6)
         .withUnitRate(new BigDecimal("1.50"))
         .withTotalCharge(new BigDecimal("15.00"))
         .withInvoiceId(7)
         .withIsBilled(true)
         .withTaskId(8)
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

      WmsBillingActivity original = new WmsBillingActivity()
         .withId(42)
         .withWarehouseId(1)
         .withClientId(2)
         .withActivityTypeId(3)
         .withActivityDate(now)
         .withQuantity(new BigDecimal("25"))
         .withReferenceType("TASK")
         .withReferenceId(10)
         .withRateId(5)
         .withUnitRate(new BigDecimal("2.50"))
         .withTotalCharge(new BigDecimal("62.50"))
         .withInvoiceId(7)
         .withIsBilled(false)
         .withTaskId(11)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsBillingActivity restored = new WmsBillingActivity(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getActivityTypeId()).isEqualTo(3);
      assertThat(restored.getActivityDate()).isEqualTo(now);
      assertThat(restored.getQuantity().compareTo(new BigDecimal("25"))).isEqualTo(0);
      assertThat(restored.getReferenceType()).isEqualTo("TASK");
      assertThat(restored.getReferenceId()).isEqualTo(10);
      assertThat(restored.getRateId()).isEqualTo(5);
      assertThat(restored.getUnitRate().compareTo(new BigDecimal("2.50"))).isEqualTo(0);
      assertThat(restored.getTotalCharge().compareTo(new BigDecimal("62.50"))).isEqualTo(0);
      assertThat(restored.getInvoiceId()).isEqualTo(7);
      assertThat(restored.getIsBilled()).isFalse();
      assertThat(restored.getTaskId()).isEqualTo(11);
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsBillingActivity entity = new WmsBillingActivity(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getActivityDate()).isNull();
      assertThat(entity.getQuantity()).isNull();
      assertThat(entity.getReferenceType()).isNull();
      assertThat(entity.getReferenceId()).isNull();
      assertThat(entity.getRateId()).isNull();
      assertThat(entity.getUnitRate()).isNull();
      assertThat(entity.getTotalCharge()).isNull();
      assertThat(entity.getInvoiceId()).isNull();
      assertThat(entity.getIsBilled()).isNull();
      assertThat(entity.getTaskId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test insert and retrieve via BaseTest helper.
    *******************************************************************************/
   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer clientId = insertClient();
      Integer activityId = insertBillingActivity(warehouseId, clientId,
         BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("25"));

      QRecord record = new GetAction().execute(new GetInput(WmsBillingActivity.TABLE_NAME).withPrimaryKey(activityId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueBigDecimal("quantity").compareTo(new BigDecimal("25"))).isEqualTo(0);
      assertThat(record.getValueBoolean("isBilled")).isFalse();
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsBillingActivity entity = new WmsBillingActivity();
      Instant now = Instant.now();

      entity.setId(1);
      entity.setWarehouseId(2);
      entity.setClientId(3);
      entity.setActivityTypeId(4);
      entity.setActivityDate(now);
      entity.setQuantity(new BigDecimal("5"));
      entity.setReferenceType("REF");
      entity.setReferenceId(6);
      entity.setRateId(7);
      entity.setUnitRate(new BigDecimal("3.00"));
      entity.setTotalCharge(new BigDecimal("15.00"));
      entity.setInvoiceId(8);
      entity.setIsBilled(true);
      entity.setTaskId(9);
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(1);
      assertThat(entity.getWarehouseId()).isEqualTo(2);
      assertThat(entity.getClientId()).isEqualTo(3);
      assertThat(entity.getActivityTypeId()).isEqualTo(4);
      assertThat(entity.getActivityDate()).isEqualTo(now);
      assertThat(entity.getQuantity()).isEqualTo(new BigDecimal("5"));
      assertThat(entity.getReferenceType()).isEqualTo("REF");
      assertThat(entity.getReferenceId()).isEqualTo(6);
      assertThat(entity.getRateId()).isEqualTo(7);
      assertThat(entity.getUnitRate()).isEqualTo(new BigDecimal("3.00"));
      assertThat(entity.getTotalCharge()).isEqualTo(new BigDecimal("15.00"));
      assertThat(entity.getInvoiceId()).isEqualTo(8);
      assertThat(entity.getIsBilled()).isTrue();
      assertThat(entity.getTaskId()).isEqualTo(9);
      assertThat(entity.getCreateDate()).isEqualTo(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }
}
