/*******************************************************************************
 ** QRecord Entity for WmsShipment table -- represents a shipping container or
 ** parcel being shipped from the warehouse, with carrier, tracking, and cost
 ** information.
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
import com.kingsrook.qbits.wms.core.enums.ShipmentStatus;
import com.kingsrook.qbits.wms.core.enums.ShippingMode;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsShipment.TableMetaDataCustomizer.class
)
public class WmsShipment extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsShipment";



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
            .withIcon(new QIcon().withName("local_shipping"))
            .withRecordLabelFormat("SHIP-%s")
            .withRecordLabelFields("shipmentNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "shipmentNumber", "statusId", "shippingModeId", "carrier", "serviceLevel")))
            .withSection(new QFieldSection("tracking", "Tracking", new QIcon("track_changes"), Tier.T2,
               java.util.List.of("trackingNumber", "shipDate", "estimatedDeliveryDate", "actualDeliveryDate")))
            .withSection(new QFieldSection("cost", "Cost", new QIcon("attach_money"), Tier.T2,
               java.util.List.of("shippingCost", "isInternational", "customsValue")))
            .withSection(new QFieldSection("warehouse", "Warehouse", new QIcon("warehouse"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "shipFromWarehouseId", "dockDoorId", "manifestId")))
            .withSection(new QFieldSection("documents", "Documents", new QIcon("description"), Tier.T2,
               java.util.List.of("labelUrl", "commercialInvoiceUrl")))
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
   private String shipmentNumber;

   @QField(maxLength = 100)
   private String carrier;

   @QField(maxLength = 50)
   private String serviceLevel;

   @QField(maxLength = 100)
   private String trackingNumber;

   @QField()
   private Instant shipDate;

   @QField()
   private LocalDate estimatedDeliveryDate;

   @QField()
   private LocalDate actualDeliveryDate;

   @QField()
   private BigDecimal shippingCost;

   @QField(possibleValueSourceName = ShipmentStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Dock Door")
   private Integer dockDoorId;

   @QField(possibleValueSourceName = WmsManifest.TABLE_NAME, label = "Manifest")
   private Integer manifestId;

   @QField(maxLength = 500)
   private String labelUrl;

   @QField(maxLength = 500)
   private String commercialInvoiceUrl;

   @QField()
   private Boolean isInternational;

   @QField()
   private BigDecimal customsValue;

   @QField(possibleValueSourceName = ShippingMode.NAME, label = "Shipping Mode")
   private Integer shippingModeId;

   @QField(possibleValueSourceName = WmsWarehouse.TABLE_NAME, label = "Ship From Warehouse")
   private Integer shipFromWarehouseId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsShipment()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsShipment(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsShipment withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsShipment withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsShipment withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getShipmentNumber() { return (this.shipmentNumber); }
   public void setShipmentNumber(String shipmentNumber) { this.shipmentNumber = shipmentNumber; }
   public WmsShipment withShipmentNumber(String shipmentNumber) { this.shipmentNumber = shipmentNumber; return (this); }

   public String getCarrier() { return (this.carrier); }
   public void setCarrier(String carrier) { this.carrier = carrier; }
   public WmsShipment withCarrier(String carrier) { this.carrier = carrier; return (this); }

   public String getServiceLevel() { return (this.serviceLevel); }
   public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
   public WmsShipment withServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; return (this); }

   public String getTrackingNumber() { return (this.trackingNumber); }
   public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
   public WmsShipment withTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return (this); }

   public Instant getShipDate() { return (this.shipDate); }
   public void setShipDate(Instant shipDate) { this.shipDate = shipDate; }
   public WmsShipment withShipDate(Instant shipDate) { this.shipDate = shipDate; return (this); }

   public LocalDate getEstimatedDeliveryDate() { return (this.estimatedDeliveryDate); }
   public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
   public WmsShipment withEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; return (this); }

   public LocalDate getActualDeliveryDate() { return (this.actualDeliveryDate); }
   public void setActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
   public WmsShipment withActualDeliveryDate(LocalDate actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; return (this); }

   public BigDecimal getShippingCost() { return (this.shippingCost); }
   public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }
   public WmsShipment withShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsShipment withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Integer getDockDoorId() { return (this.dockDoorId); }
   public void setDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; }
   public WmsShipment withDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; return (this); }

   public Integer getManifestId() { return (this.manifestId); }
   public void setManifestId(Integer manifestId) { this.manifestId = manifestId; }
   public WmsShipment withManifestId(Integer manifestId) { this.manifestId = manifestId; return (this); }

   public String getLabelUrl() { return (this.labelUrl); }
   public void setLabelUrl(String labelUrl) { this.labelUrl = labelUrl; }
   public WmsShipment withLabelUrl(String labelUrl) { this.labelUrl = labelUrl; return (this); }

   public String getCommercialInvoiceUrl() { return (this.commercialInvoiceUrl); }
   public void setCommercialInvoiceUrl(String commercialInvoiceUrl) { this.commercialInvoiceUrl = commercialInvoiceUrl; }
   public WmsShipment withCommercialInvoiceUrl(String commercialInvoiceUrl) { this.commercialInvoiceUrl = commercialInvoiceUrl; return (this); }

   public Boolean getIsInternational() { return (this.isInternational); }
   public void setIsInternational(Boolean isInternational) { this.isInternational = isInternational; }
   public WmsShipment withIsInternational(Boolean isInternational) { this.isInternational = isInternational; return (this); }

   public BigDecimal getCustomsValue() { return (this.customsValue); }
   public void setCustomsValue(BigDecimal customsValue) { this.customsValue = customsValue; }
   public WmsShipment withCustomsValue(BigDecimal customsValue) { this.customsValue = customsValue; return (this); }

   public Integer getShippingModeId() { return (this.shippingModeId); }
   public void setShippingModeId(Integer shippingModeId) { this.shippingModeId = shippingModeId; }
   public WmsShipment withShippingModeId(Integer shippingModeId) { this.shippingModeId = shippingModeId; return (this); }

   public Integer getShipFromWarehouseId() { return (this.shipFromWarehouseId); }
   public void setShipFromWarehouseId(Integer shipFromWarehouseId) { this.shipFromWarehouseId = shipFromWarehouseId; }
   public WmsShipment withShipFromWarehouseId(Integer shipFromWarehouseId) { this.shipFromWarehouseId = shipFromWarehouseId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsShipment withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsShipment withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
