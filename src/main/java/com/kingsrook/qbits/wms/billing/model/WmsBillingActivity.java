/*******************************************************************************
 ** QRecord Entity for WmsBillingActivity table -- records a single billable
 ** activity performed in the warehouse for a client.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsBillingActivity extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsBillingActivity";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(possibleValueSourceName = BillingActivityType.NAME, label = "Activity Type")
   private Integer activityTypeId;

   @QField()
   private Instant activityDate;

   @QField()
   private BigDecimal quantity;

   @QField(maxLength = 50)
   private String referenceType;

   @QField()
   private Integer referenceId;

   @QField(possibleValueSourceName = WmsBillingRate.TABLE_NAME)
   private Integer rateId;

   @QField()
   private BigDecimal unitRate;

   @QField()
   private BigDecimal totalCharge;

   @QField(possibleValueSourceName = WmsInvoice.TABLE_NAME)
   private Integer invoiceId;

   @QField()
   private Boolean isBilled;

   @QField(possibleValueSourceName = WmsTask.TABLE_NAME)
   private Integer taskId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsBillingActivity()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsBillingActivity(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsBillingActivity withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsBillingActivity withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsBillingActivity withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getActivityTypeId() { return (this.activityTypeId); }
   public void setActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; }
   public WmsBillingActivity withActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; return (this); }

   public Instant getActivityDate() { return (this.activityDate); }
   public void setActivityDate(Instant activityDate) { this.activityDate = activityDate; }
   public WmsBillingActivity withActivityDate(Instant activityDate) { this.activityDate = activityDate; return (this); }

   public BigDecimal getQuantity() { return (this.quantity); }
   public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
   public WmsBillingActivity withQuantity(BigDecimal quantity) { this.quantity = quantity; return (this); }

   public String getReferenceType() { return (this.referenceType); }
   public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
   public WmsBillingActivity withReferenceType(String referenceType) { this.referenceType = referenceType; return (this); }

   public Integer getReferenceId() { return (this.referenceId); }
   public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
   public WmsBillingActivity withReferenceId(Integer referenceId) { this.referenceId = referenceId; return (this); }

   public Integer getRateId() { return (this.rateId); }
   public void setRateId(Integer rateId) { this.rateId = rateId; }
   public WmsBillingActivity withRateId(Integer rateId) { this.rateId = rateId; return (this); }

   public BigDecimal getUnitRate() { return (this.unitRate); }
   public void setUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; }
   public WmsBillingActivity withUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; return (this); }

   public BigDecimal getTotalCharge() { return (this.totalCharge); }
   public void setTotalCharge(BigDecimal totalCharge) { this.totalCharge = totalCharge; }
   public WmsBillingActivity withTotalCharge(BigDecimal totalCharge) { this.totalCharge = totalCharge; return (this); }

   public Integer getInvoiceId() { return (this.invoiceId); }
   public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }
   public WmsBillingActivity withInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; return (this); }

   public Boolean getIsBilled() { return (this.isBilled); }
   public void setIsBilled(Boolean isBilled) { this.isBilled = isBilled; }
   public WmsBillingActivity withIsBilled(Boolean isBilled) { this.isBilled = isBilled; return (this); }

   public Integer getTaskId() { return (this.taskId); }
   public void setTaskId(Integer taskId) { this.taskId = taskId; }
   public WmsBillingActivity withTaskId(Integer taskId) { this.taskId = taskId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsBillingActivity withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsBillingActivity withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
