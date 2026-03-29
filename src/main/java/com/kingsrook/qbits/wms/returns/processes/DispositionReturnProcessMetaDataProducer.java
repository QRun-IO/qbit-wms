/*******************************************************************************
 ** MetaData producer for the DispositionReturn process.  Assigns a disposition
 ** (restock, scrap, return to vendor, etc.) to inspected return lines and
 ** creates RETURN_PUTAWAY tasks for restocked items.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


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


public class DispositionReturnProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsDispositionReturn";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Disposition Return")
         .withIcon(new QIcon().withName("rule"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Enter disposition details                                    //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterDisposition")
            .withLabel("Disposition Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("returnReceiptLineId", QFieldType.INTEGER).withLabel("Receipt Line ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("dispositionId", QFieldType.INTEGER).withLabel("Disposition").withIsRequired(true))
            .withFormField(new QFieldMetaData("dispositionLocationId", QFieldType.INTEGER).withLabel("Disposition Location")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Execute disposition (backend)                                //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeDisposition")
            .withCode(new QCodeReference(DispositionReturnStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Disposition Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
