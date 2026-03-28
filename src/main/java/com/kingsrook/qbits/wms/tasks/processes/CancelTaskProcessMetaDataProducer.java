/*******************************************************************************
 ** MetaData producer for the CancelTask process.  Cancels a task and handles
 ** type-specific rollback (e.g., deallocating inventory for PICK tasks).
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
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
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class CancelTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsCancelTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Cancel Task")
         .withIcon(new QIcon().withName("cancel"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QFrontendStepMetaData()
            .withName("enterReason")
            .withLabel("Cancel Task")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("cancellationReason", QFieldType.STRING).withLabel("Cancellation Reason").withIsRequired(true)))
         .withStep(new QBackendStepMetaData()
            .withName("executeCancel")
            .withCode(new QCodeReference(CancelTaskStep.class)))
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Task Cancelled")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
