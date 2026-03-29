/*******************************************************************************
 ** Unit tests for all receiving process MetaDataProducers.  Verifies each
 ** producer generates valid QProcessMetaData with correct names and steps.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReceivingProcessMetaDataProducerTest extends BaseTest
{

   /*******************************************************************************
    ** Test ReceiveAgainstPOProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testReceiveAgainstPOProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ReceiveAgainstPOProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(ReceiveAgainstPOProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test BlindReceiveProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testBlindReceiveProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new BlindReceiveProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(BlindReceiveProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test DirectedPutawayProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testDirectedPutawayProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new DirectedPutawayProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(DirectedPutawayProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test QualityInspectionProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testQualityInspectionProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new QualityInspectionProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(QualityInspectionProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ReceiveASNProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testReceiveASNProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ReceiveASNProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo(ReceiveASNProcessMetaDataProducer.NAME);
      assertThat(process.getStepList()).isNotEmpty();
   }
}
