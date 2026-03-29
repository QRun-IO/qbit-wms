/*******************************************************************************
 ** Backend step for GenerateShippingLabel.  Validates the carton is PACKED,
 ** creates a wms_shipment with tracking info, creates a wms_shipment_order
 ** junction record, updates the carton status to LABELED, and updates the
 ** shipment status to LABEL_PRINTED.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.ShipmentStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.shipping.CarrierAdapter;
import com.kingsrook.qbits.wms.shipping.DefaultCarrierAdapter;
import com.kingsrook.qbits.wms.shipping.model.WmsShipment;
import com.kingsrook.qbits.wms.shipping.model.WmsShipmentOrder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class GenerateShippingLabelStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(GenerateShippingLabelStep.class);

   private CarrierAdapter carrierAdapter;



   /*******************************************************************************
    ** Constructor that uses the default carrier adapter.
    *******************************************************************************/
   public GenerateShippingLabelStep()
   {
      this.carrierAdapter = new DefaultCarrierAdapter();
   }



   /*******************************************************************************
    ** Constructor that accepts a custom carrier adapter.
    *******************************************************************************/
   public GenerateShippingLabelStep(CarrierAdapter carrierAdapter)
   {
      this.carrierAdapter = carrierAdapter != null ? carrierAdapter : new DefaultCarrierAdapter();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer cartonId = input.getValueInteger("cartonId");
      String carrier = input.getValueString("carrier");
      String serviceLevel = input.getValueString("serviceLevel");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      if(cartonId == null)
      {
         throw new QUserFacingException("Carton is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load and validate carton                                            //
      /////////////////////////////////////////////////////////////////////////
      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      if(carton == null)
      {
         throw new QUserFacingException("Carton not found: " + cartonId);
      }

      Integer cartonStatusId = carton.getValueInteger("statusId");
      if(cartonStatusId == null || !cartonStatusId.equals(CartonStatus.PACKED.getPossibleValueId()))
      {
         throw new QUserFacingException("Carton must be in PACKED status to generate a label.");
      }

      Integer orderId = carton.getValueInteger("orderId");

      /////////////////////////////////////////////////////////////////////////
      // Generate tracking number via carrier adapter                       //
      /////////////////////////////////////////////////////////////////////////
      String trackingNumber = carrierAdapter.generateLabel(carrier, serviceLevel, null, null, null);
      String shipmentNumber = "SHIP-" + System.nanoTime();

      LOG.info("Generating shipping label", logPair("cartonId", cartonId), logPair("carrier", carrier));

      /////////////////////////////////////////////////////////////////////////
      // Create shipment record                                              //
      /////////////////////////////////////////////////////////////////////////
      QRecord shipment = new InsertAction().execute(new InsertInput(WmsShipment.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("shipmentNumber", shipmentNumber)
         .withValue("carrier", carrier)
         .withValue("serviceLevel", serviceLevel)
         .withValue("trackingNumber", trackingNumber)
         .withValue("statusId", ShipmentStatus.LABEL_PRINTED.getPossibleValueId())
      )).getRecords().get(0);

      Integer shipmentId = shipment.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Create shipment-order junction record                               //
      /////////////////////////////////////////////////////////////////////////
      if(orderId != null)
      {
         new InsertAction().execute(new InsertInput(WmsShipmentOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("shipmentId", shipmentId)
            .withValue("orderId", orderId)));
      }

      /////////////////////////////////////////////////////////////////////////
      // Update carton status to LABELED                                     //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", cartonId)
         .withValue("statusId", CartonStatus.LABELED.getPossibleValueId())));

      output.addValue("resultMessage", "Shipping label generated. Tracking: " + trackingNumber);
      output.addValue("shipmentId", shipmentId);
      output.addValue("trackingNumber", trackingNumber);
   }
}
