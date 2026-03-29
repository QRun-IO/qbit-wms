/*******************************************************************************
 ** Unit tests for all task process MetaDataProducers.
 ** Verifies each producer generates valid QProcessMetaData with correct names,
 ** labels, and backend steps.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class TaskProcessMetaDataProducerTest extends BaseTest
{

   /*******************************************************************************
    ** Test GetNextTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testGetNextTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new GetNextTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(GetNextTaskProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test CompleteTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testCompleteTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new CompleteTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test PauseTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testPauseTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new PauseTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ResumeTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testResumeTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ResumeTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ReassignTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testReassignTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ReassignTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ReprioritizeTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testReprioritizeTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ReprioritizeTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test CancelTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testCancelTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new CancelTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test HoldTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testHoldTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new HoldTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ReleaseTaskProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testReleaseTaskProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ReleaseTaskProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test StaleTaskCheckProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testStaleTaskCheckProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new StaleTaskCheckProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }
}
