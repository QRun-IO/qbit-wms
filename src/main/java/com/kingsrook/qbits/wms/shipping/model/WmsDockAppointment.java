/*******************************************************************************
 ** QRecord Entity for WmsDockAppointment table -- represents a scheduled
 ** inbound or outbound dock appointment for carrier pickup or delivery.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import java.time.Instant;
import java.time.LocalDate;
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
import com.kingsrook.qbits.wms.core.enums.DockAppointmentStatus;
import com.kingsrook.qbits.wms.core.enums.DockAppointmentType;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WmsDockAppointment.TableMetaDataCustomizer.class
)
public class WmsDockAppointment extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsDockAppointment";



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
            .withIcon(new QIcon().withName("event"))
            .withRecordLabelFormat("APPT-%s")
            .withRecordLabelFields("id")
            .withSection(new QFieldSection("identity", "Identity", new QIcon("badge"), Tier.T1,
               java.util.List.of("id", "appointmentTypeId", "carrierName", "statusId", "scheduledDate")))
            .withSection(new QFieldSection("schedule", "Schedule", new QIcon("schedule"), Tier.T2,
               java.util.List.of("scheduledTimeStart", "scheduledTimeEnd", "actualArrivalTime", "actualDepartureTime")))
            .withSection(new QFieldSection("location", "Location", new QIcon("place"), Tier.T2,
               java.util.List.of("warehouseId", "dockDoorId")))
            .withSection(new QFieldSection("reference", "Reference", new QIcon("link"), Tier.T2,
               java.util.List.of("referenceType", "referenceId", "notes")))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME, label = "Dock Door")
   private Integer dockDoorId;

   @QField(possibleValueSourceName = DockAppointmentType.NAME, label = "Type")
   private Integer appointmentTypeId;

   @QField(maxLength = 100)
   private String carrierName;

   @QField()
   private LocalDate scheduledDate;

   @QField(maxLength = 10)
   private String scheduledTimeStart;

   @QField(maxLength = 10)
   private String scheduledTimeEnd;

   @QField()
   private Instant actualArrivalTime;

   @QField()
   private Instant actualDepartureTime;

   @QField(possibleValueSourceName = DockAppointmentStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(maxLength = 50)
   private String referenceType;

   @QField()
   private Integer referenceId;

   @QField(maxLength = 2000)
   private String notes;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsDockAppointment()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsDockAppointment(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsDockAppointment withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsDockAppointment withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getDockDoorId() { return (this.dockDoorId); }
   public void setDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; }
   public WmsDockAppointment withDockDoorId(Integer dockDoorId) { this.dockDoorId = dockDoorId; return (this); }

   public Integer getAppointmentTypeId() { return (this.appointmentTypeId); }
   public void setAppointmentTypeId(Integer appointmentTypeId) { this.appointmentTypeId = appointmentTypeId; }
   public WmsDockAppointment withAppointmentTypeId(Integer appointmentTypeId) { this.appointmentTypeId = appointmentTypeId; return (this); }

   public String getCarrierName() { return (this.carrierName); }
   public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
   public WmsDockAppointment withCarrierName(String carrierName) { this.carrierName = carrierName; return (this); }

   public LocalDate getScheduledDate() { return (this.scheduledDate); }
   public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }
   public WmsDockAppointment withScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; return (this); }

   public String getScheduledTimeStart() { return (this.scheduledTimeStart); }
   public void setScheduledTimeStart(String scheduledTimeStart) { this.scheduledTimeStart = scheduledTimeStart; }
   public WmsDockAppointment withScheduledTimeStart(String scheduledTimeStart) { this.scheduledTimeStart = scheduledTimeStart; return (this); }

   public String getScheduledTimeEnd() { return (this.scheduledTimeEnd); }
   public void setScheduledTimeEnd(String scheduledTimeEnd) { this.scheduledTimeEnd = scheduledTimeEnd; }
   public WmsDockAppointment withScheduledTimeEnd(String scheduledTimeEnd) { this.scheduledTimeEnd = scheduledTimeEnd; return (this); }

   public Instant getActualArrivalTime() { return (this.actualArrivalTime); }
   public void setActualArrivalTime(Instant actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; }
   public WmsDockAppointment withActualArrivalTime(Instant actualArrivalTime) { this.actualArrivalTime = actualArrivalTime; return (this); }

   public Instant getActualDepartureTime() { return (this.actualDepartureTime); }
   public void setActualDepartureTime(Instant actualDepartureTime) { this.actualDepartureTime = actualDepartureTime; }
   public WmsDockAppointment withActualDepartureTime(Instant actualDepartureTime) { this.actualDepartureTime = actualDepartureTime; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public void setStatusId(Integer statusId) { this.statusId = statusId; }
   public WmsDockAppointment withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public String getReferenceType() { return (this.referenceType); }
   public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
   public WmsDockAppointment withReferenceType(String referenceType) { this.referenceType = referenceType; return (this); }

   public Integer getReferenceId() { return (this.referenceId); }
   public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }
   public WmsDockAppointment withReferenceId(Integer referenceId) { this.referenceId = referenceId; return (this); }

   public String getNotes() { return (this.notes); }
   public void setNotes(String notes) { this.notes = notes; }
   public WmsDockAppointment withNotes(String notes) { this.notes = notes; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsDockAppointment withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsDockAppointment withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
