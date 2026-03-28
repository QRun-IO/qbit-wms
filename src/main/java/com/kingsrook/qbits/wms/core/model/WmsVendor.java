/*******************************************************************************
 ** QRecord Entity for WmsVendor table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsVendor extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsVendor";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, maxLength = 20)
   private String code;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField(maxLength = 100)
   private String contactName;

   @QField(maxLength = 200)
   private String contactEmail;

   @QField(maxLength = 30)
   private String contactPhone;

   @QField()
   private Integer defaultLeadTimeDays;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsVendor()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsVendor(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsVendor withId(Integer id) { this.id = id; return (this); }

   public String getCode() { return (this.code); }
   public void setCode(String code) { this.code = code; }
   public WmsVendor withCode(String code) { this.code = code; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsVendor withName(String name) { this.name = name; return (this); }

   public String getContactName() { return (this.contactName); }
   public void setContactName(String contactName) { this.contactName = contactName; }
   public WmsVendor withContactName(String contactName) { this.contactName = contactName; return (this); }

   public String getContactEmail() { return (this.contactEmail); }
   public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
   public WmsVendor withContactEmail(String contactEmail) { this.contactEmail = contactEmail; return (this); }

   public String getContactPhone() { return (this.contactPhone); }
   public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
   public WmsVendor withContactPhone(String contactPhone) { this.contactPhone = contactPhone; return (this); }

   public Integer getDefaultLeadTimeDays() { return (this.defaultLeadTimeDays); }
   public void setDefaultLeadTimeDays(Integer defaultLeadTimeDays) { this.defaultLeadTimeDays = defaultLeadTimeDays; }
   public WmsVendor withDefaultLeadTimeDays(Integer defaultLeadTimeDays) { this.defaultLeadTimeDays = defaultLeadTimeDays; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsVendor withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsVendor withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsVendor withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
