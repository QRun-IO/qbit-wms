/*******************************************************************************
 ** QRecord Entity for WmsAsn table -- an Advanced Shipping Notice linked to a
 ** purchase order, providing pre-arrival visibility of inbound shipments.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


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
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.AsnStatus;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsAsn.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsAsnLine.class,
         joinFieldName = "asnId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "ASN Lines", enabled = true, maxRows = 50))
   }
)
public class WmsAsn extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsAsn";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsAsn.TABLE_NAME, WmsAsnLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("local_shipping"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("asnNumber")
            .withSection(SectionFactory.defaultT1("id", "asnNumber", "purchaseOrderId", "statusId"))
            .withSection(SectionFactory.defaultT2("carrierName", "trackingNumber", "expectedArrivalDate"))
            .withSection(SectionFactory.customT2("asnLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsPurchaseOrder.TABLE_NAME)
   private Integer purchaseOrderId;

   @QField(isRequired = true, maxLength = 50)
   private String asnNumber;

   @QField(maxLength = 100)
   private String carrierName;

   @QField(maxLength = 100)
   private String trackingNumber;

   @QField()
   private Instant expectedArrivalDate;

   @QField(possibleValueSourceName = AsnStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsAsn()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsAsn(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsAsn withId(Integer id) { this.id = id; return (this); }

   public Integer getPurchaseOrderId() { return (this.purchaseOrderId); }
   public void setPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
   public WmsAsn withPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return (this); }

   public String getAsnNumber() { return (this.asnNumber); }
   public void setAsnNumber(String asnNumber) { this.asnNumber = asnNumber; }
   public WmsAsn withAsnNumber(String asnNumber) { this.asnNumber = asnNumber; return (this); }

   public String getCarrierName() { return (this.carrierName); }
   public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
   public WmsAsn withCarrierName(String carrierName) { this.carrierName = carrierName; return (this); }

   public String getTrackingNumber() { return (this.trackingNumber); }
   public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
   public WmsAsn withTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return (this); }

   public Instant getExpectedArrivalDate() { return (this.expectedArrivalDate); }
   public void setExpectedArrivalDate(Instant expectedArrivalDate) { this.expectedArrivalDate = expectedArrivalDate; }
   public WmsAsn withExpectedArrivalDate(Instant expectedArrivalDate) { this.expectedArrivalDate = expectedArrivalDate; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsAsn withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsAsn withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsAsn withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
