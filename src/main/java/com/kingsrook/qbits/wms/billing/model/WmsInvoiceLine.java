/*******************************************************************************
 ** QRecord Entity for WmsInvoiceLine table -- represents a single line item
 ** on a billing invoice, aggregated from billing activities by type.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsInvoiceLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsInvoiceLine";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsInvoice.TABLE_NAME)
   private Integer invoiceId;

   @QField(possibleValueSourceName = BillingActivityType.NAME, label = "Activity Type")
   private Integer activityTypeId;

   @QField(maxLength = 500)
   private String description;

   @QField()
   private BigDecimal quantity;

   @QField()
   private BigDecimal unitRate;

   @QField()
   private BigDecimal lineTotal;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsInvoiceLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsInvoiceLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsInvoiceLine withId(Integer id) { this.id = id; return (this); }

   public Integer getInvoiceId() { return (this.invoiceId); }
   public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
   public WmsInvoiceLine withInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; return (this); }

   public Integer getActivityTypeId() { return (this.activityTypeId); }
   public void setActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; }
   public WmsInvoiceLine withActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; return (this); }

   public String getDescription() { return (this.description); }
   public void setDescription(String description) { this.description = description; }
   public WmsInvoiceLine withDescription(String description) { this.description = description; return (this); }

   public BigDecimal getQuantity() { return (this.quantity); }
   public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
   public WmsInvoiceLine withQuantity(BigDecimal quantity) { this.quantity = quantity; return (this); }

   public BigDecimal getUnitRate() { return (this.unitRate); }
   public void setUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; }
   public WmsInvoiceLine withUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; return (this); }

   public BigDecimal getLineTotal() { return (this.lineTotal); }
   public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
   public WmsInvoiceLine withLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsInvoiceLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsInvoiceLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
