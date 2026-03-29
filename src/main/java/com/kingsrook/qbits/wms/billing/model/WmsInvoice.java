/*******************************************************************************
 ** QRecord Entity for WmsInvoice table -- represents a billing invoice sent
 ** to a 3PL client for warehouse services rendered during a billing period.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import com.kingsrook.qbits.wms.core.model.WmsClient;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsInvoice.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsInvoiceLine.class,
         joinFieldName = "invoiceId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Invoice Lines", enabled = true, maxRows = 50))
   }
)
public class WmsInvoice extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsInvoice";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableMetaDataCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsInvoice.TABLE_NAME, WmsInvoiceLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("receipt_long"))
            .withRecordLabelFormat("INV-%s")
            .withRecordLabelFields("invoiceNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "clientId", "invoiceNumber", "statusId")))
            .withSection(new QFieldSection("billing", "Billing Period", new QIcon("date_range"), Tier.T2,
               java.util.List.of("billingPeriodStart", "billingPeriodEnd", "generatedDate", "sentDate", "dueDate", "paidDate")))
            .withSection(new QFieldSection("totals", "Totals", new QIcon("payments"), Tier.T2,
               java.util.List.of("subtotal", "tax", "total")))
            .withSection(SectionFactory.customT2("invoiceLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("externalInvoiceId", "notes")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, maxLength = 50)
   private String invoiceNumber;

   @QField()
   private LocalDate billingPeriodStart;

   @QField()
   private LocalDate billingPeriodEnd;

   @QField()
   private BigDecimal subtotal;

   @QField()
   private BigDecimal tax;

   @QField()
   private BigDecimal total;

   @QField(possibleValueSourceName = InvoiceStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private Instant generatedDate;

   @QField()
   private Instant sentDate;

   @QField()
   private LocalDate dueDate;

   @QField()
   private Instant paidDate;

   @QField(maxLength = 2000)
   private String notes;

   @QField(maxLength = 100)
   private String externalInvoiceId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsInvoice()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsInvoice(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsInvoice withId(Integer id) { this.id = id; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsInvoice withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getInvoiceNumber() { return (this.invoiceNumber); }
   public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
   public WmsInvoice withInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; return (this); }

   public LocalDate getBillingPeriodStart() { return (this.billingPeriodStart); }
   public void setBillingPeriodStart(LocalDate billingPeriodStart) { this.billingPeriodStart = billingPeriodStart; }
   public WmsInvoice withBillingPeriodStart(LocalDate billingPeriodStart) { this.billingPeriodStart = billingPeriodStart; return (this); }

   public LocalDate getBillingPeriodEnd() { return (this.billingPeriodEnd); }
   public void setBillingPeriodEnd(LocalDate billingPeriodEnd) { this.billingPeriodEnd = billingPeriodEnd; }
   public WmsInvoice withBillingPeriodEnd(LocalDate billingPeriodEnd) { this.billingPeriodEnd = billingPeriodEnd; return (this); }

   public BigDecimal getSubtotal() { return (this.subtotal); }
   public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
   public WmsInvoice withSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; return (this); }

   public BigDecimal getTax() { return (this.tax); }
   public void setTax(BigDecimal tax) { this.tax = tax; }
   public WmsInvoice withTax(BigDecimal tax) { this.tax = tax; return (this); }

   public BigDecimal getTotal() { return (this.total); }
   public void setTotal(BigDecimal total) { this.total = total; }
   public WmsInvoice withTotal(BigDecimal total) { this.total = total; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsInvoice withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getGeneratedDate() { return (this.generatedDate); }
   public void setGeneratedDate(Instant generatedDate) { this.generatedDate = generatedDate; }
   public WmsInvoice withGeneratedDate(Instant generatedDate) { this.generatedDate = generatedDate; return (this); }

   public Instant getSentDate() { return (this.sentDate); }
   public void setSentDate(Instant sentDate) { this.sentDate = sentDate; }
   public WmsInvoice withSentDate(Instant sentDate) { this.sentDate = sentDate; return (this); }

   public LocalDate getDueDate() { return (this.dueDate); }
   public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
   public WmsInvoice withDueDate(LocalDate dueDate) { this.dueDate = dueDate; return (this); }

   public Instant getPaidDate() { return (this.paidDate); }
   public void setPaidDate(Instant paidDate) { this.paidDate = paidDate; }
   public WmsInvoice withPaidDate(Instant paidDate) { this.paidDate = paidDate; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsInvoice withNotes(String notes) { this.notes = notes; return (this); }

   public String getExternalInvoiceId() { return (this.externalInvoiceId); }
   public void setExternalInvoiceId(String externalInvoiceId) { this.externalInvoiceId = externalInvoiceId; }
   public WmsInvoice withExternalInvoiceId(String externalInvoiceId) { this.externalInvoiceId = externalInvoiceId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsInvoice withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsInvoice withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
