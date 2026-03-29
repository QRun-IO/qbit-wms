/*******************************************************************************
 ** QRecord Entity for WmsAllocationRule table -- defines rules that control
 ** how inventory is allocated to orders (strategy, zone preference, priority).
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
import com.kingsrook.qbits.wms.core.enums.AllocationStrategy;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsItemCategory;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsAllocationRule.TableMetaDataCustomizer.class
)
public class WmsAllocationRule extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsAllocationRule";



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
            .withIcon(new QIcon().withName("rule"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "warehouseId", "clientId", "itemCategoryId", "strategyId")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("zonePreference", "priority", "isActive")))
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

   @QField(possibleValueSourceName = WmsItemCategory.TABLE_NAME, label = "Item Category")
   private Integer itemCategoryId;

   @QField(possibleValueSourceName = AllocationStrategy.NAME, label = "Strategy")
   private Integer strategyId;

   @QField(maxLength = 100)
   private String zonePreference;

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
   public WmsAllocationRule()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsAllocationRule(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsAllocationRule withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsAllocationRule withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsAllocationRule withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getItemCategoryId() { return (this.itemCategoryId); }
   public void setItemCategoryId(Integer itemCategoryId) { this.itemCategoryId = itemCategoryId; }
   public WmsAllocationRule withItemCategoryId(Integer itemCategoryId) { this.itemCategoryId = itemCategoryId; return (this); }

   public Integer getStrategyId() { return (this.strategyId); }
   public void setStrategyId(Integer strategyId) { this.strategyId = strategyId; }
   public WmsAllocationRule withStrategyId(Integer strategyId) { this.strategyId = strategyId; return (this); }

   public String getZonePreference() { return (this.zonePreference); }
   public void setZonePreference(String zonePreference) { this.zonePreference = zonePreference; }
   public WmsAllocationRule withZonePreference(String zonePreference) { this.zonePreference = zonePreference; return (this); }

   public Integer getPriority() { return (this.priority); }
   public void setPriority(Integer priority) { this.priority = priority; }
   public WmsAllocationRule withPriority(Integer priority) { this.priority = priority; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsAllocationRule withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsAllocationRule withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsAllocationRule withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
