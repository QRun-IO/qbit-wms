/*******************************************************************************
 ** Tests for WmsInvoice entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInvoiceTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsInvoice.TABLE_NAME).isEqualTo("wmsInvoice");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsInvoice entity = new WmsInvoice();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getInvoiceNumber()).isNull();
      assertThat(entity.getBillingPeriodStart()).isNull();
      assertThat(entity.getBillingPeriodEnd()).isNull();
      assertThat(entity.getSubtotal()).isNull();
      assertThat(entity.getTax()).isNull();
      assertThat(entity.getTotal()).isNull();
      assertThat(entity.getStatusId()).isNull();
      assertThat(entity.getGeneratedDate()).isNull();
      assertThat(entity.getSentDate()).isNull();
      assertThat(entity.getDueDate()).isNull();
      assertThat(entity.getPaidDate()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getExternalInvoiceId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsInvoice entity = new WmsInvoice();
      Instant now = Instant.now();
      WmsInvoice result = entity
         .withId(1)
         .withClientId(2)
         .withInvoiceNumber("INV-001")
         .withBillingPeriodStart(LocalDate.of(2025, 1, 1))
         .withBillingPeriodEnd(LocalDate.of(2025, 1, 31))
         .withSubtotal(new BigDecimal("100.00"))
         .withTax(new BigDecimal("8.00"))
         .withTotal(new BigDecimal("108.00"))
         .withStatusId(InvoiceStatus.DRAFT.getPossibleValueId())
         .withGeneratedDate(now)
         .withSentDate(now)
         .withDueDate(LocalDate.of(2025, 2, 28))
         .withPaidDate(now)
         .withNotes("Test invoice")
         .withExternalInvoiceId("EXT-001")
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
      LocalDate periodStart = LocalDate.of(2025, 3, 1);
      LocalDate periodEnd = LocalDate.of(2025, 3, 31);
      LocalDate dueDate = LocalDate.of(2025, 4, 30);

      WmsInvoice original = new WmsInvoice()
         .withId(42)
         .withClientId(5)
         .withInvoiceNumber("INV-042")
         .withBillingPeriodStart(periodStart)
         .withBillingPeriodEnd(periodEnd)
         .withSubtotal(new BigDecimal("500.00"))
         .withTax(new BigDecimal("40.00"))
         .withTotal(new BigDecimal("540.00"))
         .withStatusId(InvoiceStatus.SENT.getPossibleValueId())
         .withGeneratedDate(now)
         .withSentDate(now)
         .withDueDate(dueDate)
         .withPaidDate(now)
         .withNotes("March billing")
         .withExternalInvoiceId("EXT-042")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsInvoice restored = new WmsInvoice(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getClientId()).isEqualTo(5);
      assertThat(restored.getInvoiceNumber()).isEqualTo("INV-042");
      assertThat(restored.getBillingPeriodStart()).isEqualTo(periodStart);
      assertThat(restored.getBillingPeriodEnd()).isEqualTo(periodEnd);
      assertThat(restored.getSubtotal().compareTo(new BigDecimal("500.00"))).isEqualTo(0);
      assertThat(restored.getTax().compareTo(new BigDecimal("40.00"))).isEqualTo(0);
      assertThat(restored.getTotal().compareTo(new BigDecimal("540.00"))).isEqualTo(0);
      assertThat(restored.getStatusId()).isEqualTo(InvoiceStatus.SENT.getPossibleValueId());
      assertThat(restored.getGeneratedDate()).isEqualTo(now);
      assertThat(restored.getSentDate()).isEqualTo(now);
      assertThat(restored.getDueDate()).isEqualTo(dueDate);
      assertThat(restored.getPaidDate()).isEqualTo(now);
      assertThat(restored.getNotes()).isEqualTo("March billing");
      assertThat(restored.getExternalInvoiceId()).isEqualTo("EXT-042");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsInvoice entity = new WmsInvoice(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getInvoiceNumber()).isNull();
      assertThat(entity.getBillingPeriodStart()).isNull();
      assertThat(entity.getBillingPeriodEnd()).isNull();
      assertThat(entity.getSubtotal()).isNull();
      assertThat(entity.getTax()).isNull();
      assertThat(entity.getTotal()).isNull();
      assertThat(entity.getStatusId()).isNull();
      assertThat(entity.getGeneratedDate()).isNull();
      assertThat(entity.getSentDate()).isNull();
      assertThat(entity.getDueDate()).isNull();
      assertThat(entity.getPaidDate()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getExternalInvoiceId()).isNull();
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
      Integer invoiceId = insertInvoice(clientId);

      QRecord record = new GetAction().execute(new GetInput(WmsInvoice.TABLE_NAME).withPrimaryKey(invoiceId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueInteger("statusId")).isEqualTo(InvoiceStatus.DRAFT.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsInvoice entity = new WmsInvoice();
      Instant now = Instant.now();

      entity.setId(1);
      entity.setClientId(2);
      entity.setInvoiceNumber("INV-SET");
      entity.setBillingPeriodStart(LocalDate.of(2025, 1, 1));
      entity.setBillingPeriodEnd(LocalDate.of(2025, 1, 31));
      entity.setSubtotal(new BigDecimal("200.00"));
      entity.setTax(new BigDecimal("16.00"));
      entity.setTotal(new BigDecimal("216.00"));
      entity.setStatusId(1);
      entity.setGeneratedDate(now);
      entity.setSentDate(now);
      entity.setDueDate(LocalDate.of(2025, 2, 28));
      entity.setPaidDate(now);
      entity.setNotes("Test notes");
      entity.setExternalInvoiceId("EXT-SET");
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(1);
      assertThat(entity.getClientId()).isEqualTo(2);
      assertThat(entity.getInvoiceNumber()).isEqualTo("INV-SET");
      assertThat(entity.getSubtotal()).isEqualTo(new BigDecimal("200.00"));
      assertThat(entity.getTax()).isEqualTo(new BigDecimal("16.00"));
      assertThat(entity.getTotal()).isEqualTo(new BigDecimal("216.00"));
      assertThat(entity.getNotes()).isEqualTo("Test notes");
      assertThat(entity.getExternalInvoiceId()).isEqualTo("EXT-SET");
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var qInstance = com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
      var table = qInstance.getTable(WmsInvoice.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
