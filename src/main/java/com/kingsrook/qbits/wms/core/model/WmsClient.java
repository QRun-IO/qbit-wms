/*******************************************************************************
 ** QRecord Entity for WmsClient table (3PL client/tenant).
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;


@QMetaDataProducingEntity(producePossibleValueSource = true)
public class WmsClient extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsClient";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField(isRequired = true, maxLength = 20)
   private String code;

   @QField(maxLength = 100)
   private String contactName;

   @QField(maxLength = 200)
   private String contactEmail;

   @QField(maxLength = 30)
   private String contactPhone;

   @QField(maxLength = 200)
   private String billingEmail;

   @QField(maxLength = 100)
   private String defaultCarrier;

   @QField(maxLength = 100)
   private String defaultServiceLevel;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsClient()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsClient(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsClient withId(Integer id) { this.id = id; return (this); }

   public String getName() { return (this.name); }
   public WmsClient withName(String name) { this.name = name; return (this); }

   public String getCode() { return (this.code); }
   public WmsClient withCode(String code) { this.code = code; return (this); }

   public String getContactName() { return (this.contactName); }
   public WmsClient withContactName(String contactName) { this.contactName = contactName; return (this); }

   public String getContactEmail() { return (this.contactEmail); }
   public WmsClient withContactEmail(String contactEmail) { this.contactEmail = contactEmail; return (this); }

   public String getContactPhone() { return (this.contactPhone); }
   public WmsClient withContactPhone(String contactPhone) { this.contactPhone = contactPhone; return (this); }

   public String getBillingEmail() { return (this.billingEmail); }
   public WmsClient withBillingEmail(String billingEmail) { this.billingEmail = billingEmail; return (this); }

   public String getDefaultCarrier() { return (this.defaultCarrier); }
   public WmsClient withDefaultCarrier(String defaultCarrier) { this.defaultCarrier = defaultCarrier; return (this); }

   public String getDefaultServiceLevel() { return (this.defaultServiceLevel); }
   public WmsClient withDefaultServiceLevel(String defaultServiceLevel) { this.defaultServiceLevel = defaultServiceLevel; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public WmsClient withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsClient withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsClient withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
