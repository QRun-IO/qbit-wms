/*******************************************************************************
 ** QRecord Entity for WmsCarton table -- represents a physical shipping carton
 ** within an order, tracking dimensions, weight, and shipping label status.
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
import com.kingsrook.qbits.wms.core.enums.CartonStatus;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsCarton.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsCartonLine.class,
         joinFieldName = "cartonId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Carton Lines", enabled = true, maxRows = 50))
   }
)
public class WmsCarton extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsCarton";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsCarton.TABLE_NAME, WmsCartonLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("inventory_2"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("cartonNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "orderId", "cartonNumber", "cartonTypeId", "statusId")))
            .withSection(new QFieldSection("dimensions", "Dimensions", new QIcon("straighten"), Tier.T2,
               java.util.List.of("weightLbs", "lengthIn", "widthIn", "heightIn")))
            .withSection(new QFieldSection("shipping", "Shipping", new QIcon("local_shipping"), Tier.T2,
               java.util.List.of("trackingNumber", "labelPrinted")))
            .withSection(SectionFactory.customT2("cartonLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsOrder.TABLE_NAME)
   private Integer orderId;

   @QField(maxLength = 50)
   private String cartonNumber;

   @QField(possibleValueSourceName = WmsCartonType.TABLE_NAME, label = "Carton Type")
   private Integer cartonTypeId;

   @QField()
   private BigDecimal weightLbs;

   @QField()
   private BigDecimal lengthIn;

   @QField()
   private BigDecimal widthIn;

   @QField()
   private BigDecimal heightIn;

   @QField(maxLength = 100)
   private String trackingNumber;

   @QField()
   private Boolean labelPrinted;

   @QField(possibleValueSourceName = CartonStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsCarton()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsCarton(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsCarton withId(Integer id) { this.id = id; return (this); }

   public Integer getOrderId() { return (this.orderId); }
   public void setOrderId(Integer orderId) { this.orderId = orderId; }
   public WmsCarton withOrderId(Integer orderId) { this.orderId = orderId; return (this); }

   public String getCartonNumber() { return (this.cartonNumber); }
   public void setCartonNumber(String cartonNumber) { this.cartonNumber = cartonNumber; }
   public WmsCarton withCartonNumber(String cartonNumber) { this.cartonNumber = cartonNumber; return (this); }

   public Integer getCartonTypeId() { return (this.cartonTypeId); }
   public void setCartonTypeId(Integer cartonTypeId) { this.cartonTypeId = cartonTypeId; }
   public WmsCarton withCartonTypeId(Integer cartonTypeId) { this.cartonTypeId = cartonTypeId; return (this); }

   public BigDecimal getWeightLbs() { return (this.weightLbs); }
   public void setWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; }
   public WmsCarton withWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; return (this); }

   public BigDecimal getLengthIn() { return (this.lengthIn); }
   public void setLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; }
   public WmsCarton withLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; return (this); }

   public BigDecimal getWidthIn() { return (this.widthIn); }
   public void setWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; }
   public WmsCarton withWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; return (this); }

   public BigDecimal getHeightIn() { return (this.heightIn); }
   public void setHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; }
   public WmsCarton withHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; return (this); }

   public String getTrackingNumber() { return (this.trackingNumber); }
   public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
   public WmsCarton withTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return (this); }

   public Boolean getLabelPrinted() { return (this.labelPrinted); }
   public void setLabelPrinted(Boolean labelPrinted) { this.labelPrinted = labelPrinted; }
   public WmsCarton withLabelPrinted(Boolean labelPrinted) { this.labelPrinted = labelPrinted; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsCarton withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsCarton withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsCarton withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
