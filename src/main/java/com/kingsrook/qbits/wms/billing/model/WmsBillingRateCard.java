/*******************************************************************************
 ** QRecord Entity for WmsBillingRateCard table -- defines a set of billing
 ** rates for a specific 3PL client, with effective/expiration date range.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.time.Instant;
import java.time.LocalDate;
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
import com.kingsrook.qbits.wms.core.enums.BillingRateCardStatus;
import com.kingsrook.qbits.wms.core.model.WmsClient;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsBillingRateCard.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsBillingRate.class,
         joinFieldName = "rateCardId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Billing Rates", enabled = true, maxRows = 50))
   }
)
public class WmsBillingRateCard extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsBillingRateCard";



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
         String rateChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsBillingRateCard.TABLE_NAME, WmsBillingRate.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("request_quote"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "clientId", "name", "statusId")))
            .withSection(new QFieldSection("dates", "Date Range", new QIcon("date_range"), Tier.T2,
               java.util.List.of("effectiveDate", "expirationDate")))
            .withSection(SectionFactory.customT2("billingRates", new QIcon("list")).withWidgetName(rateChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField()
   private LocalDate effectiveDate;

   @QField()
   private LocalDate expirationDate;

   @QField(possibleValueSourceName = BillingRateCardStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsBillingRateCard()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsBillingRateCard(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsBillingRateCard withId(Integer id) { this.id = id; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsBillingRateCard withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsBillingRateCard withName(String name) { this.name = name; return (this); }

   public LocalDate getEffectiveDate() { return (this.effectiveDate); }
   public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
   public WmsBillingRateCard withEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; return (this); }

   public LocalDate getExpirationDate() { return (this.expirationDate); }
   public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
   public WmsBillingRateCard withExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsBillingRateCard withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsBillingRateCard withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsBillingRateCard withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
