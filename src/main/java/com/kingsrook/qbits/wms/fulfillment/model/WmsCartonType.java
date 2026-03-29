/*******************************************************************************
 ** QRecord Entity for WmsCartonType table -- defines standard carton sizes
 ** with dimensions, weight limits, and cost for packing operations.
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
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsCartonType.TableMetaDataCustomizer.class
)
public class WmsCartonType extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsCartonType";



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
            .withIcon(new QIcon().withName("all_inbox"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withSection(SectionFactory.defaultT1("id", "name", "isActive"))
            .withSection(new QFieldSection("dimensions", "Dimensions", new QIcon("straighten"), Tier.T2,
               java.util.List.of("lengthIn", "widthIn", "heightIn")))
            .withSection(new QFieldSection("weight", "Weight", new QIcon("fitness_center"), Tier.T2,
               java.util.List.of("maxWeightLbs", "tareWeightLbs")))
            .withSection(new QFieldSection("cost", "Cost", new QIcon("attach_money"), Tier.T2,
               java.util.List.of("cost")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField()
   private BigDecimal lengthIn;

   @QField()
   private BigDecimal widthIn;

   @QField()
   private BigDecimal heightIn;

   @QField()
   private BigDecimal maxWeightLbs;

   @QField()
   private BigDecimal tareWeightLbs;

   @QField()
   private BigDecimal cost;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsCartonType()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsCartonType(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsCartonType withId(Integer id) { this.id = id; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsCartonType withName(String name) { this.name = name; return (this); }

   public BigDecimal getLengthIn() { return (this.lengthIn); }
   public void setLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; }
   public WmsCartonType withLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; return (this); }

   public BigDecimal getWidthIn() { return (this.widthIn); }
   public void setWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; }
   public WmsCartonType withWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; return (this); }

   public BigDecimal getHeightIn() { return (this.heightIn); }
   public void setHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; }
   public WmsCartonType withHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; return (this); }

   public BigDecimal getMaxWeightLbs() { return (this.maxWeightLbs); }
   public void setMaxWeightLbs(BigDecimal maxWeightLbs) { this.maxWeightLbs = maxWeightLbs; }
   public WmsCartonType withMaxWeightLbs(BigDecimal maxWeightLbs) { this.maxWeightLbs = maxWeightLbs; return (this); }

   public BigDecimal getTareWeightLbs() { return (this.tareWeightLbs); }
   public void setTareWeightLbs(BigDecimal tareWeightLbs) { this.tareWeightLbs = tareWeightLbs; }
   public WmsCartonType withTareWeightLbs(BigDecimal tareWeightLbs) { this.tareWeightLbs = tareWeightLbs; return (this); }

   public BigDecimal getCost() { return (this.cost); }
   public void setCost(BigDecimal cost) { this.cost = cost; }
   public WmsCartonType withCost(BigDecimal cost) { this.cost = cost; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsCartonType withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsCartonType withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsCartonType withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
