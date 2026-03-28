/*******************************************************************************
 ** QRecord Entity for WmsItem table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.StorageRequirements;
import com.kingsrook.qbits.wms.core.enums.VelocityClass;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsItem.TableMetaDataCustomizer.class
)
public class WmsItem extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsItem";



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
            .withIcon(new QIcon().withName("inventory_2"))
            .withRecordLabelFormat("%s - %s")
            .withRecordLabelFields("sku", "name")
            .withSection(SectionFactory.defaultT1("id", "clientId", "sku", "name", "description", "barcodeUpc", "barcodeSecondary", "itemCategoryId", "isActive"))
            .withSection(new QFieldSection("dimensions", "Dimensions", new QIcon("straighten"), Tier.T2,
               java.util.List.of("weightLbs", "lengthIn", "widthIn", "heightIn")))
            .withSection(new QFieldSection("packaging", "Packaging", new QIcon("all_inbox"), Tier.T2,
               java.util.List.of("baseUom", "unitsPerCase", "casesPerPallet")))
            .withSection(new QFieldSection("tracking", "Tracking", new QIcon("track_changes"), Tier.T2,
               java.util.List.of("isLotTracked", "isSerialTracked", "isExpirationTracked", "shelfLifeDays", "minRemainingShelfLifeDays")))
            .withSection(new QFieldSection("slotting", "Slotting", new QIcon("speed"), Tier.T2,
               java.util.List.of("velocityClassId", "storageRequirementsId", "reorderPoint", "reorderQuantity")))
            .withSection(new QFieldSection("media", "Media", new QIcon("image"), Tier.T2,
               java.util.List.of("imageUrl")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, maxLength = 50)
   private String sku;

   @QField(isRequired = true, maxLength = 200)
   private String name;

   @QField(maxLength = 1000)
   private String description;

   @QField(maxLength = 50)
   private String barcodeUpc;

   @QField(maxLength = 50)
   private String barcodeSecondary;

   @QField(possibleValueSourceName = WmsItemCategory.TABLE_NAME)
   private Integer itemCategoryId;

   @QField()
   private BigDecimal weightLbs;

   @QField()
   private BigDecimal lengthIn;

   @QField()
   private BigDecimal widthIn;

   @QField()
   private BigDecimal heightIn;

   @QField(maxLength = 20)
   private String baseUom;

   @QField()
   private Integer unitsPerCase;

   @QField()
   private Integer casesPerPallet;

   @QField()
   private Boolean isLotTracked;

   @QField()
   private Boolean isSerialTracked;

   @QField()
   private Boolean isExpirationTracked;

   @QField()
   private Integer shelfLifeDays;

   @QField()
   private Integer minRemainingShelfLifeDays;

   @QField(possibleValueSourceName = VelocityClass.NAME, label = "Velocity Class")
   private Integer velocityClassId;

   @QField(possibleValueSourceName = StorageRequirements.NAME, label = "Storage Requirements")
   private Integer storageRequirementsId;

   @QField()
   private Integer reorderPoint;

   @QField()
   private Integer reorderQuantity;

   @QField()
   private Boolean isActive;

   @QField(maxLength = 500)
   private String imageUrl;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsItem()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsItem(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsItem withId(Integer id) { this.id = id; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsItem withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getSku() { return (this.sku); }
   public void setSku(String sku) { this.sku = sku; }
   public WmsItem withSku(String sku) { this.sku = sku; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsItem withName(String name) { this.name = name; return (this); }

   public String getDescription() { return (this.description); }
   public void setDescription(String description) { this.description = description; }
   public WmsItem withDescription(String description) { this.description = description; return (this); }

   public String getBarcodeUpc() { return (this.barcodeUpc); }
   public void setBarcodeUpc(String barcodeUpc) { this.barcodeUpc = barcodeUpc; }
   public WmsItem withBarcodeUpc(String barcodeUpc) { this.barcodeUpc = barcodeUpc; return (this); }

   public String getBarcodeSecondary() { return (this.barcodeSecondary); }
   public void setBarcodeSecondary(String barcodeSecondary) { this.barcodeSecondary = barcodeSecondary; }
   public WmsItem withBarcodeSecondary(String barcodeSecondary) { this.barcodeSecondary = barcodeSecondary; return (this); }

   public Integer getItemCategoryId() { return (this.itemCategoryId); }
   public void setItemCategoryId(Integer itemCategoryId) { this.itemCategoryId = itemCategoryId; }
   public WmsItem withItemCategoryId(Integer itemCategoryId) { this.itemCategoryId = itemCategoryId; return (this); }

   public BigDecimal getWeightLbs() { return (this.weightLbs); }
   public void setWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; }
   public WmsItem withWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; return (this); }

   public BigDecimal getLengthIn() { return (this.lengthIn); }
   public void setLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; }
   public WmsItem withLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; return (this); }

   public BigDecimal getWidthIn() { return (this.widthIn); }
   public void setWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; }
   public WmsItem withWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; return (this); }

   public BigDecimal getHeightIn() { return (this.heightIn); }
   public void setHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; }
   public WmsItem withHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; return (this); }

   public String getBaseUom() { return (this.baseUom); }
   public void setBaseUom(String baseUom) { this.baseUom = baseUom; }
   public WmsItem withBaseUom(String baseUom) { this.baseUom = baseUom; return (this); }

   public Integer getUnitsPerCase() { return (this.unitsPerCase); }
   public void setUnitsPerCase(Integer unitsPerCase) { this.unitsPerCase = unitsPerCase; }
   public WmsItem withUnitsPerCase(Integer unitsPerCase) { this.unitsPerCase = unitsPerCase; return (this); }

   public Integer getCasesPerPallet() { return (this.casesPerPallet); }
   public void setCasesPerPallet(Integer casesPerPallet) { this.casesPerPallet = casesPerPallet; }
   public WmsItem withCasesPerPallet(Integer casesPerPallet) { this.casesPerPallet = casesPerPallet; return (this); }

   public Boolean getIsLotTracked() { return (this.isLotTracked); }
   public void setIsLotTracked(Boolean isLotTracked) { this.isLotTracked = isLotTracked; }
   public WmsItem withIsLotTracked(Boolean isLotTracked) { this.isLotTracked = isLotTracked; return (this); }

   public Boolean getIsSerialTracked() { return (this.isSerialTracked); }
   public void setIsSerialTracked(Boolean isSerialTracked) { this.isSerialTracked = isSerialTracked; }
   public WmsItem withIsSerialTracked(Boolean isSerialTracked) { this.isSerialTracked = isSerialTracked; return (this); }

   public Boolean getIsExpirationTracked() { return (this.isExpirationTracked); }
   public void setIsExpirationTracked(Boolean isExpirationTracked) { this.isExpirationTracked = isExpirationTracked; }
   public WmsItem withIsExpirationTracked(Boolean isExpirationTracked) { this.isExpirationTracked = isExpirationTracked; return (this); }

   public Integer getShelfLifeDays() { return (this.shelfLifeDays); }
   public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
   public WmsItem withShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; return (this); }

   public Integer getMinRemainingShelfLifeDays() { return (this.minRemainingShelfLifeDays); }
   public void setMinRemainingShelfLifeDays(Integer minRemainingShelfLifeDays) { this.minRemainingShelfLifeDays = minRemainingShelfLifeDays; }
   public WmsItem withMinRemainingShelfLifeDays(Integer minRemainingShelfLifeDays) { this.minRemainingShelfLifeDays = minRemainingShelfLifeDays; return (this); }

   public Integer getVelocityClassId() { return (this.velocityClassId); }
   public void setVelocityClassId(Integer velocityClassId) { this.velocityClassId = velocityClassId; }
   public WmsItem withVelocityClassId(Integer velocityClassId) { this.velocityClassId = velocityClassId; return (this); }

   public Integer getStorageRequirementsId() { return (this.storageRequirementsId); }
   public void setStorageRequirementsId(Integer storageRequirementsId) { this.storageRequirementsId = storageRequirementsId; }
   public WmsItem withStorageRequirementsId(Integer storageRequirementsId) { this.storageRequirementsId = storageRequirementsId; return (this); }

   public Integer getReorderPoint() { return (this.reorderPoint); }
   public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }
   public WmsItem withReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; return (this); }

   public Integer getReorderQuantity() { return (this.reorderQuantity); }
   public void setReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; }
   public WmsItem withReorderQuantity(Integer reorderQuantity) { this.reorderQuantity = reorderQuantity; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsItem withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public String getImageUrl() { return (this.imageUrl); }
   public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
   public WmsItem withImageUrl(String imageUrl) { this.imageUrl = imageUrl; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsItem withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsItem withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
