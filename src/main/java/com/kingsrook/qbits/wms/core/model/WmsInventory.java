/*******************************************************************************
 ** QRecord Entity for WmsInventory table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


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
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsInventory.TableMetaDataCustomizer.class
)
public class WmsInventory extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsInventory";



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
            .withIcon(new QIcon().withName("inventory"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(SectionFactory.defaultT1("id", "warehouseId", "clientId", "itemId", "locationId", "inventoryStatusId"))
            .withSection(new QFieldSection("quantities", "Quantities", new QIcon("calculate"), QFieldSection.Tier.T2,
               java.util.List.of("quantityOnHand", "quantityAllocated", "quantityAvailable", "quantityOnHold")))
            .withSection(new QFieldSection("tracking", "Tracking", new QIcon("track_changes"), QFieldSection.Tier.T2,
               java.util.List.of("lotNumber", "serialNumber", "expirationDate", "manufactureDate", "lpnId")))
            .withSection(new QFieldSection("receiving", "Receiving", new QIcon("local_shipping"), QFieldSection.Tier.T2,
               java.util.List.of("receiptId", "receivedDate", "costPerUnit")))
            .withSection(new QFieldSection("audit", "Audit", new QIcon("fact_check"), QFieldSection.Tier.T2,
               java.util.List.of("holdReason", "lastCountDate")))
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

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true, possibleValueSourceName = WmsLocation.TABLE_NAME)
   private Integer locationId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(maxLength = 50)
   private String serialNumber;

   @QField()
   private LocalDate expirationDate;

   @QField()
   private LocalDate manufactureDate;

   @QField(possibleValueSourceName = WmsLicensePlate.TABLE_NAME)
   private Integer lpnId;

   @QField(isRequired = true)
   private BigDecimal quantityOnHand;

   @QField()
   private BigDecimal quantityAllocated;

   @QField()
   private BigDecimal quantityAvailable;

   @QField()
   private BigDecimal quantityOnHold;

   @QField(possibleValueSourceName = InventoryStatus.NAME, label = "Inventory Status")
   private Integer inventoryStatusId;

   @QField(maxLength = 250)
   private String holdReason;

   @QField()
   private Integer receiptId;

   @QField()
   private Instant receivedDate;

   @QField()
   private Instant lastCountDate;

   @QField()
   private BigDecimal costPerUnit;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsInventory()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsInventory(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsInventory withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public WmsInventory withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public WmsInventory withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public WmsInventory withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getLocationId() { return (this.locationId); }
   public WmsInventory withLocationId(Integer locationId) { this.locationId = locationId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public WmsInventory withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumber() { return (this.serialNumber); }
   public WmsInventory withSerialNumber(String serialNumber) { this.serialNumber = serialNumber; return (this); }

   public LocalDate getExpirationDate() { return (this.expirationDate); }
   public WmsInventory withExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; return (this); }

   public LocalDate getManufactureDate() { return (this.manufactureDate); }
   public WmsInventory withManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; return (this); }

   public Integer getLpnId() { return (this.lpnId); }
   public WmsInventory withLpnId(Integer lpnId) { this.lpnId = lpnId; return (this); }

   public BigDecimal getQuantityOnHand() { return (this.quantityOnHand); }
   public WmsInventory withQuantityOnHand(BigDecimal quantityOnHand) { this.quantityOnHand = quantityOnHand; return (this); }

   public BigDecimal getQuantityAllocated() { return (this.quantityAllocated); }
   public WmsInventory withQuantityAllocated(BigDecimal quantityAllocated) { this.quantityAllocated = quantityAllocated; return (this); }

   public BigDecimal getQuantityAvailable() { return (this.quantityAvailable); }
   public WmsInventory withQuantityAvailable(BigDecimal quantityAvailable) { this.quantityAvailable = quantityAvailable; return (this); }

   public BigDecimal getQuantityOnHold() { return (this.quantityOnHold); }
   public WmsInventory withQuantityOnHold(BigDecimal quantityOnHold) { this.quantityOnHold = quantityOnHold; return (this); }

   public Integer getInventoryStatusId() { return (this.inventoryStatusId); }
   public WmsInventory withInventoryStatusId(Integer inventoryStatusId) { this.inventoryStatusId = inventoryStatusId; return (this); }

   public String getHoldReason() { return (this.holdReason); }
   public WmsInventory withHoldReason(String holdReason) { this.holdReason = holdReason; return (this); }

   public Integer getReceiptId() { return (this.receiptId); }
   public WmsInventory withReceiptId(Integer receiptId) { this.receiptId = receiptId; return (this); }

   public Instant getReceivedDate() { return (this.receivedDate); }
   public WmsInventory withReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; return (this); }

   public Instant getLastCountDate() { return (this.lastCountDate); }
   public WmsInventory withLastCountDate(Instant lastCountDate) { this.lastCountDate = lastCountDate; return (this); }

   public BigDecimal getCostPerUnit() { return (this.costPerUnit); }
   public WmsInventory withCostPerUnit(BigDecimal costPerUnit) { this.costPerUnit = costPerUnit; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsInventory withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsInventory withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
