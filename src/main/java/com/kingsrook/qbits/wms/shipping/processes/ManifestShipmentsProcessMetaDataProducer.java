/*******************************************************************************
 ** MetaData producer for the ManifestShipments process.  Gathers unmanifested
 ** shipments for a carrier, creates a manifest, and closes it.
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


public class ManifestShipmentsProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsManifestShipments";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Manifest Shipments")
         .withIcon(new QIcon().withName("receipt_long"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select carrier                                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectCarrier")
            .withLabel("Select Carrier")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("carrier", QFieldType.STRING).withLabel("Carrier").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Collect and manifest shipments (backend)                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("manifestShipments")
            .withCode(new QCodeReference(ManifestShipmentsStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Manifest Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("manifestId", QFieldType.INTEGER).withLabel("Manifest ID"))
            .withViewField(new QFieldMetaData("totalShipments", QFieldType.INTEGER).withLabel("Total Shipments")));
   }
}
