/*******************************************************************************
 ** QRecord Entity for WmsManifest table -- represents a carrier manifest that
 ** groups multiple shipments for end-of-day closeout and carrier pickup.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import com.kingsrook.qbits.wms.core.enums.ManifestStatus;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsManifest.TableMetaDataCustomizer.class
)
public class WmsManifest extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsManifest";



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
            .withIcon(new QIcon().withName("receipt_long"))
            .withRecordLabelFormat("MAN-%s")
            .withRecordLabelFields("manifestNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "manifestNumber", "carrier", "statusId", "manifestDate")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("warehouseId", "totalShipments", "totalWeightLbs", "closedDate")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(maxLength = 100)
   private String carrier;

   @QField(maxLength = 50)
   private String manifestNumber;

   @QField()
   private LocalDate manifestDate;

   @QField()
   private Integer totalShipments;

   @QField()
   private BigDecimal totalWeightLbs;

   @QField(possibleValueSourceName = ManifestStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private Instant closedDate;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsManifest()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsManifest(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsManifest withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsManifest withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public String getCarrier() { return (this.carrier); }
   public void setCarrier(String carrier) { this.carrier = carrier; }
   public WmsManifest withCarrier(String carrier) { this.carrier = carrier; return (this); }

   public String getManifestNumber() { return (this.manifestNumber); }
   public void setManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; }
   public WmsManifest withManifestNumber(String manifestNumber) { this.manifestNumber = manifestNumber; return (this); }

   public LocalDate getManifestDate() { return (this.manifestDate); }
   public void setManifestDate(LocalDate manifestDate) { this.manifestDate = manifestDate; }
   public WmsManifest withManifestDate(LocalDate manifestDate) { this.manifestDate = manifestDate; return (this); }

   public Integer getTotalShipments() { return (this.totalShipments); }
   public void setTotalShipments(Integer totalShipments) { this.totalShipments = totalShipments; }
   public WmsManifest withTotalShipments(Integer totalShipments) { this.totalShipments = totalShipments; return (this); }

   public BigDecimal getTotalWeightLbs() { return (this.totalWeightLbs); }
   public void setTotalWeightLbs(BigDecimal totalWeightLbs) { this.totalWeightLbs = totalWeightLbs; }
   public WmsManifest withTotalWeightLbs(BigDecimal totalWeightLbs) { this.totalWeightLbs = totalWeightLbs; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsManifest withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getClosedDate() { return (this.closedDate); }
   public void setClosedDate(Instant closedDate) { this.closedDate = closedDate; }
   public WmsManifest withClosedDate(Instant closedDate) { this.closedDate = closedDate; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsManifest withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsManifest withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
