/*******************************************************************************
 ** MetaData producer for the PauseTask process.  Sets an IN_PROGRESS task to
 ** PAUSED status and saves partial progress so the worker can resume later.
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


public class PauseTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsPauseTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Pause Task")
         .withIcon(new QIcon().withName("pause_circle"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QFrontendStepMetaData()
            .withName("enterDetails")
            .withLabel("Pause Task")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("pauseReason", QFieldType.STRING).withLabel("Reason for Pause")))
         .withStep(new QBackendStepMetaData()
            .withName("executePause")
            .withCode(new QCodeReference(PauseTaskStep.class)))
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Task Paused")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
