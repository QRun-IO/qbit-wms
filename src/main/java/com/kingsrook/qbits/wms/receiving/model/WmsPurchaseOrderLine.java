/*******************************************************************************
 ** QRecord Entity for WmsPurchaseOrderLine table -- individual line items on a
 ** purchase order, tracking expected and received quantities per item.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsPurchaseOrderLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsPurchaseOrderLine";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsPurchaseOrder.TABLE_NAME)
   private Integer purchaseOrderId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer expectedQuantity;

   @QField(defaultValue = "0")
   private Integer receivedQuantity;

   @QField(possibleValueSourceName = WmsUnitOfMeasure.TABLE_NAME, label = "UOM")
   private Integer uomId;

   @QField()
   private Integer lineNumber;

   @QField(possibleValueSourceName = PurchaseOrderStatus.NAME, label = "Status")
   private Integer statusId;

   @QField()
   private BigDecimal overReceiveTolerancePct;

   @QField()
   private BigDecimal underReceiveTolerancePct;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsPurchaseOrderLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsPurchaseOrderLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsPurchaseOrderLine withId(Integer id) { this.id = id; return (this); }

   public Integer getPurchaseOrderId() { return (this.purchaseOrderId); }
   public void setPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
   public WmsPurchaseOrderLine withPurchaseOrderId(Integer purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsPurchaseOrderLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getExpectedQuantity() { return (this.expectedQuantity); }
   public void setExpectedQuantity(Integer expectedQuantity) { this.expectedQuantity = expectedQuantity; }
   public WmsPurchaseOrderLine withExpectedQuantity(Integer expectedQuantity) { this.expectedQuantity = expectedQuantity; return (this); }

   public Integer getReceivedQuantity() { return (this.receivedQuantity); }
   public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
   public WmsPurchaseOrderLine withReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; return (this); }

   public Integer getUomId() { return (this.uomId); }
   public void setUomId(Integer uomId) { this.uomId = uomId; }
   public WmsPurchaseOrderLine withUomId(Integer uomId) { this.uomId = uomId; return (this); }

   public Integer getLineNumber() { return (this.lineNumber); }
   public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
   public WmsPurchaseOrderLine withLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsPurchaseOrderLine withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public BigDecimal getOverReceiveTolerancePct() { return (this.overReceiveTolerancePct); }
   public void setOverReceiveTolerancePct(BigDecimal overReceiveTolerancePct) { this.overReceiveTolerancePct = overReceiveTolerancePct; }
   public WmsPurchaseOrderLine withOverReceiveTolerancePct(BigDecimal overReceiveTolerancePct) { this.overReceiveTolerancePct = overReceiveTolerancePct; return (this); }

   public BigDecimal getUnderReceiveTolerancePct() { return (this.underReceiveTolerancePct); }
   public void setUnderReceiveTolerancePct(BigDecimal underReceiveTolerancePct) { this.underReceiveTolerancePct = underReceiveTolerancePct; }
   public WmsPurchaseOrderLine withUnderReceiveTolerancePct(BigDecimal underReceiveTolerancePct) { this.underReceiveTolerancePct = underReceiveTolerancePct; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsPurchaseOrderLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsPurchaseOrderLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
