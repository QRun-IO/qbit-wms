/*******************************************************************************
 ** MetaData producer for the PackOrder process.  Multi-step flow: load order,
 ** scan items into cartons, then complete the carton.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


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


public class PackOrderProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsPackOrder";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Pack Order")
         .withIcon(new QIcon().withName("inventory_2"))

         .withStep(new QFrontendStepMetaData()
            .withName("selectOrder")
            .withLabel("Select Order")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("orderId", QFieldType.INTEGER).withLabel("Order ID").withIsRequired(true)))

         .withStep(new QBackendStepMetaData()
            .withName("loadOrder")
            .withCode(new QCodeReference(PackOrderLoadStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("scanItems")
            .withLabel("Scan Items into Carton")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("cartonId", QFieldType.INTEGER).withLabel("Carton ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("itemBarcode", QFieldType.STRING).withLabel("Item Barcode").withIsRequired(true))
            .withFormField(new QFieldMetaData("quantity", QFieldType.INTEGER).withLabel("Quantity").withIsRequired(true)))

         .withStep(new QBackendStepMetaData()
            .withName("scanItem")
            .withCode(new QCodeReference(PackOrderScanStep.class)))

         .withStep(new QBackendStepMetaData()
            .withName("completeCarton")
            .withCode(new QCodeReference(PackOrderCompleteStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Pack Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
