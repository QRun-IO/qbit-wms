/*******************************************************************************
 ** QRecord Entity for WmsInventoryHold table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.HoldType;


@QMetaDataProducingEntity(producePossibleValueSource = true, produceTableMetaData = true)
public class WmsInventoryHold extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsInventoryHold";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, possibleValueSourceName = WmsItem.TABLE_NAME)
   private Integer itemId;

   @QField(maxLength = 50)
   private String lotNumber;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME)
   private Integer locationId;

   @QField(possibleValueSourceName = HoldType.NAME, label = "Hold Type")
   private Integer holdTypeId;

   @QField(maxLength = 500)
   private String reason;

   @QField(maxLength = 100)
   private String placedBy;

   @QField()
   private Instant placedDate;

   @QField(maxLength = 100)
   private String releasedBy;

   @QField()
   private Instant releasedDate;

   @QField(maxLength = 20)
   private String status;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsInventoryHold()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsInventoryHold(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public void setId(Integer id) { this.id = id; }
   public WmsInventoryHold withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public void setWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; }
   public WmsInventoryHold withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public void setClientId(Integer clientId) { this.clientId = clientId; }
   public WmsInventoryHold withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public Integer getItemId() { return (this.itemId); }
   public void setItemId(Integer itemId) { this.itemId = itemId; }
   public WmsInventoryHold withItemId(Integer itemId) { this.itemId = itemId; return (this); }

   public String getLotNumber() { return (this.lotNumber); }
   public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
   public WmsInventoryHold withLotNumber(String lotNumber) { this.lotNumber = lotNumber; return (this); }

   public Integer getLocationId() { return (this.locationId); }
   public void setLocationId(Integer locationId) { this.locationId = locationId; }
   public WmsInventoryHold withLocationId(Integer locationId) { this.locationId = locationId; return (this); }

   public Integer getHoldTypeId() { return (this.holdTypeId); }
   public void setHoldTypeId(Integer holdTypeId) { this.holdTypeId = holdTypeId; }
   public WmsInventoryHold withHoldTypeId(Integer holdTypeId) { this.holdTypeId = holdTypeId; return (this); }

   public String getReason() { return (this.reason); }
   public void setReason(String reason) { this.reason = reason; }
   public WmsInventoryHold withReason(String reason) { this.reason = reason; return (this); }

   public String getPlacedBy() { return (this.placedBy); }
   public void setPlacedBy(String placedBy) { this.placedBy = placedBy; }
   public WmsInventoryHold withPlacedBy(String placedBy) { this.placedBy = placedBy; return (this); }

   public Instant getPlacedDate() { return (this.placedDate); }
   public void setPlacedDate(Instant placedDate) { this.placedDate = placedDate; }
   public WmsInventoryHold withPlacedDate(Instant placedDate) { this.placedDate = placedDate; return (this); }

   public String getReleasedBy() { return (this.releasedBy); }
   public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }
   public WmsInventoryHold withReleasedBy(String releasedBy) { this.releasedBy = releasedBy; return (this); }

   public Instant getReleasedDate() { return (this.releasedDate); }
   public void setReleasedDate(Instant releasedDate) { this.releasedDate = releasedDate; }
   public WmsInventoryHold withReleasedDate(Instant releasedDate) { this.releasedDate = releasedDate; return (this); }

   public String getStatus() { return (this.status); }
   public void setStatus(String status) { this.status = status; }
   public WmsInventoryHold withStatus(String status) { this.status = status; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public void setCreateDate(Instant createDate) { this.createDate = createDate; }
   public WmsInventoryHold withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public void setModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; }
   public WmsInventoryHold withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
