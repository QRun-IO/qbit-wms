/*******************************************************************************
 ** QRecord Entity for WmsReceiptLine table -- individual items received on a
 ** receipt, with quantity, condition, QC status, and lot/serial tracking.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.ConditionCode;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLicensePlate;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsReceiptLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReceiptLine";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsReceipt.TABLE_NAME)
   private Integer receiptId;

   @QField(possibleValueSourceName = WmsPurchaseOrderLine.TABLE_NAME, label = "PO Line")
   private Integer purchaseOrderLineId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer quantityReceived;

   @QField(defaultValue = "0")
   private Integer quantityDamaged;

   @QField(defaultValue = "0")
   private Integer quantityRejected;

   @QField(possibleValueSourceName = WmsUnitOfMeasure.TABLE_NAME, label = "UOM")
   private Integer uomId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(maxLength = 2000)
   private String serialNumbers;

   @QField()
   private LocalDate expirationDate;

   @QField()
   private LocalDate manufactureDate;

   @QField(possibleValueSourceName = ConditionCode.NAME, label = "Condition")
   private Integer conditionId;

   @QField(possibleValueSourceName = WmsLicensePlate.TABLE_NAME, label = "LPN")
   private Integer lpnId;

   @QField(possibleValueSourceName = QcStatus.NAME, defaultValue = "4", label = "QC Status")
   private Integer qcStatusId;

   @QField(possibleValueSourceName = ReceiptStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReceiptLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReceiptLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReceiptLine withId(Integer id) { this.id = id; return (this); }

   public Integer getReceiptId() { return (this.receiptId); }
   public void setReceiptId(Integer receiptId) { this.receiptId = receiptId; }
   public WmsReceiptLine withReceiptId(Integer receiptId) { this.receiptId = receiptId; return (this); }

   public Integer getPurchaseOrderLineId() { return (this.purchaseOrderLineId); }
   public void setPurchaseOrderLineId(Integer purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; }
   public WmsReceiptLine withPurchaseOrderLineId(Integer purchaseOrderLineId) { this.purchaseOrderLineId = purchaseOrderLineId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsReceiptLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getQuantityReceived() { return (this.quantityReceived); }
   public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
   public WmsReceiptLine withQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return (this); }

   public Integer getQuantityDamaged() { return (this.quantityDamaged); }
   public void setQuantityDamaged(Integer quantityDamaged) { this.quantityDamaged = quantityDamaged; }
   public WmsReceiptLine withQuantityDamaged(Integer quantityDamaged) { this.quantityDamaged = quantityDamaged; return (this); }

   public Integer getQuantityRejected() { return (this.quantityRejected); }
   public void setQuantityRejected(Integer quantityRejected) { this.quantityRejected = quantityRejected; }
   public WmsReceiptLine withQuantityRejected(Integer quantityRejected) { this.quantityRejected = quantityRejected; return (this); }

   public Integer getUomId() { return (this.uomId); }
   public void setUomId(Integer uomId) { this.uomId = uomId; }
   public WmsReceiptLine withUomId(Integer uomId) { this.uomId = uomId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsReceiptLine withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumbers() { return (this.serialNumbers); }
   public void setSerialNumbers(String serialNumbers) { this.serialNumbers = serialNumbers; }
   public WmsReceiptLine withSerialNumbers(String serialNumbers) { this.serialNumbers = serialNumbers; return (this); }

   public LocalDate getExpirationDate() { return (this.expirationDate); }
   public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
   public WmsReceiptLine withExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; return (this); }

   public LocalDate getManufactureDate() { return (this.manufactureDate); }
   public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
   public WmsReceiptLine withManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; return (this); }

   public Integer getConditionId() { return (this.conditionId); }
   public void setConditionId(Integer conditionId) { this.conditionId = conditionId; }
   public WmsReceiptLine withConditionId(Integer conditionId) { this.conditionId = conditionId; return (this); }

   public Integer getLpnId() { return (this.lpnId); }
   public void setLpnId(Integer lpnId) { this.lpnId = lpnId; }
   public WmsReceiptLine withLpnId(Integer lpnId) { this.lpnId = lpnId; return (this); }

   public Integer getQcStatusId() { return (this.qcStatusId); }
   public void setQcStatusId(Integer qcStatusId) { this.qcStatusId = qcStatusId; }
   public WmsReceiptLine withQcStatusId(Integer qcStatusId) { this.qcStatusId = qcStatusId; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsReceiptLine withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReceiptLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReceiptLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
