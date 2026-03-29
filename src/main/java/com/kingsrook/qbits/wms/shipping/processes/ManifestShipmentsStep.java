/*******************************************************************************
 ** Backend step for ManifestShipments.  Queries unmanifested LABEL_PRINTED
 ** shipments for the selected carrier, creates a manifest, assigns shipments
 ** to it, updates shipment statuses to MANIFESTED, and closes the manifest.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.ManifestStatus;
import com.kingsrook.qbits.wms.core.enums.ShipmentStatus;
import com.kingsrook.qbits.wms.shipping.model.WmsManifest;
import com.kingsrook.qbits.wms.shipping.model.WmsShipment;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ManifestShipmentsStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ManifestShipmentsStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      String carrier = input.getValueString("carrier");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      if(carrier == null || carrier.isBlank())
      {
         throw new QUserFacingException("Carrier is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Query unmanifested shipments for this carrier                       //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput shipmentQuery = new QueryAction().execute(new QueryInput(WmsShipment.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
            .withCriteria(new QFilterCriteria("carrier", QCriteriaOperator.EQUALS, carrier))
            .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, ShipmentStatus.LABEL_PRINTED.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("manifestId", QCriteriaOperator.IS_BLANK))));

      List<QRecord> shipments = shipmentQuery.getRecords();

      if(shipments.isEmpty())
      {
         output.addValue("resultMessage", "No unmanifested shipments found for carrier: " + carrier);
         output.addValue("totalShipments", 0);
         return;
      }

      LOG.info("Manifesting shipments", logPair("carrier", carrier), logPair("count", shipments.size()));

      /////////////////////////////////////////////////////////////////////////
      // Create the manifest                                                 //
      /////////////////////////////////////////////////////////////////////////
      String manifestNumber = "MAN-" + System.nanoTime();
      QRecord manifest = new InsertAction().execute(new InsertInput(WmsManifest.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("carrier", carrier)
         .withValue("manifestNumber", manifestNumber)
         .withValue("manifestDate", LocalDate.now())
         .withValue("totalShipments", shipments.size())
         .withValue("statusId", ManifestStatus.CLOSED.getPossibleValueId())
         .withValue("closedDate", Instant.now())
      )).getRecords().get(0);

      Integer manifestId = manifest.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Assign each shipment to the manifest and update status              //
      /////////////////////////////////////////////////////////////////////////
      for(QRecord shipment : shipments)
      {
         new UpdateAction().execute(new UpdateInput(WmsShipment.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", shipment.getValueInteger("id"))
            .withValue("manifestId", manifestId)
            .withValue("statusId", ShipmentStatus.MANIFESTED.getPossibleValueId())));
      }

      output.addValue("resultMessage", "Manifest created with " + shipments.size() + " shipments. Manifest: " + manifestNumber);
      output.addValue("manifestId", manifestId);
      output.addValue("totalShipments", shipments.size());
   }
}
