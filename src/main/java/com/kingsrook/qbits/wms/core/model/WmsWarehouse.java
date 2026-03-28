/*******************************************************************************
 ** QRecord Entity for WmsWarehouse table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


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
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsWarehouse.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsZone.class,
         joinFieldName = "warehouseId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Zones", enabled = true, maxRows = 50))
   }
)
public class WmsWarehouse extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsWarehouse";



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
         String zoneChildJoinName = com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData.makeInferredJoinName(WmsWarehouse.TABLE_NAME, WmsZone.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("warehouse"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withSection(SectionFactory.defaultT1("id", "name", "code", "isActive"))
            .withSection(new QFieldSection("address", "Address", new QIcon("location_on"), Tier.T2,
               java.util.List.of("addressLine1", "addressLine2", "city", "stateProvince", "postalCode", "country", "timezone")))
            .withSection(SectionFactory.customT2("zones", new QIcon("grid_view")).withWidgetName(zoneChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, maxLength = 100)
   private String name;

   @QField(isRequired = true, maxLength = 20)
   private String code;

   @QField(maxLength = 200)
   private String addressLine1;

   @QField(maxLength = 200)
   private String addressLine2;

   @QField(maxLength = 100)
   private String city;

   @QField(maxLength = 100)
   private String stateProvince;

   @QField(maxLength = 20)
   private String postalCode;

   @QField(maxLength = 100)
   private String country;

   @QField(maxLength = 50)
   private String timezone;

   @QField()
   private Boolean isActive;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsWarehouse()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsWarehouse(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsWarehouse withId(Integer id) { this.id = id; return (this); }

   public String getName() { return (this.name); }
   public void setName(String name) { this.name = name; }
   public WmsWarehouse withName(String name) { this.name = name; return (this); }

   public String getCode() { return (this.code); }
   public void setCode(String code) { this.code = code; }
   public WmsWarehouse withCode(String code) { this.code = code; return (this); }

   public String getAddressLine1() { return (this.addressLine1); }
   public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
   public WmsWarehouse withAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; return (this); }

   public String getAddressLine2() { return (this.addressLine2); }
   public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
   public WmsWarehouse withAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; return (this); }

   public String getCity() { return (this.city); }
   public void setCity(String city) { this.city = city; }
   public WmsWarehouse withCity(String city) { this.city = city; return (this); }

   public String getStateProvince() { return (this.stateProvince); }
   public void setStateProvince(String stateProvince) { this.stateProvince = stateProvince; }
   public WmsWarehouse withStateProvince(String stateProvince) { this.stateProvince = stateProvince; return (this); }

   public String getPostalCode() { return (this.postalCode); }
   public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
   public WmsWarehouse withPostalCode(String postalCode) { this.postalCode = postalCode; return (this); }

   public String getCountry() { return (this.country); }
   public void setCountry(String country) { this.country = country; }
   public WmsWarehouse withCountry(String country) { this.country = country; return (this); }

   public String getTimezone() { return (this.timezone); }
   public void setTimezone(String timezone) { this.timezone = timezone; }
   public WmsWarehouse withTimezone(String timezone) { this.timezone = timezone; return (this); }

   public Boolean getIsActive() { return (this.isActive); }
   public void setIsActive(Boolean isActive) { this.isActive = isActive; }
   public WmsWarehouse withIsActive(Boolean isActive) { this.isActive = isActive; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsWarehouse withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsWarehouse withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
