/*******************************************************************************
 ** QRecord Entity for WmsReplenishmentRule table -- defines the min/max
 ** thresholds for automatic replenishment of pick face locations from bulk
 ** storage zones.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.model;


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
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReplenishmentRule.TableMetaDataCustomizer.class
)
public class WmsReplenishmentRule extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReplenishmentRule";



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
            .withIcon(new QIcon().withName("autorenew"))
            .withRecordLabelFormat("Rule %s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "warehouseId", "itemId", "pickLocationId", "isActive")))
            .withSection(new QFieldSection("thresholds", "Thresholds", new QIcon("tune"), Tier.T2,
               java.util.List.of("minQuantity", "maxQuantity", "replenishmentUom", "priority")))
            .withSection(new QFieldSection("source", "Source", new QIcon("source"), Tier.T2,
               java.util.List.of("sourceZoneId")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true, possibleValueSourceName = WmsLocation.TABLE_NAME)
   private Integer pickLocationId;

   @QField(isRequired = true)
   private Integer minQuantity;

   @QField(isRequired = true)
   private Integer maxQuantity;

   @QField(maxLength = 20)
   private String replenishmentUom;

   @QField(possibleValueSourceName = WmsZone.TABLE_NAME)
   private Integer sourceZoneId;

   @QField()
   private Integer priority;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReplenishmentRule()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReplenishmentRule(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReplenishmentRule withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsReplenishmentRule withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsReplenishmentRule withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getPickLocationId() { return (this.pickLocationId); }
   public void setPickLocationId(Integer pickLocationId) { this.pickLocationId = pickLocationId; }
   public WmsReplenishmentRule withPickLocationId(Integer pickLocationId) { this.pickLocationId = pickLocationId; return (this); }

   public Integer getMinQuantity() { return (this.minQuantity); }
   public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
   public WmsReplenishmentRule withMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; return (this); }

   public Integer getMaxQuantity() { return (this.maxQuantity); }
   public void setMaxQuantity(Integer maxQuantity) { this.maxQuantity = maxQuantity; }
   public WmsReplenishmentRule withMaxQuantity(Integer maxQuantity) { this.maxQuantity = maxQuantity; return (this); }

   public String getReplenishmentUom() { return (this.replenishmentUom); }
   public void setReplenishmentUom(String replenishmentUom) { this.replenishmentUom = replenishmentUom; }
   public WmsReplenishmentRule withReplenishmentUom(String replenishmentUom) { this.replenishmentUom = replenishmentUom; return (this); }

   public Integer getSourceZoneId() { return (this.sourceZoneId); }
   public void setSourceZoneId(Integer sourceZoneId) { this.sourceZoneId = sourceZoneId; }
   public WmsReplenishmentRule withSourceZoneId(Integer sourceZoneId) { this.sourceZoneId = sourceZoneId; return (this); }

   public Integer getPriority() { return (this.priority); }
   public void setPriority(Integer priority) { this.priority = priority; }
   public WmsReplenishmentRule withPriority(Integer priority) { this.priority = priority; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsReplenishmentRule withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReplenishmentRule withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReplenishmentRule withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
