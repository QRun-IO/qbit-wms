/*******************************************************************************
 ** QRecord Entity for WmsUnitOfMeasure table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsUnitOfMeasure extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsUnitOfMeasure";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(maxLength = 20)
   private String uomType;

   @QField(maxLength = 50)
   private String label;

   @QField()
   private Integer quantityOfBase;

   @QField(maxLength = 50)
   private String barcode;

   @QField()
   private BigDecimal weightLbs;

   @QField()
   private BigDecimal lengthIn;

   @QField()
   private BigDecimal widthIn;

   @QField()
   private BigDecimal heightIn;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsUnitOfMeasure()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsUnitOfMeasure(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsUnitOfMeasure withId(Integer id) { this.id = id; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsUnitOfMeasure withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public String getUomType() { return (this.uomType); }
   public void setUomType(String uomType) { this.uomType = uomType; }
   public WmsUnitOfMeasure withUomType(String uomType) { this.uomType = uomType; return (this); }

   public String getLabel() { return (this.label); }
   public void setLabel(String label) { this.label = label; }
   public WmsUnitOfMeasure withLabel(String label) { this.label = label; return (this); }

   public Integer getQuantityOfBase() { return (this.quantityOfBase); }
   public void setQuantityOfBase(Integer quantityOfBase) { this.quantityOfBase = quantityOfBase; }
   public WmsUnitOfMeasure withQuantityOfBase(Integer quantityOfBase) { this.quantityOfBase = quantityOfBase; return (this); }

   public String getBarcode() { return (this.barcode); }
   public void setBarcode(String barcode) { this.barcode = barcode; }
   public WmsUnitOfMeasure withBarcode(String barcode) { this.barcode = barcode; return (this); }

   public BigDecimal getWeightLbs() { return (this.weightLbs); }
   public void setWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; }
   public WmsUnitOfMeasure withWeightLbs(BigDecimal weightLbs) { this.weightLbs = weightLbs; return (this); }

   public BigDecimal getLengthIn() { return (this.lengthIn); }
   public void setLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; }
   public WmsUnitOfMeasure withLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; return (this); }

   public BigDecimal getWidthIn() { return (this.widthIn); }
   public void setWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; }
   public WmsUnitOfMeasure withWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; return (this); }

   public BigDecimal getHeightIn() { return (this.heightIn); }
   public void setHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; }
   public WmsUnitOfMeasure withHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsUnitOfMeasure withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsUnitOfMeasure withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
