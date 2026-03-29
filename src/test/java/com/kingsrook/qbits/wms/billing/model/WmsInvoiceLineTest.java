/*******************************************************************************
 ** Tests for WmsInvoiceLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInvoiceLineTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsInvoiceLine.TABLE_NAME).isEqualTo("wmsInvoiceLine");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsInvoiceLine entity = new WmsInvoiceLine();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getInvoiceId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getDescription()).isNull();
      assertThat(entity.getQuantity()).isNull();
      assertThat(entity.getUnitRate()).isNull();
      assertThat(entity.getLineTotal()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsInvoiceLine entity = new WmsInvoiceLine();
      Instant now = Instant.now();
      WmsInvoiceLine result = entity
         .withId(1)
         .withInvoiceId(2)
         .withActivityTypeId(3)
         .withDescription("Pick Per Unit")
         .withQuantity(new BigDecimal("100"))
         .withUnitRate(new BigDecimal("1.50"))
         .withLineTotal(new BigDecimal("150.00"))
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

      WmsInvoiceLine original = new WmsInvoiceLine()
         .withId(42)
         .withInvoiceId(5)
         .withActivityTypeId(BillingActivityType.PICK_PER_UNIT.getPossibleValueId())
         .withDescription("Unit picking charges")
         .withQuantity(new BigDecimal("250"))
         .withUnitRate(new BigDecimal("0.75"))
         .withLineTotal(new BigDecimal("187.50"))
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsInvoiceLine restored = new WmsInvoiceLine(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getInvoiceId()).isEqualTo(5);
      assertThat(restored.getActivityTypeId()).isEqualTo(BillingActivityType.PICK_PER_UNIT.getPossibleValueId());
      assertThat(restored.getDescription()).isEqualTo("Unit picking charges");
      assertThat(restored.getQuantity().compareTo(new BigDecimal("250"))).isEqualTo(0);
      assertThat(restored.getUnitRate().compareTo(new BigDecimal("0.75"))).isEqualTo(0);
      assertThat(restored.getLineTotal().compareTo(new BigDecimal("187.50"))).isEqualTo(0);
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsInvoiceLine entity = new WmsInvoiceLine(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getInvoiceId()).isNull();
      assertThat(entity.getActivityTypeId()).isNull();
      assertThat(entity.getDescription()).isNull();
      assertThat(entity.getQuantity()).isNull();
      assertThat(entity.getUnitRate()).isNull();
      assertThat(entity.getLineTotal()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test insert and retrieve with all fields populated.
    *******************************************************************************/
   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer invoiceId = insertInvoice(clientId);

      Integer lineId = new InsertAction().execute(new InsertInput(WmsInvoiceLine.TABLE_NAME)
            .withRecordEntity(new WmsInvoiceLine()
               .withInvoiceId(invoiceId)
               .withActivityTypeId(BillingActivityType.PICK_PER_UNIT.getPossibleValueId())
               .withDescription("Pick Per Unit")
               .withQuantity(new BigDecimal("100"))
               .withUnitRate(new BigDecimal("1.50"))
               .withLineTotal(new BigDecimal("150"))))
         .getRecords().get(0).getValueInteger("id");

      QRecord record = new GetAction().execute(new GetInput(WmsInvoiceLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("invoiceId")).isEqualTo(invoiceId);
      assertThat(record.getValueBigDecimal("lineTotal").compareTo(new BigDecimal("150"))).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsInvoiceLine entity = new WmsInvoiceLine();
      Instant now = Instant.now();

      entity.setId(1);
      entity.setInvoiceId(2);
      entity.setActivityTypeId(3);
      entity.setDescription("Storage");
      entity.setQuantity(new BigDecimal("50"));
      entity.setUnitRate(new BigDecimal("2.00"));
      entity.setLineTotal(new BigDecimal("100.00"));
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(1);
      assertThat(entity.getInvoiceId()).isEqualTo(2);
      assertThat(entity.getActivityTypeId()).isEqualTo(3);
      assertThat(entity.getDescription()).isEqualTo("Storage");
      assertThat(entity.getQuantity()).isEqualTo(new BigDecimal("50"));
      assertThat(entity.getUnitRate()).isEqualTo(new BigDecimal("2.00"));
      assertThat(entity.getLineTotal()).isEqualTo(new BigDecimal("100.00"));
      assertThat(entity.getCreateDate()).isEqualTo(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }
}
