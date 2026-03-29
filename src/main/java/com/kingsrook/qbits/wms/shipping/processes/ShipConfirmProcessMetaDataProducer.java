/*******************************************************************************
 ** MetaData producer for the ShipConfirm process.  Validates that cartons are
 ** labeled and ready, then pushes external confirmation.  Note: inventory
 ** transactions and status updates are handled by the LOAD completion handler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


public class ShipConfirmProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsShipConfirm";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Ship Confirm")
         .withIcon(new QIcon().withName("local_shipping"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Scan carton at dock                                          //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanCarton")
            .withLabel("Scan Carton")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("shipmentId", QFieldType.INTEGER).withLabel("Shipment ID").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Validate and confirm (backend)                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("confirmShip")
            .withCode(new QCodeReference(ShipConfirmStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showConfirmation")
            .withLabel("Ship Confirmed")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
