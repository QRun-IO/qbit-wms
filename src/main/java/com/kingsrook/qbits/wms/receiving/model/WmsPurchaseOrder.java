/*******************************************************************************
 ** QRecord Entity for WmsPurchaseOrder table -- represents an inbound purchase
 ** order from a vendor, tracking expected and actual receipt of goods.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
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
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsVendor;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsPurchaseOrder.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsPurchaseOrderLine.class,
         joinFieldName = "purchaseOrderId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "PO Lines", enabled = true, maxRows = 50))
   }
)
public class WmsPurchaseOrder extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsPurchaseOrder";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsPurchaseOrder.TABLE_NAME, WmsPurchaseOrderLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("receipt_long"))
            .withRecordLabelFormat("PO-%s")
            .withRecordLabelFields("poNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "poNumber", "statusId", "vendorId")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "expectedDeliveryDate", "actualDeliveryDate", "dockDoorId")))
            .withSection(SectionFactory.customT2("poLines", new QIcon("list")).withWidgetName(lineChildJoinName))
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

   @QField(isRequired = true, possibleValueSourceName = WmsVendor.TABLE_NAME)
   private Integer vendorId;

   @QField(isRequired = true, maxLength = 50)
   private String poNumber;

   @QField(possibleValueSourceName = PurchaseOrderStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private LocalDate expectedDeliveryDate;

   @QField()
   private LocalDate actualDeliveryDate;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Dock Door")
   private Integer dockDoorId;

   @QField(maxLength = 2000)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsPurchaseOrder()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsPurchaseOrder(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsPurchaseOrder withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsPurchaseOrder withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsPurchaseOrder withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getVendorId() { return (this.vendorId); }
   public void setVendorId(Integer vendorId) { this.vendorId = vendorId; }
   public WmsPurchaseOrder withVendorId(Integer vendorId) { this.vendorId = vendorId; return (this); }

   public String getPoNumber() { return (this.poNumber); }
   public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
   public WmsPurchaseOrder withPoNumber(String poNumber) { this.poNumber = poNumber; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsPurchaseOrder withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public LocalDate getExpectedDeliveryDate() { return (this.expectedDeliveryDate); }
   public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
   public WmsPurchaseOrder withExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; return (this); }

   public LocalDate getActualDeliveryDate() { return (this.actualDeliveryDate); }
   public void setActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
   public WmsPurchaseOrder withActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; return (this); }

   public Integer getDockDoorId() { return (this.dockDoorId); }
   public void setDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; }
   public WmsPurchaseOrder withDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsPurchaseOrder withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsPurchaseOrder withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsPurchaseOrder withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
