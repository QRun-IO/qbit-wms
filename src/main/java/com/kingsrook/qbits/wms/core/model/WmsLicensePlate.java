/*******************************************************************************
 ** QRecord Entity for WmsLicensePlate (LPN) table.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qbits.wms.core.enums.LpnStatus;


@QMetaDataProducingEntity(producePossibleValueSource = true)
public class WmsLicensePlate extends QRecordEntity
{
   public static final String TABLE_NAME = "wmsLicensePlate";

   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WmsWarehouse.TABLE_NAME)
   private Integer warehouseId;

   @QField(possibleValueSourceName = WmsClient.TABLE_NAME)
   private Integer clientId;

   @QField(isRequired = true, maxLength = 50)
   private String lpnBarcode;

   @QField(possibleValueSourceName = LpnStatus.NAME, label = "Status")
   private Integer statusId;

   @QField(possibleValueSourceName = WmsLocation.TABLE_NAME)
   private Integer locationId;

   @QField()
   private Integer receiptId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WmsLicensePlate()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WmsLicensePlate(QRecord record)
   {
      populateFromQRecord(record);
   }



   public Integer getId() { return (this.id); }
   public WmsLicensePlate withId(Integer id) { this.id = id; return (this); }

   public Integer getWarehouseId() { return (this.warehouseId); }
   public WmsLicensePlate withWarehouseId(Integer warehouseId) { this.warehouseId = warehouseId; return (this); }

   public Integer getClientId() { return (this.clientId); }
   public WmsLicensePlate withClientId(Integer clientId) { this.clientId = clientId; return (this); }

   public String getLpnBarcode() { return (this.lpnBarcode); }
   public WmsLicensePlate withLpnBarcode(String lpnBarcode) { this.lpnBarcode = lpnBarcode; return (this); }

   public Integer getStatusId() { return (this.statusId); }
   public WmsLicensePlate withStatusId(Integer statusId) { this.statusId = statusId; return (this); }

   public Integer getLocationId() { return (this.locationId); }
   public WmsLicensePlate withLocationId(Integer locationId) { this.locationId = locationId; return (this); }

   public Integer getReceiptId() { return (this.receiptId); }
   public WmsLicensePlate withReceiptId(Integer receiptId) { this.receiptId = receiptId; return (this); }

   public Instant getCreateDate() { return (this.createDate); }
   public WmsLicensePlate withCreateDate(Instant createDate) { this.createDate = createDate; return (this); }

   public Instant getModifyDate() { return (this.modifyDate); }
   public WmsLicensePlate withModifyDate(Instant modifyDate) { this.modifyDate = modifyDate; return (this); }
}
