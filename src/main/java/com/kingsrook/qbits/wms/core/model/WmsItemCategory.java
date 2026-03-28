/*******************************************************************************
 ** QRecord Entity for WmsItemCategory table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.StorageRequirements;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsItemCategory extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsItemCategory";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField(possibleValueSourceName = "wmsItemCategory")
   private Integer parentCategoryId;

   @QField(possibleValueSourceName = StorageRequirements.NAME, label = "Default Storage Requirements")
   private Integer defaultStorageRequirementsId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsItemCategory()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsItemCategory(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsItemCategory withId(Integer id) { this.id = id; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsItemCategory withName(String name) { this.name = name; return (this); }

   public Integer getParentCategoryId() { return (this.parentCategoryId); }
   public void setParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; }
   public WmsItemCategory withParentCategoryId(Integer parentCategoryId) { this.parentCategoryId = parentCategoryId; return (this); }

   public Integer getDefaultStorageRequirementsId() { return (this.defaultStorageRequirementsId); }
   public void setDefaultStorageRequirementsId(Integer defaultStorageRequirementsId) { this.defaultStorageRequirementsId = defaultStorageRequirementsId; }
   public WmsItemCategory withDefaultStorageRequirementsId(Integer defaultStorageRequirementsId) { this.defaultStorageRequirementsId = defaultStorageRequirementsId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsItemCategory withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsItemCategory withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
