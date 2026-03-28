/*******************************************************************************
 ** QRecord Entity for WmsCycleCount table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;


@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsCycleCount.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WmsCycleCountLine.class,
         joinFieldName = "cycleCountId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Count Lines", enabled = true, maxRows = 100))
   }
)
public class WmsCycleCount extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsCycleCount";



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
         String lineChildJoinName = QJoinMetaData.makeInferredJoinName(WmsCycleCount.TABLE_NAME, WmsCycleCountLine.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("exposure"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("id")
            .withSection(SectionFactory.defaultT1("id", "warehouseId", "clientId", "countTypeId", "cycleCountStatusId", "assignedTo"))
            .withSection(SectionFactory.defaultT2("plannedDate", "startedDate", "completedDate", "notes"))
            .withSection(SectionFactory.customT2("countLines", new QIcon("list")).withWidgetName(lineChildJoinName))
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

   @QField(isRequired = true, possibleValueSourceName = CycleCountType.NAME, label = "Count Type")
   private Integer countTypeId;

   @QField(isRequired = true, possibleValueSourceName = CycleCountStatus.NAME, label = "Status")
   private Integer cycleCountStatusId;

   @QField()
   private Instant plannedDate;

   @QField()
   private Instant startedDate;

   @QField()
   private Instant completedDate;

   @QField(maxLength = 100)
   private String assignedTo;

   @QField(maxLength = 500)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsCycleCount()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsCycleCount(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsCycleCount withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public WmsCycleCount withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public WmsCycleCount withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getCountTypeId() { return (this.countTypeId); }
   public WmsCycleCount withCountTypeId(Integer countTypeId) { this.countTypeId = countTypeId; return (this); }

   public Integer getCycleCountStatusId() { return (this.cycleCountStatusId); }
   public WmsCycleCount withCycleCountStatusId(Integer cycleCountStatusId) { this.cycleCountStatusId = cycleCountStatusId; return (this); }

   public Instant getPlannedDate() { return (this.plannedDate); }
   public WmsCycleCount withPlannedDate(Instant plannedDate) { this.plannedDate = plannedDate; return (this); }

   public Instant getStartedDate() { return (this.startedDate); }
   public WmsCycleCount withStartedDate(Instant startedDate) { this.startedDate = startedDate; return (this); }

   public Instant getCompletedDate() { return (this.completedDate); }
   public WmsCycleCount withCompletedDate(Instant completedDate) { this.completedDate = completedDate; return (this); }

   public String getAssignedTo() { return (this.assignedTo); }
   public WmsCycleCount withAssignedTo(String assignedTo) { this.assignedTo = assignedTo; return (this); }

   public String getNotes() { return (this.notes); }
   public WmsCycleCount withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsCycleCount withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsCycleCount withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
