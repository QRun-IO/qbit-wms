/*******************************************************************************
 ** QRecord Entity for WmsZone table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.ZoneType;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsZone.TableMetaDataCustomizer.class
)
public class WmsZone extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsZone";



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
            .withIcon(new QIcon().withName("grid_view"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withSection(SectionFactory.defaultT1("id", "warehouseId", "name", "code", "zoneTypeId", "pickSequence", "isActive"))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField(isRequired = true, maxLength = 20)
   private String code;

   @QField(possibleValueSourceName = ZoneType.NAME, label = "Zone Type")
   private Integer zoneTypeId;

   @QField()
   private Integer pickSequence;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsZone()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsZone(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WmsZone withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for warehouseId
    *******************************************************************************/
   public Integer getWarehouseId()
   {
      return (this.warehouseId);
   }



   /*******************************************************************************
    ** Fluent setter for warehouseId
    *******************************************************************************/
   public WmsZone withWarehouseId(Integer warehouseId)
   {
      this.warehouseId = warehouseId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public WmsZone withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for code
    *******************************************************************************/
   public String getCode()
   {
      return (this.code);
   }



   /*******************************************************************************
    ** Fluent setter for code
    *******************************************************************************/
   public WmsZone withCode(String code)
   {
      this.code = code;
      return (this);
   }



   /*******************************************************************************
    ** Getter for zoneTypeId
    *******************************************************************************/
   public Integer getZoneTypeId()
   {
      return (this.zoneTypeId);
   }



   /*******************************************************************************
    ** Fluent setter for zoneTypeId
    *******************************************************************************/
   public WmsZone withZoneTypeId(Integer zoneTypeId)
   {
      this.zoneTypeId = zoneTypeId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pickSequence
    *******************************************************************************/
   public Integer getPickSequence()
   {
      return (this.pickSequence);
   }



   /*******************************************************************************
    ** Fluent setter for pickSequence
    *******************************************************************************/
   public WmsZone withPickSequence(Integer pickSequence)
   {
      this.pickSequence = pickSequence;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isActive
    *******************************************************************************/
   public Boolean getIsActive()
   {
      return (this.isActive);
   }



   /*******************************************************************************
    ** Fluent setter for isActive
    *******************************************************************************/
   public WmsZone withIsActive(Boolean isActive)
   {
      this.isActive = isActive;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    *******************************************************************************/
   public WmsZone withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    *******************************************************************************/
   public WmsZone withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }
}
