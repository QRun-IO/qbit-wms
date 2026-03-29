/*******************************************************************************
 ** QRecord Entity for WmsReturnReceipt table -- represents a physical receipt
 ** event when returned goods arrive at the warehouse against an RMA.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.model;


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


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReturnReceipt.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsReturnReceiptLine.class,
         joinFieldName = "returnReceiptId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Receipt Lines", enabled = true, maxRows = 50))
   }
)
public class WmsReturnReceipt extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReturnReceipt";



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
         String lineChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(
            WmsReturnReceipt.TABLE_NAME, WmsReturnReceiptLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("assignment_returned"))
            .withRecordLabelFormat("RRCPT-%s")
            .withRecordLabelFields("receiptNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "receiptNumber", "returnAuthorizationId", "receivedDate")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("receivedBy", "carrierName", "trackingNumber")))
            .withSection(SectionFactory.customT2("receiptLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsReturnAuthorization.TABLE_NAME)
   private Integer returnAuthorizationId;

   @QField(maxLength = 50)
   private String receiptNumber;

   @QField(maxLength = 100)
   private String receivedBy;

   @QField()
   private Instant receivedDate;

   @QField(maxLength = 100)
   private String carrierName;

   @QField(maxLength = 100)
   private String trackingNumber;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReturnReceipt()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReturnReceipt(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReturnReceipt withId(Integer id) { this.id = id; return (this); }

   public Integer getReturnAuthorizationId() { return (this.returnAuthorizationId); }
   public void setReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; }
   public WmsReturnReceipt withReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; return (this); }

   public String getReceiptNumber() { return (this.receiptNumber); }
   public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
   public WmsReturnReceipt withReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; return (this); }

   public String getReceivedBy() { return (this.receivedBy); }
   public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
   public WmsReturnReceipt withReceivedBy(String receivedBy) { this.receivedBy = receivedBy; return (this); }

   public Instant getReceivedDate() { return (this.receivedDate); }
   public void setReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; }
   public WmsReturnReceipt withReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; return (this); }

   public String getCarrierName() { return (this.carrierName); }
   public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
   public WmsReturnReceipt withCarrierName(String carrierName) { this.carrierName = carrierName; return (this); }

   public String getTrackingNumber() { return (this.trackingNumber); }
   public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
   public WmsReturnReceipt withTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReturnReceipt withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReturnReceipt withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
