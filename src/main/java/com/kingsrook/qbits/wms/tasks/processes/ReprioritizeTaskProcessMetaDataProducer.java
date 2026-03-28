/*******************************************************************************
 ** MetaData producer for the ReprioritizeTask process.  Supervisor action that
 ** changes the priority on one or more tasks.
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


public class ReprioritizeTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReprioritizeTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Reprioritize Task")
         .withIcon(new QIcon().withName("low_priority"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QFrontendStepMetaData()
            .withName("enterPriority")
            .withLabel("Set New Priority")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("newPriority", QFieldType.INTEGER).withLabel("New Priority (1-9)").withIsRequired(true)))
         .withStep(new QBackendStepMetaData()
            .withName("executePriority")
            .withCode(new QCodeReference(ReprioritizeTaskStep.class)))
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Priority Updated")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
