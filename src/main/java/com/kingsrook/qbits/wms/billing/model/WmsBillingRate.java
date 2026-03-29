/*******************************************************************************
 ** QRecord Entity for WmsBillingRate table -- defines a per-activity-type rate
 ** within a billing rate card.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsBillingRate extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsBillingRate";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsBillingRateCard.TABLE_NAME)
   private Integer rateCardId;

   @QField(possibleValueSourceName = BillingActivityType.NAME, label = "Activity Type")
   private Integer activityTypeId;

   @QField(isRequired = true)
   private BigDecimal rate;

   @QField()
   private BigDecimal minimumCharge;

   @QField(maxLength = 500)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsBillingRate()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsBillingRate(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsBillingRate withId(Integer id) { this.id = id; return (this); }

   public Integer getRateCardId() { return (this.rateCardId); }
   public void setRateCardId(Integer rateCardId) { this.rateCardId = rateCardId; }
   public WmsBillingRate withRateCardId(Integer rateCardId) { this.rateCardId = rateCardId; return (this); }

   public Integer getActivityTypeId() { return (this.activityTypeId); }
   public void setActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; }
   public WmsBillingRate withActivityTypeId(Integer activityTypeId) { this.activityTypeId = activityTypeId; return (this); }

   public BigDecimal getRate() { return (this.rate); }
   public void setRate(BigDecimal rate) { this.rate = rate; }
   public WmsBillingRate withRate(BigDecimal rate) { this.rate = rate; return (this); }

   public BigDecimal getMinimumCharge() { return (this.minimumCharge); }
   public void setMinimumCharge(BigDecimal minimumCharge) { this.minimumCharge = minimumCharge; }
   public WmsBillingRate withMinimumCharge(BigDecimal minimumCharge) { this.minimumCharge = minimumCharge; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsBillingRate withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsBillingRate withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsBillingRate withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
