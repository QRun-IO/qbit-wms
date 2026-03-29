/*******************************************************************************
 ** QRecord Entity for WmsReturnReceiptLine table -- individual line item
 ** within a return receipt, capturing quantity received, condition, inspection
 ** grade, and disposition details.
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
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qbits.wms.core.enums.ConditionCode;
import com.kingsrook.qbits.wms.core.enums.Disposition;
import com.kingsrook.qbits.wms.core.enums.InspectionGrade;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLocation;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReturnReceiptLine.TableMetaDataCustomizer.class
)
public class WmsReturnReceiptLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReturnReceiptLine";



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
            .withIcon(new QIcon().withName("fact_check"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "returnReceiptId", "returnAuthorizationLineId", "itemId", "quantityReceived")))
            .withSection(new QFieldSection("condition", "Condition", new QIcon("search"), Tier.T2,
               java.util.List.of("actualConditionId", "inspectionGradeId", "dispositionId", "dispositionLocationId")))
            .withSection(new QFieldSection("tracking", "Tracking", new QIcon("qr_code"), Tier.T2,
               java.util.List.of("lotNumber", "serialNumber")))
            .withSection(new QFieldSection("inspection", "Inspection", new QIcon("assignment"), Tier.T2,
               java.util.List.of("inspectionNotes", "inspectedBy", "inspectedDate")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsReturnReceipt.TABLE_NAME)
   private Integer returnReceiptId;

   @QField(possibleValueSourceName = WmsReturnAuthorizationLine.TABLE_NAME)
   private Integer returnAuthorizationLineId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer quantityReceived;

   @QField(possibleValueSourceName = ConditionCode.NAME, label = "Actual Condition")
   private Integer actualConditionId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(maxLength = 100)
   private String serialNumber;

   @QField(possibleValueSourceName = InspectionGrade.NAME, label = "Inspection Grade")
   private Integer inspectionGradeId;

   @QField(possibleValueSourceName = Disposition.NAME, label = "Disposition")
   private Integer dispositionId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Disposition Location")
   private Integer dispositionLocationId;

   @QField(maxLength = 2000)
   private String inspectionNotes;

   @QField(maxLength = 100)
   private String inspectedBy;

   @QField()
   private Instant inspectedDate;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReturnReceiptLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReturnReceiptLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReturnReceiptLine withId(Integer id) { this.id = id; return (this); }

   public Integer getReturnReceiptId() { return (this.returnReceiptId); }
   public void setReturnReceiptId(Integer returnReceiptId) { this.returnReceiptId = returnReceiptId; }
   public WmsReturnReceiptLine withReturnReceiptId(Integer returnReceiptId) { this.returnReceiptId = returnReceiptId; return (this); }

   public Integer getReturnAuthorizationLineId() { return (this.returnAuthorizationLineId); }
   public void setReturnAuthorizationLineId(Integer returnAuthorizationLineId) { this.returnAuthorizationLineId = returnAuthorizationLineId; }
   public WmsReturnReceiptLine withReturnAuthorizationLineId(Integer returnAuthorizationLineId) { this.returnAuthorizationLineId = returnAuthorizationLineId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsReturnReceiptLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getQuantityReceived() { return (this.quantityReceived); }
   public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
   public WmsReturnReceiptLine withQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return (this); }

   public Integer getActualConditionId() { return (this.actualConditionId); }
   public void setActualConditionId(Integer actualConditionId) { this.actualConditionId = actualConditionId; }
   public WmsReturnReceiptLine withActualConditionId(Integer actualConditionId) { this.actualConditionId = actualConditionId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsReturnReceiptLine withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public String getSerialNumber() { return (this.serialNumber); }
   public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
   public WmsReturnReceiptLine withSerialNumber(String serialNumber) { this.serialNumber = serialNumber; return (this); }

   public Integer getInspectionGradeId() { return (this.inspectionGradeId); }
   public void setInspectionGradeId(Integer inspectionGradeId) { this.inspectionGradeId = inspectionGradeId; }
   public WmsReturnReceiptLine withInspectionGradeId(Integer inspectionGradeId) { this.inspectionGradeId = inspectionGradeId; return (this); }

   public Integer getDispositionId() { return (this.dispositionId); }
   public void setDispositionId(Integer dispositionId) { this.dispositionId = dispositionId; }
   public WmsReturnReceiptLine withDispositionId(Integer dispositionId) { this.dispositionId = dispositionId; return (this); }

   public Integer getDispositionLocationId() { return (this.dispositionLocationId); }
   public void setDispositionLocationId(Integer dispositionLocationId) { this.dispositionLocationId = dispositionLocationId; }
   public WmsReturnReceiptLine withDispositionLocationId(Integer dispositionLocationId) { this.dispositionLocationId = dispositionLocationId; return (this); }

   public String getInspectionNotes() { return (this.inspectionNotes); }
   public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
   public WmsReturnReceiptLine withInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; return (this); }

   public String getInspectedBy() { return (this.inspectedBy); }
   public void setInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; }
   public WmsReturnReceiptLine withInspectedBy(String inspectedBy) { this.inspectedBy = inspectedBy; return (this); }

   public Instant getInspectedDate() { return (this.inspectedDate); }
   public void setInspectedDate(Instant inspectedDate) { this.inspectedDate = inspectedDate; }
   public WmsReturnReceiptLine withInspectedDate(Instant inspectedDate) { this.inspectedDate = inspectedDate; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReturnReceiptLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReturnReceiptLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
