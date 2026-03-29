/*******************************************************************************
 ** MetaData producer for the QualityInspection process.  Allows QC staff
 ** to inspect received items, record pass/fail results, and trigger
 ** downstream PUTAWAY task release or hold via the TaskCompletionDispatcher.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


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


public class QualityInspectionProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsQualityInspection";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Quality Inspection")
         .withIcon(new QIcon().withName("fact_check"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select receipt line for inspection                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectReceiptLine")
            .withLabel("Select Receipt Line")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("receiptLineId", QFieldType.INTEGER).withLabel("Receipt Line ID").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Load for inspection (backend)                                //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("loadForInspection")
            .withCode(new QCodeReference(QualityInspectionStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Perform inspection                                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("performInspection")
            .withLabel("Perform Inspection")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("itemId", QFieldType.INTEGER).withLabel("Item").withIsEditable(false))
            .withFormField(new QFieldMetaData("quantityReceived", QFieldType.INTEGER).withLabel("Quantity Received").withIsEditable(false))
            .withFormField(new QFieldMetaData("lotNumber", QFieldType.STRING).withLabel("Lot Number").withIsEditable(false))
            .withFormField(new QFieldMetaData("inspectionResult", QFieldType.STRING).withLabel("Result (pass/fail)").withIsRequired(true))
            .withFormField(new QFieldMetaData("notes", QFieldType.STRING).withLabel("Notes")))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Record inspection result (backend)                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("recordInspection")
            .withCode(new QCodeReference(QualityInspectionStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 5: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Inspection Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("inspectionResult", QFieldType.STRING).withLabel("Inspection Result"))
            .withViewField(new QFieldMetaData("receiptLineId", QFieldType.INTEGER).withLabel("Receipt Line ID")));
   }
}
