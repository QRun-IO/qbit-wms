/*******************************************************************************
 ** QRecord Entity for WmsReturnAuthorizationLine table -- individual line item
 ** within a return authorization, tracking authorized vs received quantities.
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
import com.kingsrook.qbits.wms.core.model.WmsItem;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsReturnAuthorizationLine.TableMetaDataCustomizer.class
)
public class WmsReturnAuthorizationLine extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsReturnAuthorizationLine";



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
            .withIcon(new QIcon().withName("list_alt"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "returnAuthorizationId", "itemId", "quantityAuthorized", "quantityReceived")))
            .withSection(new QFieldSection("details", "Details", new QIcon("info"), Tier.T2,
               java.util.List.of("expectedConditionId")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsReturnAuthorization.TABLE_NAME)
   private Integer returnAuthorizationId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(isRequired = true)
   private Integer quantityAuthorized;

   @QField()
   private Integer quantityReceived;

   @QField(possibleValueSourceName = ConditionCode.NAME, label = "Expected Condition")
   private Integer expectedConditionId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsReturnAuthorizationLine()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsReturnAuthorizationLine(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsReturnAuthorizationLine withId(Integer id) { this.id = id; return (this); }

   public Integer getReturnAuthorizationId() { return (this.returnAuthorizationId); }
   public void setReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; }
   public WmsReturnAuthorizationLine withReturnAuthorizationId(Integer returnAuthorizationId) { this.returnAuthorizationId = returnAuthorizationId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsReturnAuthorizationLine withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public Integer getQuantityAuthorized() { return (this.quantityAuthorized); }
   public void setQuantityAuthorized(Integer quantityAuthorized) { this.quantityAuthorized = quantityAuthorized; }
   public WmsReturnAuthorizationLine withQuantityAuthorized(Integer quantityAuthorized) { this.quantityAuthorized = quantityAuthorized; return (this); }

   public Integer getQuantityReceived() { return (this.quantityReceived); }
   public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }
   public WmsReturnAuthorizationLine withQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; return (this); }

   public Integer getExpectedConditionId() { return (this.expectedConditionId); }
   public void setExpectedConditionId(Integer expectedConditionId) { this.expectedConditionId = expectedConditionId; }
   public WmsReturnAuthorizationLine withExpectedConditionId(Integer expectedConditionId) { this.expectedConditionId = expectedConditionId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsReturnAuthorizationLine withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsReturnAuthorizationLine withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
