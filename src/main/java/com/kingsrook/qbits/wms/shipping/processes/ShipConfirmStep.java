/*******************************************************************************
 ** Backend step for ShipConfirm.  Validates the shipment exists and is labeled
 ** or manifested, then marks it as PICKED_UP (ready for carrier).  Note:
 ** actual inventory deductions and order status updates are handled by the
 ** LOAD task completion handler, not this process.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import java.time.Instant;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.ShipmentStatus;
import com.kingsrook.qbits.wms.shipping.model.WmsShipment;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ShipConfirmStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ShipConfirmStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer shipmentId = input.getValueInteger("shipmentId");

      if(shipmentId == null)
      {
         throw new QUserFacingException("Shipment ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load and validate shipment                                          //
      /////////////////////////////////////////////////////////////////////////
      QRecord shipment = new GetAction().execute(new GetInput(WmsShipment.TABLE_NAME).withPrimaryKey(shipmentId)).getRecord();
      if(shipment == null)
      {
         throw new QUserFacingException("Shipment not found: " + shipmentId);
      }

      Integer statusId = shipment.getValueInteger("statusId");
      if(!Objects.equals(statusId, ShipmentStatus.LABEL_PRINTED.getPossibleValueId())
         && !Objects.equals(statusId, ShipmentStatus.MANIFESTED.getPossibleValueId()))
      {
         throw new QUserFacingException("Shipment must be LABEL_PRINTED or MANIFESTED to confirm ship.");
      }

      LOG.info("Confirming shipment", logPair("shipmentId", shipmentId));

      /////////////////////////////////////////////////////////////////////////
      // Update shipment to PICKED_UP                                        //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsShipment.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", shipmentId)
         .withValue("statusId", ShipmentStatus.PICKED_UP.getPossibleValueId())
         .withValue("shipDate", Instant.now())));

      output.addValue("resultMessage", "Shipment " + shipmentId + " confirmed and picked up by carrier.");
   }
}
