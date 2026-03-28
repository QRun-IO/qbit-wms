/*******************************************************************************
 ** QRecord Entity for WmsTask table -- the central work execution entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.EquipmentType;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsTask.TableMetaDataCustomizer.class
)
public class WmsTask extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsTask";



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
         table
            .withIcon(new QIcon().withName("assignment"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("taskInfo", "Task Info", new QIcon("info"), Tier.T1,
               java.util.List.of("id", "warehouseId", "clientId", "taskTypeId", "taskStatusId", "priority", "equipmentTypeId")))
            .withSection(new QFieldSection("what", "What", new QIcon("inventory_2"), Tier.T2,
               java.util.List.of("itemId", "quantityRequested", "quantityCompleted", "lotNumber", "serialNumber", "lpnId")))
            .withSection(new QFieldSection("where", "Where", new QIcon("place"), Tier.T2,
               java.util.List.of("sourceLocationId", "destinationLocationId", "zoneId")))
            .withSection(new QFieldSection("who", "Who", new QIcon("person"), Tier.T2,
               java.util.List.of("assignedTo", "assignedDate", "startedDate", "completedDate", "completedBy")))
            .withSection(new QFieldSection("grouping", "Grouping", new QIcon("workspaces"), Tier.T2,
               java.util.List.of("waveId", "taskGroupId", "sequence")))
            .withSection(new QFieldSection("references", "References", new QIcon("link"), Tier.T2,
               java.util.List.of("referenceType", "referenceId", "orderId", "orderLineId", "receiptId", "receiptLineId", "cycleCountId", "returnAuthorizationId")))
            .withSection(new QFieldSection("countSpecific", "Count Specific", new QIcon("exposure"), Tier.T2,
               java.util.List.of("expectedQuantity", "countedQuantity", "variance", "isBlindCount", "recountRequired")))
            .withSection(new QFieldSection("notes", "Notes", new QIcon("notes"), Tier.T2,
               java.util.List.of("notes", "shortReason")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, possibleValueSourceName = TaskType.NAME, label = "Task Type")
   private Integer taskTypeId;

   @QField(isRequired = true, possibleValueSourceName = TaskStatus.NAME, label = "Status")
   private Integer taskStatusId;

   @QField()
   private Integer priority;

   @QField(possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField()
   private BigDecimal quantityRequested;

   @QField()
   private BigDecimal quantityCompleted;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(maxLength = 50)
   private String serialNumber;

   @QField(possibleValueSourceName = WmsLicensePlate.TABLE_NAME)
   private Integer lpnId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Source Location")
   private Integer sourceLocationId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Destination Location")
   private Integer destinationLocationId;

   @QField(possibleValueSourceName = "wmsZone")
   private Integer zoneId;

   @QField(maxLength = 100)
   private String assignedTo;

   @QField()
   private Instant assignedDate;

   @QField()
   private Instant startedDate;

   @QField()
   private Instant completedDate;

   @QField(maxLength = 100)
   private String completedBy;

   @QField()
   private Integer waveId;

   @QField(maxLength = 50)
   private String taskGroupId;

   @QField()
   private Integer sequence;

   @QField(possibleValueSourceName = EquipmentType.NAME, label = "Equipment Type")
   private Integer equipmentTypeId;

   @QField(maxLength = 50)
   private String referenceType;

   @QField()
   private Integer referenceId;

   @QField()
   private Integer orderId;

   @QField()
   private Integer orderLineId;

   @QField()
   private Integer receiptId;

   @QField()
   private Integer receiptLineId;

   @QField()
   private Integer cycleCountId;

   @QField()
   private Integer returnAuthorizationId;

   @QField()
   private BigDecimal expectedQuantity;

   @QField()
   private BigDecimal countedQuantity;

   @QField()
   private BigDecimal variance;

   @QField()
   private Boolean isBlindCount;

   @QField()
   private Boolean recountRequired;

   @QField(maxLength = 500)
   private String notes;

   @QField(maxLength = 250)
   private String shortReason;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsTask()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsTask(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsTask withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsTask withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsTask withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getTaskTypeId() { return (this.taskTypeId); }
   public void setTaskTypeId(Integer taskTypeId) { this.taskTypeId = taskTypeId; }
   public WmsTask withTaskTypeId(Integer taskTypeId) { this.taskTypeId = taskTypeId; return (this); }

   public Integer getTaskStatusId() { return (this.taskStatusId); }
   public void setTaskStatusId(Integer taskStatusId) { this.taskStatusId = taskStatusId; }
   public WmsTask withTaskStatusId(Integer taskStatusId) { this.taskStatusId = taskStatusId; return (this); }

   public Integer getPriority() { return (this.priority); }
   public void setPriority(Integer priority) { this.priority = priority; }
   public WmsTask withPriority(Integer priority) { this.priority = priority; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsTask withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public BigDecimal getQuantityRequested() { return (this.quantityRequested); }
   public void setQuantityRequested(BigDecimal quantityRequested) { this.quantityRequested = quantityRequested; }
   public WmsTask withQuantityRequested(BigDecimal quantityRequested) { this.quantityRequested = quantityRequested; return (this); }

   public BigDecimal getQuantityCompleted() { return (this.quantityCompleted); }
   public void setQuantityCompleted(BigDecimal quantityCompleted) { this.quantityCompleted = quantityCompleted; }
   public WmsTask withQuantityCompleted(BigDecimal quantityCompleted) { this.quantityCompleted = quantityCompleted; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsTask withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumber() { return (this.serialNumber); }
   public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
   public WmsTask withSerialNumber(String serialNumber) { this.serialNumber = serialNumber; return (this); }

   public Integer getLpnId() { return (this.lpnId); }
   public void setLpnId(Integer lpnId) { this.lpnId = lpnId; }
   public WmsTask withLpnId(Integer lpnId) { this.lpnId = lpnId; return (this); }

   public Integer getSourceLocationId() { return (this.sourceLocationId); }
   public void setSourceLocationId(Integer sourceLocationId) { this.sourceLocationId = sourceLocationId; }
   public WmsTask withSourceLocationId(Integer sourceLocationId) { this.sourceLocationId = sourceLocationId; return (this); }

   public Integer getDestinationLocationId() { return (this.destinationLocationId); }
   public void setDestinationLocationId(Integer destinationLocationId) { this.destinationLocationId = destinationLocationId; }
   public WmsTask withDestinationLocationId(Integer destinationLocationId) { this.destinationLocationId = destinationLocationId; return (this); }

   public Integer getZoneId() { return (this.zoneId); }
   public void setZoneId(Integer zoneId) { this.zoneId = zoneId; }
   public WmsTask withZoneId(Integer zoneId) { this.zoneId = zoneId; return (this); }

   public String getAssignedTo() { return (this.assignedTo); }
   public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
   public WmsTask withAssignedTo(String assignedTo) { this.assignedTo = assignedTo; return (this); }

   public Instant getAssignedDate() { return (this.assignedDate); }
   public void setAssignedDate(Instant assignedDate) { this.assignedDate = assignedDate; }
   public WmsTask withAssignedDate(Instant assignedDate) { this.assignedDate = assignedDate; return (this); }

   public Instant getStartedDate() { return (this.startedDate); }
   public void setStartedDate(Instant startedDate) { this.startedDate = startedDate; }
   public WmsTask withStartedDate(Instant startedDate) { this.startedDate = startedDate; return (this); }

   public Instant getCompletedDate() { return (this.completedDate); }
   public void setCompletedDate(Instant completedDate) { this.completedDate = completedDate; }
   public WmsTask withCompletedDate(Instant completedDate) { this.completedDate = completedDate; return (this); }

   public String getCompletedBy() { return (this.completedBy); }
   public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
   public WmsTask withCompletedBy(String completedBy) { this.completedBy = completedBy; return (this); }

   public Integer getWaveId() { return (this.waveId); }
   public void setWaveId(Integer waveId) { this.waveId = waveId; }
   public WmsTask withWaveId(Integer waveId) { this.waveId = waveId; return (this); }

   public String getTaskGroupId() { return (this.taskGroupId); }
   public void setTaskGroupId(String taskGroupId) { this.taskGroupId = taskGroupId; }
   public WmsTask withTaskGroupId(String taskGroupId) { this.taskGroupId = taskGroupId; return (this); }

   public Integer getSequence() { return (this.sequence); }
   public void setSequence(Integer sequence) { this.sequence = sequence; }
   public WmsTask withSequence(Integer sequence) { this.sequence = sequence; return (this); }

   public Integer getEquipmentTypeId() { return (this.equipmentTypeId); }
   public void setEquipmentTypeId(Integer equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; }
   public WmsTask withEquipmentTypeId(Integer equipmentTypeId) { this.equipmentTypeId = equipmentTypeId; return (this); }

   public String getReferenceType() { return (this.referenceType); }
   public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
   public WmsTask withReferenceType(String referenceType) { this.referenceType = referenceType; return (this); }

   public Integer getReferenceId() { return (this.referenceId); }
   public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
   public WmsTask withReferenceId(Integer referenceId) { this.referenceId = referenceId; return (this); }

   public Integer getOrderId() { return (this.orderId); }
   public void setOrderId(Integer orderId) { this.orderId = orderId; }
   public WmsTask withOrderId(Integer orderId) { this.orderId = orderId; return (this); }

   public Integer getOrderLineId() { return (this.orderLineId); }
   public void setOrderLineId(Integer orderLineId) { this.orderLineId = orderLineId; }
   public WmsTask withOrderLineId(Integer orderLineId) { this.orderLineId = orderLineId; return (this); }

   public Integer getReceiptId() { return (this.receiptId); }
   public void setReceiptId(Integer receiptId) { this.receiptId = receiptId; }
   public WmsTask withReceiptId(Integer receiptId) { this.receiptId = receiptId; return (this); }

   public Integer getReceiptLineId() { return (this.receiptLineId); }
   public void setReceiptLineId(Integer receiptLineId) { this.receiptLineId = receiptLineId; }
   public WmsTask withReceiptLineId(Integer receiptLineId) { this.receiptLineId = receiptLineId; return (this); }

   public Integer getCycleCountId() { return (this.cycleCountId); }
   public void setCycleCountId(Integer cycleCountId) { this.cycleCountId = cycleCountId; }
   public WmsTask withCycleCountId(Integer cycleCountId) { this.cycleCountId = cycleCountId; return (this); }

   public Integer getReturnAuthorizationId() { return (this.returnAuthorizationId); }
   public void setReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; }
   public WmsTask withReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; return (this); }

   public BigDecimal getExpectedQuantity() { return (this.expectedQuantity); }
   public void setExpectedQuantity(BigDecimal expectedQuantity) { this.expectedQuantity = expectedQuantity; }
   public WmsTask withExpectedQuantity(BigDecimal expectedQuantity) { this.expectedQuantity = expectedQuantity; return (this); }

   public BigDecimal getCountedQuantity() { return (this.countedQuantity); }
   public void setCountedQuantity(BigDecimal countedQuantity) { this.countedQuantity = countedQuantity; }
   public WmsTask withCountedQuantity(BigDecimal countedQuantity) { this.countedQuantity = countedQuantity; return (this); }

   public BigDecimal getVariance() { return (this.variance); }
   public void setVariance(BigDecimal variance) { this.variance = variance; }
   public WmsTask withVariance(BigDecimal variance) { this.variance = variance; return (this); }

   public Boolean getIsBlindCount() { return (this.isBlindCount); }
   public void setIsBlindCount(Boolean isBlindCount) { this.isBlindCount = isBlindCount; }
   public WmsTask withIsBlindCount(Boolean isBlindCount) { this.isBlindCount = isBlindCount; return (this); }

   public Boolean getRecountRequired() { return (this.recountRequired); }
   public void setRecountRequired(Boolean recountRequired) { this.recountRequired = recountRequired; }
   public WmsTask withRecountRequired(Boolean recountRequired) { this.recountRequired = recountRequired; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsTask withNotes(String notes) { this.notes = notes; return (this); }

   public String getShortReason() { return (this.shortReason); }
   public void setShortReason(String shortReason) { this.shortReason = shortReason; }
   public WmsTask withShortReason(String shortReason) { this.shortReason = shortReason; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsTask withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsTask withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
