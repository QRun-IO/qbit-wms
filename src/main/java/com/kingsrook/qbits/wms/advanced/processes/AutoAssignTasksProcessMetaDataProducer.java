/*******************************************************************************
 ** MetaData producer for the AutoAssignTasks process.  Scheduled process that
 ** queries PENDING tasks by priority and assigns them to available workers
 ** (workers with no IN_PROGRESS tasks).
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


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


public class AutoAssignTasksProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsAutoAssignTasks";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Auto-Assign Tasks")
         .withIcon(new QIcon().withName("person_add"))

         .withStep(new QBackendStepMetaData()
            .withName("autoAssign")
            .withCode(new QCodeReference(AutoAssignTasksStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Auto-Assign Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("tasksAssigned", QFieldType.INTEGER).withLabel("Tasks Assigned")));
   }
}
