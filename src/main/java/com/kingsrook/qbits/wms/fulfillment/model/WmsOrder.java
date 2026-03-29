/*******************************************************************************
 ** QRecord Entity for WmsOrder table -- represents an outbound customer order
 ** tracked through allocation, picking, packing, and shipping.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsOrder.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsOrderLine.class,
         joinFieldName = "orderId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Order Lines", enabled = true, maxRows = 50))
   }
)
public class WmsOrder extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsOrder";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsOrder.TABLE_NAME, WmsOrderLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("shopping_cart"))
            .withRecordLabelFormat("ORD-%s")
            .withRecordLabelFields("orderNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "orderNumber", "statusId", "priority", "orderSource")))
            .withSection(new QFieldSection("shipTo", "Ship To", new QIcon("local_shipping"), Tier.T2,
               java.util.List.of("shipToName", "shipToCompany", "shipToAddress1", "shipToAddress2", "shipToCity",
                  "shipToState", "shipToPostalCode", "shipToCountry", "shipToPhone", "shipToEmail", "isResidential")))
            .withSection(new QFieldSection("fulfillment", "Fulfillment", new QIcon("inventory"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "waveId", "allocatedDate", "pickedDate", "packedDate", "shippedDate")))
            .withSection(SectionFactory.customT2("orderLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("carrier", "serviceLevel", "shipByDate", "cancelDate", "externalOrderId",
                  "totalLineCount", "totalUnitCount", "orderValue", "specialInstructions")))
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
   private String orderNumber;

   @QField(maxLength = 100)
   private String externalOrderId;

   @QField(maxLength = 50)
   private String orderSource;

   @QField(possibleValueSourceName = OrderStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private Integer priority;

   @QField()
   private Instant shipByDate;

   @QField()
   private Instant cancelDate;

   @QField(maxLength = 100)
   private String carrier;

   @QField(maxLength = 50)
   private String serviceLevel;

   @QField(maxLength = 100)
   private String shipToName;

   @QField(maxLength = 100)
   private String shipToCompany;

   @QField(maxLength = 200)
   private String shipToAddress1;

   @QField(maxLength = 200)
   private String shipToAddress2;

   @QField(maxLength = 100)
   private String shipToCity;

   @QField(maxLength = 50)
   private String shipToState;

   @QField(maxLength = 20)
   private String shipToPostalCode;

   @QField(maxLength = 50)
   private String shipToCountry;

   @QField(maxLength = 30)
   private String shipToPhone;

   @QField(maxLength = 100)
   private String shipToEmail;

   @QField()
   private Boolean isResidential;

   @QField(maxLength = 2000)
   private String specialInstructions;

   @QField(possibleValueSourceName = WmsWave.TABLE_NAME)
   private Integer waveId;

   @QField()
   private Instant allocatedDate;

   @QField()
   private Instant pickedDate;

   @QField()
   private Instant packedDate;

   @QField()
   private Instant shippedDate;

   @QField()
   private Integer totalLineCount;

   @QField()
   private Integer totalUnitCount;

   @QField()
   private BigDecimal orderValue;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsOrder()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsOrder(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsOrder withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsOrder withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsOrder withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getOrderNumber() { return (this.orderNumber); }
   public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
   public WmsOrder withOrderNumber(String orderNumber) { this.orderNumber = orderNumber; return (this); }

   public String getExternalOrderId() { return (this.externalOrderId); }
   public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
   public WmsOrder withExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; return (this); }

   public String getOrderSource() { return (this.orderSource); }
   public void setOrderSource(String orderSource) { this.orderSource = orderSource; }
   public WmsOrder withOrderSource(String orderSource) { this.orderSource = orderSource; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsOrder withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Integer getPriority() { return (this.priority); }
   public void setPriority(Integer priority) { this.priority = priority; }
   public WmsOrder withPriority(Integer priority) { this.priority = priority; return (this); }

   public Instant getShipByDate() { return (this.shipByDate); }
   public void setShipByDate(Instant shipByDate) { this.shipByDate = shipByDate; }
   public WmsOrder withShipByDate(Instant shipByDate) { this.shipByDate = shipByDate; return (this); }

   public Instant getCancelDate() { return (this.cancelDate); }
   public void setCancelDate(Instant cancelDate) { this.cancelDate = cancelDate; }
   public WmsOrder withCancelDate(Instant cancelDate) { this.cancelDate = cancelDate; return (this); }

   public String getCarrier() { return (this.carrier); }
   public void setCarrier(String carrier) { this.carrier = carrier; }
   public WmsOrder withCarrier(String carrier) { this.carrier = carrier; return (this); }

   public String getServiceLevel() { return (this.serviceLevel); }
   public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
   public WmsOrder withServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; return (this); }

   public String getShipToName() { return (this.shipToName); }
   public void setShipToName(String shipToName) { this.shipToName = shipToName; }
   public WmsOrder withShipToName(String shipToName) { this.shipToName = shipToName; return (this); }

   public String getShipToCompany() { return (this.shipToCompany); }
   public void setShipToCompany(String shipToCompany) { this.shipToCompany = shipToCompany; }
   public WmsOrder withShipToCompany(String shipToCompany) { this.shipToCompany = shipToCompany; return (this); }

   public String getShipToAddress1() { return (this.shipToAddress1); }
   public void setShipToAddress1(String shipToAddress1) { this.shipToAddress1 = shipToAddress1; }
   public WmsOrder withShipToAddress1(String shipToAddress1) { this.shipToAddress1 = shipToAddress1; return (this); }

   public String getShipToAddress2() { return (this.shipToAddress2); }
   public void setShipToAddress2(String shipToAddress2) { this.shipToAddress2 = shipToAddress2; }
   public WmsOrder withShipToAddress2(String shipToAddress2) { this.shipToAddress2 = shipToAddress2; return (this); }

   public String getShipToCity() { return (this.shipToCity); }
   public void setShipToCity(String shipToCity) { this.shipToCity = shipToCity; }
   public WmsOrder withShipToCity(String shipToCity) { this.shipToCity = shipToCity; return (this); }

   public String getShipToState() { return (this.shipToState); }
   public void setShipToState(String shipToState) { this.shipToState = shipToState; }
   public WmsOrder withShipToState(String shipToState) { this.shipToState = shipToState; return (this); }

   public String getShipToPostalCode() { return (this.shipToPostalCode); }
   public void setShipToPostalCode(String shipToPostalCode) { this.shipToPostalCode = shipToPostalCode; }
   public WmsOrder withShipToPostalCode(String shipToPostalCode) { this.shipToPostalCode = shipToPostalCode; return (this); }

   public String getShipToCountry() { return (this.shipToCountry); }
   public void setShipToCountry(String shipToCountry) { this.shipToCountry = shipToCountry; }
   public WmsOrder withShipToCountry(String shipToCountry) { this.shipToCountry = shipToCountry; return (this); }

   public String getShipToPhone() { return (this.shipToPhone); }
   public void setShipToPhone(String shipToPhone) { this.shipToPhone = shipToPhone; }
   public WmsOrder withShipToPhone(String shipToPhone) { this.shipToPhone = shipToPhone; return (this); }

   public String getShipToEmail() { return (this.shipToEmail); }
   public void setShipToEmail(String shipToEmail) { this.shipToEmail = shipToEmail; }
   public WmsOrder withShipToEmail(String shipToEmail) { this.shipToEmail = shipToEmail; return (this); }

   public Boolean getIsResidential() { return (this.isResidential); }
   public void setIsResidential(Boolean isResidential) { this.isResidential = isResidential; }
   public WmsOrder withIsResidential(Boolean isResidential) { this.isResidential = isResidential; return (this); }

   public String getSpecialInstructions() { return (this.specialInstructions); }
   public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
   public WmsOrder withSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; return (this); }

   public Integer getWaveId() { return (this.waveId); }
   public void setWaveId(Integer waveId) { this.waveId = waveId; }
   public WmsOrder withWaveId(Integer waveId) { this.waveId = waveId; return (this); }

   public Instant getAllocatedDate() { return (this.allocatedDate); }
   public void setAllocatedDate(Instant allocatedDate) { this.allocatedDate = allocatedDate; }
   public WmsOrder withAllocatedDate(Instant allocatedDate) { this.allocatedDate = allocatedDate; return (this); }

   public Instant getPickedDate() { return (this.pickedDate); }
   public void setPickedDate(Instant pickedDate) { this.pickedDate = pickedDate; }
   public WmsOrder withPickedDate(Instant pickedDate) { this.pickedDate = pickedDate; return (this); }

   public Instant getPackedDate() { return (this.packedDate); }
   public void setPackedDate(Instant packedDate) { this.packedDate = packedDate; }
   public WmsOrder withPackedDate(Instant packedDate) { this.packedDate = packedDate; return (this); }

   public Instant getShippedDate() { return (this.shippedDate); }
   public void setShippedDate(Instant shippedDate) { this.shippedDate = shippedDate; }
   public WmsOrder withShippedDate(Instant shippedDate) { this.shippedDate = shippedDate; return (this); }

   public Integer getTotalLineCount() { return (this.totalLineCount); }
   public void setTotalLineCount(Integer totalLineCount) { this.totalLineCount = totalLineCount; }
   public WmsOrder withTotalLineCount(Integer totalLineCount) { this.totalLineCount = totalLineCount; return (this); }

   public Integer getTotalUnitCount() { return (this.totalUnitCount); }
   public void setTotalUnitCount(Integer totalUnitCount) { this.totalUnitCount = totalUnitCount; }
   public WmsOrder withTotalUnitCount(Integer totalUnitCount) { this.totalUnitCount = totalUnitCount; return (this); }

   public BigDecimal getOrderValue() { return (this.orderValue); }
   public void setOrderValue(BigDecimal orderValue) { this.orderValue = orderValue; }
   public WmsOrder withOrderValue(BigDecimal orderValue) { this.orderValue = orderValue; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsOrder withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsOrder withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
