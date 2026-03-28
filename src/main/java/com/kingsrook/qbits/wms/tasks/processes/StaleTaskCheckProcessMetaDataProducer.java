/*******************************************************************************
 ** MetaData producer for the StaleTaskCheck scheduled process.  Finds ASSIGNED
 ** tasks that have been idle beyond a threshold and unassigns them, returning
 ** them to PENDING status so they can be picked up by another worker.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class StaleTaskCheckProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsStaleTaskCheck";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Stale Task Check")
         .withIcon(new QIcon().withName("schedule"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QBackendStepMetaData()
            .withName("checkStaleTasks")
            .withCode(new QCodeReference(StaleTaskCheckStep.class)));
   }
}
