/*******************************************************************************
 ** Tests for returns process MetaData producers.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReturnsProcessMetaDataProducerTest extends BaseTest
{

   @Test
   void testCreateRMA_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(CreateRMAProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsCreateRMA");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testReceiveReturn_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ReceiveReturnProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsReceiveReturn");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testInspectReturn_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(InspectReturnProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsInspectReturn");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testDispositionReturn_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(DispositionReturnProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsDispositionReturn");
      assertThat(process.getStepList()).isNotEmpty();
   }
}
