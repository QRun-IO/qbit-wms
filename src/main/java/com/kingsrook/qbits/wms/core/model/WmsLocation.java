/*******************************************************************************
 ** QRecord Entity for WmsLocation table.
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.LocationType;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsLocation.TableMetaDataCustomizer.class
)
public class WmsLocation extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsLocation";



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
            .withIcon(new QIcon().withName("place"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("barcode")
            .withSection(SectionFactory.defaultT1("id", "warehouseId", "zoneId", "barcode", "label", "locationTypeId", "isActive"))
            .withSection(new QFieldSection("position", "Position", new QIcon("pin_drop"), QFieldSection.Tier.T2,
               java.util.List.of("aisle", "rack", "shelf", "position", "pickSequence")))
            .withSection(new QFieldSection("capacity", "Capacity", new QIcon("straighten"), QFieldSection.Tier.T2,
               java.util.List.of("maxWeightLbs", "maxVolumeCubicFt", "currentWeightLbs", "currentVolumeCubicFt", "isMixedSkuAllowed")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = "wmsZone")
   private Integer zoneId;

   @QField(isRequired = true, maxLength = 50)
   private String barcode;

   @QField(maxLength = 100)
   private String label;

   @QField(maxLength = 10)
   private String aisle;

   @QField(maxLength = 10)
   private String rack;

   @QField(maxLength = 10)
   private String shelf;

   @QField(maxLength = 10)
   private String position;

   @QField(possibleValueSourceName = LocationType.NAME, label = "Location Type")
   private Integer locationTypeId;

   @QField()
   private BigDecimal maxWeightLbs;

   @QField()
   private BigDecimal maxVolumeCubicFt;

   @QField()
   private BigDecimal currentWeightLbs;

   @QField()
   private BigDecimal currentVolumeCubicFt;

   @QField()
   private Boolean isMixedSkuAllowed;

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
   public WmsLocation()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsLocation(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsLocation withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public WmsLocation withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getZoneId() { return (this.zoneId); }
   public WmsLocation withZoneId(Integer zoneId) { this.zoneId = zoneId; return (this); }

   public String getBarcode() { return (this.barcode); }
   public WmsLocation withBarcode(String barcode) { this.barcode = barcode; return (this); }

   public String getLabel() { return (this.label); }
   public WmsLocation withLabel(String label) { this.label = label; return (this); }

   public String getAisle() { return (this.aisle); }
   public WmsLocation withAisle(String aisle) { this.aisle = aisle; return (this); }

   public String getRack() { return (this.rack); }
   public WmsLocation withRack(String rack) { this.rack = rack; return (this); }

   public String getShelf() { return (this.shelf); }
   public WmsLocation withShelf(String shelf) { this.shelf = shelf; return (this); }

   public String getPosition() { return (this.position); }
   public WmsLocation withPosition(String position) { this.position = position; return (this); }

   public Integer getLocationTypeId() { return (this.locationTypeId); }
   public WmsLocation withLocationTypeId(Integer locationTypeId) { this.locationTypeId = locationTypeId; return (this); }

   public BigDecimal getMaxWeightLbs() { return (this.maxWeightLbs); }
   public WmsLocation withMaxWeightLbs(BigDecimal maxWeightLbs) { this.maxWeightLbs = maxWeightLbs; return (this); }

   public BigDecimal getMaxVolumeCubicFt() { return (this.maxVolumeCubicFt); }
   public WmsLocation withMaxVolumeCubicFt(BigDecimal maxVolumeCubicFt) { this.maxVolumeCubicFt = maxVolumeCubicFt; return (this); }

   public BigDecimal getCurrentWeightLbs() { return (this.currentWeightLbs); }
   public WmsLocation withCurrentWeightLbs(BigDecimal currentWeightLbs) { this.currentWeightLbs = currentWeightLbs; return (this); }

   public BigDecimal getCurrentVolumeCubicFt() { return (this.currentVolumeCubicFt); }
   public WmsLocation withCurrentVolumeCubicFt(BigDecimal currentVolumeCubicFt) { this.currentVolumeCubicFt = currentVolumeCubicFt; return (this); }

   public Boolean getIsMixedSkuAllowed() { return (this.isMixedSkuAllowed); }
   public WmsLocation withIsMixedSkuAllowed(Boolean isMixedSkuAllowed) { this.isMixedSkuAllowed = isMixedSkuAllowed; return (this); }

   public Integer getPickSequence() { return (this.pickSequence); }
   public WmsLocation withPickSequence(Integer pickSequence) { this.pickSequence = pickSequence; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public WmsLocation withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsLocation withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsLocation withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
