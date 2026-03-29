/*******************************************************************************
 ** QRecord Entity for WmsReturnAuthorization table -- represents a return
 ** merchandise authorization (RMA) tracking the return of items from a
 ** customer order through receipt, inspection, and disposition.
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
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.core.enums.ReturnReasonCode;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReturnAuthorization.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsReturnAuthorizationLine.class,
         joinFieldName = "returnAuthorizationId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Return Lines", enabled = true, maxRows = 50))
   }
)
public class WmsReturnAuthorization extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReturnAuthorization";



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
            WmsReturnAuthorization.TABLE_NAME, WmsReturnAuthorizationLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("assignment_return"))
            .withRecordLabelFormat("RMA-%s")
            .withRecordLabelFields("rmaNumber")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "rmaNumber", "statusId", "reasonCodeId", "customerName")))
            .withSection(new QFieldSection("references", "References", new QIcon("link"), Tier.T2,
               java.util.List.of("warehouseId", "clientId", "originalOrderId")))
            .withSection(SectionFactory.customT2("returnLines", new QIcon("list")).withWidgetName(lineChildJoinName))
            .withSection(new QFieldSection("timeline", "Timeline", new QIcon("event"), Tier.T2,
               java.util.List.of("authorizedDate", "expectedReceiptDate", "receivedDate", "closedDate")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("notes")))
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
   private String rmaNumber;

   @QField(possibleValueSourceName = WmsOrder.TABLE_NAME, label = "Original Order")
   private Integer originalOrderId;

   @QField(maxLength = 100)
   private String customerName;

   @QField(possibleValueSourceName = ReturnReasonCode.NAME, label = "Reason Code")
   private Integer reasonCodeId;

   @QField(possibleValueSourceName = ReturnAuthorizationStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private Instant authorizedDate;

   @QField()
   private Instant expectedReceiptDate;

   @QField()
   private Instant receivedDate;

   @QField()
   private Instant closedDate;

   @QField(maxLength = 2000)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReturnAuthorization()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReturnAuthorization(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReturnAuthorization withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsReturnAuthorization withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsReturnAuthorization withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getRmaNumber() { return (this.rmaNumber); }
   public void setRmaNumber(String rmaNumber) { this.rmaNumber = rmaNumber; }
   public WmsReturnAuthorization withRmaNumber(String rmaNumber) { this.rmaNumber = rmaNumber; return (this); }

   public Integer getOriginalOrderId() { return (this.originalOrderId); }
   public void setOriginalOrderId(Integer originalOrderId) { this.originalOrderId = originalOrderId; }
   public WmsReturnAuthorization withOriginalOrderId(Integer originalOrderId) { this.originalOrderId = originalOrderId; return (this); }

   public String getCustomerName() { return (this.customerName); }
   public void setCustomerName(String customerName) { this.customerName = customerName; }
   public WmsReturnAuthorization withCustomerName(String customerName) { this.customerName = customerName; return (this); }

   public Integer getReasonCodeId() { return (this.reasonCodeId); }
   public void setReasonCodeId(Integer reasonCodeId) { this.reasonCodeId = reasonCodeId; }
   public WmsReturnAuthorization withReasonCodeId(Integer reasonCodeId) { this.reasonCodeId = reasonCodeId; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsReturnAuthorization withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getAuthorizedDate() { return (this.authorizedDate); }
   public void setAuthorizedDate(Instant authorizedDate) { this.authorizedDate = authorizedDate; }
   public WmsReturnAuthorization withAuthorizedDate(Instant authorizedDate) { this.authorizedDate = authorizedDate; return (this); }

   public Instant getExpectedReceiptDate() { return (this.expectedReceiptDate); }
   public void setExpectedReceiptDate(Instant expectedReceiptDate) { this.expectedReceiptDate = expectedReceiptDate; }
   public WmsReturnAuthorization withExpectedReceiptDate(Instant expectedReceiptDate) { this.expectedReceiptDate = expectedReceiptDate; return (this); }

   public Instant getReceivedDate() { return (this.receivedDate); }
   public void setReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; }
   public WmsReturnAuthorization withReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; return (this); }

   public Instant getClosedDate() { return (this.closedDate); }
   public void setClosedDate(Instant closedDate) { this.closedDate = closedDate; }
   public WmsReturnAuthorization withClosedDate(Instant closedDate) { this.closedDate = closedDate; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsReturnAuthorization withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReturnAuthorization withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReturnAuthorization withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
