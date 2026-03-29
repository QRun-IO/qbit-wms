/*******************************************************************************
 ** QRecord Entity for WmsPutawayRule table -- configurable rules that determine
 ** the target zone for putaway tasks based on item attributes such as zone type,
 ** category, velocity class, and storage requirements.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.StorageRequirements;
import com.kingsrook.qbits.wms.core.enums.VelocityClass;
import com.kingsrook.qbits.wms.core.enums.ZoneType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsItemCategory;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsPutawayRule extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsPutawayRule";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, maxLength = 100)
   private String ruleName;

   @QField(defaultValue = "5")
   private Integer priority;

   @QField(possibleValueSourceName = ZoneType.NAME, label = "Zone Type Match")
   private Integer zoneTypeMatch;

   @QField(possibleValueSourceName = WmsItemCategory.TABLE_NAME, label = "Item Category Match")
   private Integer itemCategoryMatch;

   @QField(possibleValueSourceName = VelocityClass.NAME, label = "Velocity Class Match")
   private Integer velocityClassMatch;

   @QField(possibleValueSourceName = StorageRequirements.NAME, label = "Storage Requirements Match")
   private Integer storageRequirementsMatch;

   @QField(isRequired = true, possibleValueSourceName = WmsZone.TABLE_NAME, label = "Target Zone")
   private Integer targetZoneId;

   @QField(defaultValue = "true")
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsPutawayRule()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsPutawayRule(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsPutawayRule withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsPutawayRule withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsPutawayRule withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getRuleName() { return (this.ruleName); }
   public void setRuleName(String ruleName) { this.ruleName = ruleName; }
   public WmsPutawayRule withRuleName(String ruleName) { this.ruleName = ruleName; return (this); }

   public Integer getPriority() { return (this.priority); }
   public void setPriority(Integer priority) { this.priority = priority; }
   public WmsPutawayRule withPriority(Integer priority) { this.priority = priority; return (this); }

   public Integer getZoneTypeMatch() { return (this.zoneTypeMatch); }
   public void setZoneTypeMatch(Integer zoneTypeMatch) { this.zoneTypeMatch = zoneTypeMatch; }
   public WmsPutawayRule withZoneTypeMatch(Integer zoneTypeMatch) { this.zoneTypeMatch = zoneTypeMatch; return (this); }

   public Integer getItemCategoryMatch() { return (this.itemCategoryMatch); }
   public void setItemCategoryMatch(Integer itemCategoryMatch) { this.itemCategoryMatch = itemCategoryMatch; }
   public WmsPutawayRule withItemCategoryMatch(Integer itemCategoryMatch) { this.itemCategoryMatch = itemCategoryMatch; return (this); }

   public Integer getVelocityClassMatch() { return (this.velocityClassMatch); }
   public void setVelocityClassMatch(Integer velocityClassMatch) { this.velocityClassMatch = velocityClassMatch; }
   public WmsPutawayRule withVelocityClassMatch(Integer velocityClassMatch) { this.velocityClassMatch = velocityClassMatch; return (this); }

   public Integer getStorageRequirementsMatch() { return (this.storageRequirementsMatch); }
   public void setStorageRequirementsMatch(Integer storageRequirementsMatch) { this.storageRequirementsMatch = storageRequirementsMatch; }
   public WmsPutawayRule withStorageRequirementsMatch(Integer storageRequirementsMatch) { this.storageRequirementsMatch = storageRequirementsMatch; return (this); }

   public Integer getTargetZoneId() { return (this.targetZoneId); }
   public void setTargetZoneId(Integer targetZoneId) { this.targetZoneId = targetZoneId; }
   public WmsPutawayRule withTargetZoneId(Integer targetZoneId) { this.targetZoneId = targetZoneId; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsPutawayRule withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsPutawayRule withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsPutawayRule withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
