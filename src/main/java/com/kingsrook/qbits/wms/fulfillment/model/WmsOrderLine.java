/*******************************************************************************
 ** QRecord Entity for WmsOrderLine table -- represents an individual line item
 ** within a customer order, tracking quantities through the fulfillment pipeline.
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
import com.kingsrook.qbits.wms.core.enums.OrderLineStatus;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsOrderLine.TableMetaDataCustomizer.class
)
public class WmsOrderLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsOrderLine";



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
            .withIcon(new QIcon().withName("list_alt"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "orderId", "itemId", "lineNumber", "statusId")))
            .withSection(new QFieldSection("quantities", "Quantities", new QIcon("inventory_2"), Tier.T2,
               java.util.List.of("quantityOrdered", "quantityAllocated", "quantityPicked", "quantityPacked",
                  "quantityShipped", "quantityBackordered")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("uomId", "lotPreference")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsOrder.TABLE_NAME)
   private Integer orderId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer quantityOrdered;

   @QField()
   private Integer quantityAllocated;

   @QField()
   private Integer quantityPicked;

   @QField()
   private Integer quantityPacked;

   @QField()
   private Integer quantityShipped;

   @QField()
   private Integer quantityBackordered;

   @QField(possibleValueSourceName = WmsUnitOfMeasure.TABLE_NAME)
   private Integer uomId;

   @QField()
   private Integer lineNumber;

   @QField(maxLength = 50)
   private String lotPreference;

   @QField(possibleValueSourceName = OrderLineStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsOrderLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsOrderLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsOrderLine withId(Integer id) { this.id = id; return (this); }

   public Integer getOrderId() { return (this.orderId); }
   public void setOrderId(Integer orderId) { this.orderId = orderId; }
   public WmsOrderLine withOrderId(Integer orderId) { this.orderId = orderId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsOrderLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getQuantityOrdered() { return (this.quantityOrdered); }
   public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }
   public WmsOrderLine withQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; return (this); }

   public Integer getQuantityAllocated() { return (this.quantityAllocated); }
   public void setQuantityAllocated(Integer quantityAllocated) { this.quantityAllocated = quantityAllocated; }
   public WmsOrderLine withQuantityAllocated(Integer quantityAllocated) { this.quantityAllocated = quantityAllocated; return (this); }

   public Integer getQuantityPicked() { return (this.quantityPicked); }
   public void setQuantityPicked(Integer quantityPicked) { this.quantityPicked = quantityPicked; }
   public WmsOrderLine withQuantityPicked(Integer quantityPicked) { this.quantityPicked = quantityPicked; return (this); }

   public Integer getQuantityPacked() { return (this.quantityPacked); }
   public void setQuantityPacked(Integer quantityPacked) { this.quantityPacked = quantityPacked; }
   public WmsOrderLine withQuantityPacked(Integer quantityPacked) { this.quantityPacked = quantityPacked; return (this); }

   public Integer getQuantityShipped() { return (this.quantityShipped); }
   public void setQuantityShipped(Integer quantityShipped) { this.quantityShipped = quantityShipped; }
   public WmsOrderLine withQuantityShipped(Integer quantityShipped) { this.quantityShipped = quantityShipped; return (this); }

   public Integer getQuantityBackordered() { return (this.quantityBackordered); }
   public void setQuantityBackordered(Integer quantityBackordered) { this.quantityBackordered = quantityBackordered; }
   public WmsOrderLine withQuantityBackordered(Integer quantityBackordered) { this.quantityBackordered = quantityBackordered; return (this); }

   public Integer getUomId() { return (this.uomId); }
   public void setUomId(Integer uomId) { this.uomId = uomId; }
   public WmsOrderLine withUomId(Integer uomId) { this.uomId = uomId; return (this); }

   public Integer getLineNumber() { return (this.lineNumber); }
   public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
   public WmsOrderLine withLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return (this); }

   public String getLotPreference() { return (this.lotPreference); }
   public void setLotPreference(String lotPreference) { this.lotPreference = lotPreference; }
   public WmsOrderLine withLotPreference(String lotPreference) { this.lotPreference = lotPreference; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsOrderLine withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsOrderLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsOrderLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
