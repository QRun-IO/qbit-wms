/*******************************************************************************
 ** MetaData producer for the InspectReturn process.  Provides a frontend to
 ** enter inspection details (grade, condition) for returned items and a backend
 ** step that updates the receipt lines and RMA status.
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


public class InspectReturnProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsInspectReturn";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Inspect Return")
         .withIcon(new QIcon().withName("search"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Enter inspection details                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterInspection")
            .withLabel("Inspection Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("returnReceiptLineId", QFieldType.INTEGER).withLabel("Receipt Line ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("inspectionGradeId", QFieldType.INTEGER).withLabel("Inspection Grade").withIsRequired(true))
            .withFormField(new QFieldMetaData("actualConditionId", QFieldType.INTEGER).withLabel("Actual Condition"))
            .withFormField(new QFieldMetaData("inspectionNotes", QFieldType.STRING).withLabel("Notes"))
            .withFormField(new QFieldMetaData("inspectedBy", QFieldType.STRING).withLabel("Inspected By")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Record inspection (backend)                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("recordInspection")
            .withCode(new QCodeReference(InspectReturnStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Inspection Recorded")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
