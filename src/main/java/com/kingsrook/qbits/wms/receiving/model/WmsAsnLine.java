/*******************************************************************************
 ** QRecord Entity for WmsAsnLine table -- individual line items expected on an
 ** Advanced Shipping Notice, with item, quantity, and lot tracking details.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsUnitOfMeasure;


@QMetaDataProducingEntity(produceTableMetaData = true)
public class WmsAsnLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsAsnLine";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsAsn.TABLE_NAME)
   private Integer asnId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField()
   private Integer expectedQuantity;

   @QField(possibleValueSourceName = WmsUnitOfMeasure.TABLE_NAME, label = "UOM")
   private Integer uomId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField()
   private LocalDate expirationDate;

   @QField(maxLength = 100)
   private String lpnBarcode;

   @QField()
   private Integer lineNumber;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsAsnLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsAsnLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsAsnLine withId(Integer id) { this.id = id; return (this); }

   public Integer getAsnId() { return (this.asnId); }
   public void setAsnId(Integer asnId) { this.asnId = asnId; }
   public WmsAsnLine withAsnId(Integer asnId) { this.asnId = asnId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsAsnLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getExpectedQuantity() { return (this.expectedQuantity); }
   public void setExpectedQuantity(Integer expectedQuantity) { this.expectedQuantity = expectedQuantity; }
   public WmsAsnLine withExpectedQuantity(Integer expectedQuantity) { this.expectedQuantity = expectedQuantity; return (this); }

   public Integer getUomId() { return (this.uomId); }
   public void setUomId(Integer uomId) { this.uomId = uomId; }
   public WmsAsnLine withUomId(Integer uomId) { this.uomId = uomId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsAsnLine withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public LocalDate getExpirationDate() { return (this.expirationDate); }
   public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
   public WmsAsnLine withExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; return (this); }

   public String getLpnBarcode() { return (this.lpnBarcode); }
   public void setLpnBarcode(String lpnBarcode) { this.lpnBarcode = lpnBarcode; }
   public WmsAsnLine withLpnBarcode(String lpnBarcode) { this.lpnBarcode = lpnBarcode; return (this); }

   public Integer getLineNumber() { return (this.lineNumber); }
   public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }
   public WmsAsnLine withLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsAsnLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsAsnLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
