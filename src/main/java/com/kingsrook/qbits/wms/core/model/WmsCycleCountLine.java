/*******************************************************************************
 ** QRecord Entity for WmsCycleCountLine table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;


@QMetaDataProducingEntity(producePossibleValueSource = true)
public class WmsCycleCountLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsCycleCountLine";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsCycleCount.TABLE_NAME)
   private Integer cycleCountId;

   @QField(isRequired = true, possibleValueSourceName = WmsLocation.TABLE_NAME)
   private Integer locationId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField()
   private BigDecimal expectedQuantity;

   @QField()
   private BigDecimal countedQuantity;

   @QField()
   private BigDecimal variance;

   @QField()
   private Boolean varianceApproved;

   @QField(maxLength = 100)
   private String approvedBy;

   @QField(maxLength = 20)
   private String status;

   @QField()
   private Integer taskId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsCycleCountLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsCycleCountLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsCycleCountLine withId(Integer id) { this.id = id; return (this); }

   public Integer getCycleCountId() { return (this.cycleCountId); }
   public WmsCycleCountLine withCycleCountId(Integer cycleCountId) { this.cycleCountId = cycleCountId; return (this); }

   public Integer getLocationId() { return (this.locationId); }
   public WmsCycleCountLine withLocationId(Integer locationId) { this.locationId = locationId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public WmsCycleCountLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public WmsCycleCountLine withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public BigDecimal getExpectedQuantity() { return (this.expectedQuantity); }
   public WmsCycleCountLine withExpectedQuantity(BigDecimal expectedQuantity) { this.expectedQuantity = expectedQuantity; return (this); }

   public BigDecimal getCountedQuantity() { return (this.countedQuantity); }
   public WmsCycleCountLine withCountedQuantity(BigDecimal countedQuantity) { this.countedQuantity = countedQuantity; return (this); }

   public BigDecimal getVariance() { return (this.variance); }
   public WmsCycleCountLine withVariance(BigDecimal variance) { this.variance = variance; return (this); }

   public Boolean getVarianceApproved() { return (this.varianceApproved); }
   public WmsCycleCountLine withVarianceApproved(Boolean varianceApproved) { this.varianceApproved = varianceApproved; return (this); }

   public String getApprovedBy() { return (this.approvedBy); }
   public WmsCycleCountLine withApprovedBy(String approvedBy) { this.approvedBy = approvedBy; return (this); }

   public String getStatus() { return (this.status); }
   public WmsCycleCountLine withStatus(String status) { this.status = status; return (this); }

   public Integer getTaskId() { return (this.taskId); }
   public WmsCycleCountLine withTaskId(Integer taskId) { this.taskId = taskId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsCycleCountLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsCycleCountLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
