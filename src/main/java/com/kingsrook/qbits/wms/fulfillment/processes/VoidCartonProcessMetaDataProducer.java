/*******************************************************************************
 ** MetaData producer for the VoidCarton process.
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


public class VoidCartonProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsVoidCarton";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Void Carton")
         .withIcon(new QIcon().withName("delete"))

         .withStep(new QFrontendStepMetaData()
            .withName("selectCarton")
            .withLabel("Select Carton")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("cartonId", QFieldType.INTEGER).withLabel("Carton ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("reason", QFieldType.STRING).withLabel("Reason")))

         .withStep(new QBackendStepMetaData()
            .withName("voidCarton")
            .withCode(new QCodeReference(VoidCartonStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Carton Voided")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
