/*******************************************************************************
 ** MetaData producer for the ReassignTask process.  Supervisor action that
 ** changes the assignedTo field on a task.
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


public class ReassignTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReassignTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Reassign Task")
         .withIcon(new QIcon().withName("swap_horiz"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QFrontendStepMetaData()
            .withName("enterNewAssignee")
            .withLabel("Reassign Task")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("newAssignee", QFieldType.STRING).withLabel("New Assignee").withIsRequired(true)))
         .withStep(new QBackendStepMetaData()
            .withName("executeReassign")
            .withCode(new QCodeReference(ReassignTaskStep.class)))
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Reassignment Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result")));
   }
}
