/*******************************************************************************
 ** QRecord Entity for WmsShipmentOrder table -- junction table linking
 ** shipments to orders, supporting split shipments and order consolidation.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


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
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsShipmentOrder.TableMetaDataCustomizer.class
)
public class WmsShipmentOrder extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsShipmentOrder";



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
            .withIcon(new QIcon().withName("link"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "shipmentId", "orderId")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsShipment.TABLE_NAME)
   private Integer shipmentId;

   @QField(isRequired = true, possibleValueSourceName = WmsOrder.TABLE_NAME)
   private Integer orderId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsShipmentOrder()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsShipmentOrder(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsShipmentOrder withId(Integer id) { this.id = id; return (this); }

   public Integer getShipmentId() { return (this.shipmentId); }
   public void setShipmentId(Integer shipmentId) { this.shipmentId = shipmentId; }
   public WmsShipmentOrder withShipmentId(Integer shipmentId) { this.shipmentId = shipmentId; return (this); }

   public Integer getOrderId() { return (this.orderId); }
   public void setOrderId(Integer orderId) { this.orderId = orderId; }
   public WmsShipmentOrder withOrderId(Integer orderId) { this.orderId = orderId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsShipmentOrder withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsShipmentOrder withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
