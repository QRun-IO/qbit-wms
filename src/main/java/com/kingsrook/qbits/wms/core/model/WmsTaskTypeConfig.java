/*******************************************************************************
 ** QRecord Entity for WmsTaskTypeConfig table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.EquipmentType;
import com.kingsrook.qbits.wms.core.enums.TaskType;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsTaskTypeConfig extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsTaskTypeConfig";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = TaskType.NAME, label = "Task Type")
   private Integer taskTypeId;

   @QField()
   private Integer defaultPriority;

   @QField(possibleValueSourceName = EquipmentType.NAME, label = "Default Equipment Type")
   private Integer defaultEquipmentTypeId;

   @QField()
   private Boolean autoAssignEnabled;

   @QField()
   private Boolean scanSourceLocation;

   @QField()
   private Boolean scanDestinationLocation;

   @QField()
   private Boolean scanItem;

   @QField()
   private Integer escalationMinutes;

   @QField(maxLength = 500)
   private String description;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsTaskTypeConfig()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsTaskTypeConfig(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsTaskTypeConfig withId(Integer id) { this.id = id; return (this); }

   public Integer getTaskTypeId() { return (this.taskTypeId); }
   public void setTaskTypeId(Integer taskTypeId) { this.taskTypeId = taskTypeId; }
   public WmsTaskTypeConfig withTaskTypeId(Integer taskTypeId) { this.taskTypeId = taskTypeId; return (this); }

   public Integer getDefaultPriority() { return (this.defaultPriority); }
   public void setDefaultPriority(Integer defaultPriority) { this.defaultPriority = defaultPriority; }
   public WmsTaskTypeConfig withDefaultPriority(Integer defaultPriority) { this.defaultPriority = defaultPriority; return (this); }

   public Integer getDefaultEquipmentTypeId() { return (this.defaultEquipmentTypeId); }
   public void setDefaultEquipmentTypeId(Integer defaultEquipmentTypeId) { this.defaultEquipmentTypeId = defaultEquipmentTypeId; }
   public WmsTaskTypeConfig withDefaultEquipmentTypeId(Integer defaultEquipmentTypeId) { this.defaultEquipmentTypeId = defaultEquipmentTypeId; return (this); }

   public Boolean getAutoAssignEnabled() { return (this.autoAssignEnabled); }
   public void setAutoAssignEnabled(Boolean autoAssignEnabled) { this.autoAssignEnabled = autoAssignEnabled; }
   public WmsTaskTypeConfig withAutoAssignEnabled(Boolean autoAssignEnabled) { this.autoAssignEnabled = autoAssignEnabled; return (this); }

   public Boolean getScanSourceLocation() { return (this.scanSourceLocation); }
   public void setScanSourceLocation(Boolean scanSourceLocation) { this.scanSourceLocation = scanSourceLocation; }
   public WmsTaskTypeConfig withScanSourceLocation(Boolean scanSourceLocation) { this.scanSourceLocation = scanSourceLocation; return (this); }

   public Boolean getScanDestinationLocation() { return (this.scanDestinationLocation); }
   public void setScanDestinationLocation(Boolean scanDestinationLocation) { this.scanDestinationLocation = scanDestinationLocation; }
   public WmsTaskTypeConfig withScanDestinationLocation(Boolean scanDestinationLocation) { this.scanDestinationLocation = scanDestinationLocation; return (this); }

   public Boolean getScanItem() { return (this.scanItem); }
   public void setScanItem(Boolean scanItem) { this.scanItem = scanItem; }
   public WmsTaskTypeConfig withScanItem(Boolean scanItem) { this.scanItem = scanItem; return (this); }

   public Integer getEscalationMinutes() { return (this.escalationMinutes); }
   public void setEscalationMinutes(Integer escalationMinutes) { this.escalationMinutes = escalationMinutes; }
   public WmsTaskTypeConfig withEscalationMinutes(Integer escalationMinutes) { this.escalationMinutes = escalationMinutes; return (this); }

   public String getDescription() { return (this.description); }
   public void setDescription(String description) { this.description = description; }
   public WmsTaskTypeConfig withDescription(String description) { this.description = description; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsTaskTypeConfig withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsTaskTypeConfig withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
