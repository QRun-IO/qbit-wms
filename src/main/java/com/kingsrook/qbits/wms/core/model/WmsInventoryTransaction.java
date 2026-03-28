/*******************************************************************************
 ** QRecord Entity for WmsInventoryTransaction table (immutable ledger).
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.TransactionType;


@QMetaDataProducingEntity(producePossibleValueSource = true)
public class WmsInventoryTransaction extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsInventoryTransaction";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isEditable = false, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(isEditable = false, possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isEditable = false, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isEditable = false, possibleValueSourceName = TransactionType.NAME, label = "Transaction Type")
   private Integer transactionTypeId;

   @QField(isEditable = false, possibleValueSourceName = WmsLocation.TABLE_NAME, label = "From Location")
   private Integer fromLocationId;

   @QField(isEditable = false, possibleValueSourceName = WmsLocation.TABLE_NAME, label = "To Location")
   private Integer toLocationId;

   @QField(isEditable = false)
   private BigDecimal quantity;

   @QField(isEditable = false, maxLength = 50)
   private String lotNumber;

   @QField(isEditable = false, maxLength = 50)
   private String serialNumber;

   @QField(isEditable = false, possibleValueSourceName = WmsLicensePlate.TABLE_NAME)
   private Integer lpnId;

   @QField(isEditable = false, maxLength = 50)
   private String referenceType;

   @QField(isEditable = false)
   private Integer referenceId;

   @QField(isEditable = false)
   private Integer taskId;

   @QField(isEditable = false, maxLength = 100)
   private String reasonCode;

   @QField(isEditable = false, maxLength = 100)
   private String performedBy;

   @QField(isEditable = false)
   private Instant performedDate;

   @QField(isEditable = false, maxLength = 500)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsInventoryTransaction()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsInventoryTransaction(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsInventoryTransaction withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public WmsInventoryTransaction withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public WmsInventoryTransaction withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public WmsInventoryTransaction withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getTransactionTypeId() { return (this.transactionTypeId); }
   public WmsInventoryTransaction withTransactionTypeId(Integer transactionTypeId) { this.transactionTypeId = transactionTypeId; return (this); }

   public Integer getFromLocationId() { return (this.fromLocationId); }
   public WmsInventoryTransaction withFromLocationId(Integer fromLocationId) { this.fromLocationId = fromLocationId; return (this); }

   public Integer getToLocationId() { return (this.toLocationId); }
   public WmsInventoryTransaction withToLocationId(Integer toLocationId) { this.toLocationId = toLocationId; return (this); }

   public BigDecimal getQuantity() { return (this.quantity); }
   public WmsInventoryTransaction withQuantity(BigDecimal quantity) { this.quantity = quantity; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public WmsInventoryTransaction withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumber() { return (this.serialNumber); }
   public WmsInventoryTransaction withSerialNumber(String serialNumber) { this.serialNumber = serialNumber; return (this); }

   public Integer getLpnId() { return (this.lpnId); }
   public WmsInventoryTransaction withLpnId(Integer lpnId) { this.lpnId = lpnId; return (this); }

   public String getReferenceType() { return (this.referenceType); }
   public WmsInventoryTransaction withReferenceType(String referenceType) { this.referenceType = referenceType; return (this); }

   public Integer getReferenceId() { return (this.referenceId); }
   public WmsInventoryTransaction withReferenceId(Integer referenceId) { this.referenceId = referenceId; return (this); }

   public Integer getTaskId() { return (this.taskId); }
   public WmsInventoryTransaction withTaskId(Integer taskId) { this.taskId = taskId; return (this); }

   public String getReasonCode() { return (this.reasonCode); }
   public WmsInventoryTransaction withReasonCode(String reasonCode) { this.reasonCode = reasonCode; return (this); }

   public String getPerformedBy() { return (this.performedBy); }
   public WmsInventoryTransaction withPerformedBy(String performedBy) { this.performedBy = performedBy; return (this); }

   public Instant getPerformedDate() { return (this.performedDate); }
   public WmsInventoryTransaction withPerformedDate(Instant performedDate) { this.performedDate = performedDate; return (this); }

   public String getNotes() { return (this.notes); }
   public WmsInventoryTransaction withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsInventoryTransaction withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsInventoryTransaction withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
