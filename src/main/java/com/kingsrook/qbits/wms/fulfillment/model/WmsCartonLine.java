/*******************************************************************************
 ** QRecord Entity for WmsCartonLine table -- represents an individual item line
 ** packed into a carton, with optional lot and serial tracking.
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
import com.kingsrook.qbits.wms.core.model.WmsItem;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsCartonLine.TableMetaDataCustomizer.class
)
public class WmsCartonLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsCartonLine";



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
            .withIcon(new QIcon().withName("view_in_ar"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "cartonId", "orderLineId", "itemId", "quantity")))
            .withSection(new QFieldSection("tracking", "Tracking", new QIcon("track_changes"), Tier.T2,
               java.util.List.of("lotNumber", "serialNumber")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsCarton.TABLE_NAME)
   private Integer cartonId;

   @QField()
   private Integer orderLineId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer quantity;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(maxLength = 50)
   private String serialNumber;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsCartonLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsCartonLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsCartonLine withId(Integer id) { this.id = id; return (this); }

   public Integer getCartonId() { return (this.cartonId); }
   public void setCartonId(Integer cartonId) { this.cartonId = cartonId; }
   public WmsCartonLine withCartonId(Integer cartonId) { this.cartonId = cartonId; return (this); }

   public Integer getOrderLineId() { return (this.orderLineId); }
   public void setOrderLineId(Integer orderLineId) { this.orderLineId = orderLineId; }
   public WmsCartonLine withOrderLineId(Integer orderLineId) { this.orderLineId = orderLineId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsCartonLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getQuantity() { return (this.quantity); }
   public void setQuantity(Integer quantity) { this.quantity = quantity; }
   public WmsCartonLine withQuantity(Integer quantity) { this.quantity = quantity; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsCartonLine withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumber() { return (this.serialNumber); }
   public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
   public WmsCartonLine withSerialNumber(String serialNumber) { this.serialNumber = serialNumber; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsCartonLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsCartonLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
