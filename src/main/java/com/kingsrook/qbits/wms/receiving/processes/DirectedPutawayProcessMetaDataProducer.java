/*******************************************************************************
 ** MetaData producer for the DirectedPutaway process.  A utility process
 ** (backend-only, no UI) that evaluates wmsPutawayRule records to determine
 ** the best putaway location for a given item and warehouse.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


public class DirectedPutawayProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsDirectedPutaway";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Directed Putaway")
         .withIcon(new QIcon().withName("move_to_inbox"))

         //////////////////////////////////////////////////////////////////////////
         // Single backend step -- no UI                                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("evaluatePutaway")
            .withCode(new QCodeReference(DirectedPutawayStep.class)));
   }
}
