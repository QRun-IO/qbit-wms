/*******************************************************************************
 ** QRecord Entity for WmsWave table -- groups orders into a wave for batch
 ** picking and fulfillment processing.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


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
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import com.kingsrook.qbits.wms.core.enums.WaveType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsWave.TableMetaDataCustomizer.class
)
public class WmsWave extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsWave";



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
            .withIcon(new QIcon().withName("waves"))
            .withRecordLabelFormat("WAVE-%s")
            .withRecordLabelFields("waveNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "waveNumber", "statusId", "waveTypeId")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "carrierFilter", "serviceLevelFilter", "shipByFilter")))
            .withSection(new QFieldSection("timing", "Timing", new QIcon("schedule"), Tier.T2,
               java.util.List.of("plannedReleaseTime", "releasedDate", "completedDate")))
            .withSection(new QFieldSection("totals", "Totals", new QIcon("bar_chart"), Tier.T2,
               java.util.List.of("totalOrders", "totalLines", "totalUnits")))
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

   @QField(isRequired = true, maxLength = 50)
   private String waveNumber;

   @QField(possibleValueSourceName = WaveStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(possibleValueSourceName = WaveType.NAME, label = "Wave Type")
   private Integer waveTypeId;

   @QField(maxLength = 100)
   private String carrierFilter;

   @QField(maxLength = 50)
   private String serviceLevelFilter;

   @QField()
   private LocalDate shipByFilter;

   @QField()
   private Instant plannedReleaseTime;

   @QField()
   private Instant releasedDate;

   @QField()
   private Instant completedDate;

   @QField()
   private Integer totalOrders;

   @QField()
   private Integer totalLines;

   @QField()
   private Integer totalUnits;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsWave()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsWave(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsWave withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsWave withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsWave withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getWaveNumber() { return (this.waveNumber); }
   public void setWaveNumber(String waveNumber) { this.waveNumber = waveNumber; }
   public WmsWave withWaveNumber(String waveNumber) { this.waveNumber = waveNumber; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsWave withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Integer getWaveTypeId() { return (this.waveTypeId); }
   public void setWaveTypeId(Integer waveTypeId) { this.waveTypeId = waveTypeId; }
   public WmsWave withWaveTypeId(Integer waveTypeId) { this.waveTypeId = waveTypeId; return (this); }

   public String getCarrierFilter() { return (this.carrierFilter); }
   public void setCarrierFilter(String carrierFilter) { this.carrierFilter = carrierFilter; }
   public WmsWave withCarrierFilter(String carrierFilter) { this.carrierFilter = carrierFilter; return (this); }

   public String getServiceLevelFilter() { return (this.serviceLevelFilter); }
   public void setServiceLevelFilter(String serviceLevelFilter) { this.serviceLevelFilter = serviceLevelFilter; }
   public WmsWave withServiceLevelFilter(String serviceLevelFilter) { this.serviceLevelFilter = serviceLevelFilter; return (this); }

   public LocalDate getShipByFilter() { return (this.shipByFilter); }
   public void setShipByFilter(LocalDate shipByFilter) { this.shipByFilter = shipByFilter; }
   public WmsWave withShipByFilter(LocalDate shipByFilter) { this.shipByFilter = shipByFilter; return (this); }

   public Instant getPlannedReleaseTime() { return (this.plannedReleaseTime); }
   public void setPlannedReleaseTime(Instant plannedReleaseTime) { this.plannedReleaseTime = plannedReleaseTime; }
   public WmsWave withPlannedReleaseTime(Instant plannedReleaseTime) { this.plannedReleaseTime = plannedReleaseTime; return (this); }

   public Instant getReleasedDate() { return (this.releasedDate); }
   public void setReleasedDate(Instant releasedDate) { this.releasedDate = releasedDate; }
   public WmsWave withReleasedDate(Instant releasedDate) { this.releasedDate = releasedDate; return (this); }

   public Instant getCompletedDate() { return (this.completedDate); }
   public void setCompletedDate(Instant completedDate) { this.completedDate = completedDate; }
   public WmsWave withCompletedDate(Instant completedDate) { this.completedDate = completedDate; return (this); }

   public Integer getTotalOrders() { return (this.totalOrders); }
   public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
   public WmsWave withTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; return (this); }

   public Integer getTotalLines() { return (this.totalLines); }
   public void setTotalLines(Integer totalLines) { this.totalLines = totalLines; }
   public WmsWave withTotalLines(Integer totalLines) { this.totalLines = totalLines; return (this); }

   public Integer getTotalUnits() { return (this.totalUnits); }
   public void setTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; }
   public WmsWave withTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsWave withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsWave withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
