/*******************************************************************************
 ** QRecord Entity for WmsKitWorkOrder table -- represents a work order for
 ** assembling or disassembling kits, tracking the kit item, quantity, and
 ** progress through the assembly process.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


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
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qbits.wms.core.enums.KitWorkOrderStatus;
import com.kingsrook.qbits.wms.core.enums.KitWorkOrderType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsKitWorkOrder.TableMetaDataCustomizer.class
)
public class WmsKitWorkOrder extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsKitWorkOrder";



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
            .withIcon(new QIcon().withName("build"))
            .withRecordLabelFormat("KWO-%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "kitItemId", "quantity", "workOrderTypeId", "statusId")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "assignedTo", "completedDate")))
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

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME, label = "Kit Item")
   private Integer kitItemId;

   @QField(isRequired = true)
   private Integer quantity;

   @QField(possibleValueSourceName = KitWorkOrderType.NAME, label = "Type")
   private Integer workOrderTypeId;

   @QField(possibleValueSourceName = KitWorkOrderStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(maxLength = 100)
   private String assignedTo;

   @QField()
   private Instant completedDate;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsKitWorkOrder()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsKitWorkOrder(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsKitWorkOrder withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsKitWorkOrder withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsKitWorkOrder withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getKitItemId() { return (this.kitItemId); }
   public void setKitItemId(Integer kitItemId) { this.kitItemId = kitItemId; }
   public WmsKitWorkOrder withKitItemId(Integer kitItemId) { this.kitItemId = kitItemId; return (this); }

   public Integer getQuantity() { return (this.quantity); }
   public void setQuantity(Integer quantity) { this.quantity = quantity; }
   public WmsKitWorkOrder withQuantity(Integer quantity) { this.quantity = quantity; return (this); }

   public Integer getWorkOrderTypeId() { return (this.workOrderTypeId); }
   public void setWorkOrderTypeId(Integer workOrderTypeId) { this.workOrderTypeId = workOrderTypeId; }
   public WmsKitWorkOrder withWorkOrderTypeId(Integer workOrderTypeId) { this.workOrderTypeId = workOrderTypeId; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsKitWorkOrder withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public String getAssignedTo() { return (this.assignedTo); }
   public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
   public WmsKitWorkOrder withAssignedTo(String assignedTo) { this.assignedTo = assignedTo; return (this); }

   public Instant getCompletedDate() { return (this.completedDate); }
   public void setCompletedDate(Instant completedDate) { this.completedDate = completedDate; }
   public WmsKitWorkOrder withCompletedDate(Instant completedDate) { this.completedDate = completedDate; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsKitWorkOrder withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsKitWorkOrder withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
