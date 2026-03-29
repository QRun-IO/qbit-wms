/*******************************************************************************
 ** MetaData producer for the GenerateShippingLabel process.  Provides steps to
 ** select a packed carton, choose carrier/service, generate a label, and create
 ** the shipment and shipment-order records.
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


public class GenerateShippingLabelProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsGenerateShippingLabel";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Generate Shipping Label")
         .withIcon(new QIcon().withName("label"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select carton to label                                       //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectCarton")
            .withLabel("Select Carton")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("cartonId", QFieldType.INTEGER).withLabel("Carton ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("carrier", QFieldType.STRING).withLabel("Carrier").withIsRequired(true))
            .withFormField(new QFieldMetaData("serviceLevel", QFieldType.STRING).withLabel("Service Level")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Generate label (backend)                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("generateLabel")
            .withCode(new QCodeReference(GenerateShippingLabelStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Show label result                                            //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showLabel")
            .withLabel("Label Generated")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("shipmentId", QFieldType.INTEGER).withLabel("Shipment ID"))
            .withViewField(new QFieldMetaData("trackingNumber", QFieldType.STRING).withLabel("Tracking Number")));
   }
}
