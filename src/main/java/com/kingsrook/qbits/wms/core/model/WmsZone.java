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



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsZone withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsZone withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsZone withName(String name) { this.name = name; return (this); }

   public String getCode() { return (this.code); }
   public void setCode(String code) { this.code = code; }
   public WmsZone withCode(String code) { this.code = code; return (this); }

   public Integer getZoneTypeId() { return (this.zoneTypeId); }
   public void setZoneTypeId(Integer zoneTypeId) { this.zoneTypeId = zoneTypeId; }
   public WmsZone withZoneTypeId(Integer zoneTypeId) { this.zoneTypeId = zoneTypeId; return (this); }

   public Integer getPickSequence() { return (this.pickSequence); }
   public void setPickSequence(Integer pickSequence) { this.pickSequence = pickSequence; }
   public WmsZone withPickSequence(Integer pickSequence) { this.pickSequence = pickSequence; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsZone withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsZone withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsZone withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
