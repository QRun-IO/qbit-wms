/*******************************************************************************
 ** MetaData producer for the ApproveCountVariance process.  Supervisor reviews
 ** cycle count variances and either approves them (adjusting inventory) or
 ** rejects them (triggering a recount).
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


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
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;


public class ApproveCountVarianceProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsApproveCountVariance";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Approve Count Variance")
         .withIcon(new QIcon().withName("fact_check"))
         .withTableName(WmsCycleCount.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select cycle count to review                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectCycleCount")
            .withLabel("Select Cycle Count")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("cycleCountId", QFieldType.INTEGER).withLabel("Cycle Count ID").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Review variances (display lines with variance)               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("reviewVariances")
            .withLabel("Review Variances")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("approveAll", QFieldType.BOOLEAN).withLabel("Approve All Variances").withDefaultValue(false))
            .withFormField(new QFieldMetaData("rejectLineIds", QFieldType.STRING).withLabel("Line IDs to Reject (comma-separated)")))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Apply decisions                                              //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("applyDecisions")
            .withCode(new QCodeReference(ApproveCountVarianceStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Variance Review Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("approvedCount", QFieldType.INTEGER).withLabel("Lines Approved"))
            .withViewField(new QFieldMetaData("rejectedCount", QFieldType.INTEGER).withLabel("Recounts Requested")));
   }
}
