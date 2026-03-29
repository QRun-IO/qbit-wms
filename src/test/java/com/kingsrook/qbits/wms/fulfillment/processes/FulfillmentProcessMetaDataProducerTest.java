/*******************************************************************************
 ** Tests that all Phase 3 process MetaData producers register processes
 ** correctly in the QInstance.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class FulfillmentProcessMetaDataProducerTest extends BaseTest
{

   @Test
   void testAllocateOrders_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(AllocateOrdersProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Allocate Orders");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testCreateWave_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(CreateWaveProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Create Wave");
   }



   @Test
   void testReleaseWave_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ReleaseWaveProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Release Wave");
   }



   @Test
   void testPackOrder_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(PackOrderProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Pack Order");
   }



   @Test
   void testShortPickResolution_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ShortPickResolutionProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Short Pick Resolution");
   }



   @Test
   void testCancelWave_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(CancelWaveProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Cancel Wave");
   }



   @Test
   void testHoldOrder_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(HoldOrderProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Hold Order");
   }



   @Test
   void testReleaseOrder_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ReleaseOrderProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Release Order");
   }



   @Test
   void testVoidCarton_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(VoidCartonProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Void Carton");
   }
}
