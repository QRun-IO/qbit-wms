/*******************************************************************************
 ** QRecord Entity for WmsKitBom table -- defines the bill of materials for a
 ** kit item, mapping a finished kit to its component items and quantities.
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
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsKitBom.TableMetaDataCustomizer.class
)
public class WmsKitBom extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsKitBom";



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
            .withIcon(new QIcon().withName("account_tree"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "kitItemId", "componentItemId", "componentQuantity")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("componentUomId", "clientId")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME, label = "Kit Item")
   private Integer kitItemId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME, label = "Component Item")
   private Integer componentItemId;

   @QField(isRequired = true)
   private Integer componentQuantity;

   @QField(possibleValueSourceName = WmsUnitOfMeasure.TABLE_NAME, label = "Component UOM")
   private Integer componentUomId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsKitBom()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsKitBom(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsKitBom withId(Integer id) { this.id = id; return (this); }

   public Integer getKitItemId() { return (this.kitItemId); }
   public void setKitItemId(Integer kitItemId) { this.kitItemId = kitItemId; }
   public WmsKitBom withKitItemId(Integer kitItemId) { this.kitItemId = kitItemId; return (this); }

   public Integer getComponentItemId() { return (this.componentItemId); }
   public void setComponentItemId(Integer componentItemId) { this.componentItemId = componentItemId; }
   public WmsKitBom withComponentItemId(Integer componentItemId) { this.componentItemId = componentItemId; return (this); }

   public Integer getComponentQuantity() { return (this.componentQuantity); }
   public void setComponentQuantity(Integer componentQuantity) { this.componentQuantity = componentQuantity; }
   public WmsKitBom withComponentQuantity(Integer componentQuantity) { this.componentQuantity = componentQuantity; return (this); }

   public Integer getComponentUomId() { return (this.componentUomId); }
   public void setComponentUomId(Integer componentUomId) { this.componentUomId = componentUomId; }
   public WmsKitBom withComponentUomId(Integer componentUomId) { this.componentUomId = componentUomId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsKitBom withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsKitBom withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsKitBom withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
