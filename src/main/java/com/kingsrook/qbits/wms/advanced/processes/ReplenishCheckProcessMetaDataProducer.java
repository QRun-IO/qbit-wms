/*******************************************************************************
 ** MetaData producer for the ReplenishCheck process.  Scheduled process that
 ** queries active replenishment rules, compares pick location inventory to min
 ** threshold, and creates REPLENISH tasks for items below minimum.
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


public class ReplenishCheckProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReplenishCheck";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Replenishment Check")
         .withIcon(new QIcon().withName("autorenew"))

         .withStep(new QBackendStepMetaData()
            .withName("checkReplenishment")
            .withCode(new QCodeReference(ReplenishCheckStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Replenishment Check Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("tasksCreated", QFieldType.INTEGER).withLabel("Tasks Created")));
   }
}
