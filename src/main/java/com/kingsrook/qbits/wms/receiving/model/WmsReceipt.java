/*******************************************************************************
 ** QRecord Entity for WmsReceipt table -- represents a single receiving event,
 ** optionally linked to a purchase order.  Blind receipts have no PO reference.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReceipt.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsReceiptLine.class,
         joinFieldName = "receiptId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Receipt Lines", enabled = true, maxRows = 50))
   }
)
public class WmsReceipt extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReceipt";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsReceipt.TABLE_NAME, WmsReceiptLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("move_to_inbox"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("receiptNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "receiptNumber", "receiptTypeId", "statusId", "purchaseOrderId")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "receivedBy", "receivedDate")))
            .withSection(new QFieldSection("shipping", "Shipping", new QIcon("local_shipping"), Tier.T2,
               java.util.List.of("carrierName", "trailerNumber", "sealNumber")))
            .withSection(SectionFactory.customT2("receiptLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(new QFieldSection("notes", "Notes", new QIcon("notes"), Tier.T2,
               java.util.List.of("notes")))
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

   @QField(possibleValueSourceName = WmsPurchaseOrder.TABLE_NAME, label = "Purchase Order")
   private Integer purchaseOrderId;

   @QField(isRequired = true, maxLength = 50)
   private String receiptNumber;

   @QField(possibleValueSourceName = ReceiptType.NAME, label = "Receipt Type")
   private Integer receiptTypeId;

   @QField(possibleValueSourceName = ReceiptStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(maxLength = 100)
   private String receivedBy;

   @QField()
   private Instant receivedDate;

   @QField(maxLength = 100)
   private String carrierName;

   @QField(maxLength = 50)
   private String trailerNumber;

   @QField(maxLength = 50)
   private String sealNumber;

   @QField(maxLength = 2000)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReceipt()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReceipt(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReceipt withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsReceipt withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsReceipt withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getPurchaseOrderId() { return (this.purchaseOrderId); }
   public void setPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
   public WmsReceipt withPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return (this); }

   public String getReceiptNumber() { return (this.receiptNumber); }
   public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
   public WmsReceipt withReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; return (this); }

   public Integer getReceiptTypeId() { return (this.receiptTypeId); }
   public void setReceiptTypeId(Integer receiptTypeId) { this.receiptTypeId = receiptTypeId; }
   public WmsReceipt withReceiptTypeId(Integer receiptTypeId) { this.receiptTypeId = receiptTypeId; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsReceipt withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public String getReceivedBy() { return (this.receivedBy); }
   public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
   public WmsReceipt withReceivedBy(String receivedBy) { this.receivedBy = receivedBy; return (this); }

   public Instant getReceivedDate() { return (this.receivedDate); }
   public void setReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; }
   public WmsReceipt withReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; return (this); }

   public String getCarrierName() { return (this.carrierName); }
   public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
   public WmsReceipt withCarrierName(String carrierName) { this.carrierName = carrierName; return (this); }

   public String getTrailerNumber() { return (this.trailerNumber); }
   public void setTrailerNumber(String trailerNumber) { this.trailerNumber = trailerNumber; }
   public WmsReceipt withTrailerNumber(String trailerNumber) { this.trailerNumber = trailerNumber; return (this); }

   public String getSealNumber() { return (this.sealNumber); }
   public void setSealNumber(String sealNumber) { this.sealNumber = sealNumber; }
   public WmsReceipt withSealNumber(String sealNumber) { this.sealNumber = sealNumber; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsReceipt withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReceipt withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReceipt withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
