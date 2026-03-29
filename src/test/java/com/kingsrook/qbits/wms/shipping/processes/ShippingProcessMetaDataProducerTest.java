/*******************************************************************************
 ** Tests for shipping process MetaData producers.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ShippingProcessMetaDataProducerTest extends BaseTest
{

   /*******************************************************************************
    ** Test GenerateShippingLabel process metadata is registered.
    *******************************************************************************/
   @Test
   void testGenerateShippingLabel_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(GenerateShippingLabelProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsGenerateShippingLabel");
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ManifestShipments process metadata is registered.
    *******************************************************************************/
   @Test
   void testManifestShipments_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ManifestShipmentsProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsManifestShipments");
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ShipConfirm process metadata is registered.
    *******************************************************************************/
   @Test
   void testShipConfirm_processRegistered() throws Exception
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ShipConfirmProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getName()).isEqualTo("wmsShipConfirm");
      assertThat(process.getStepList()).isNotEmpty();
   }
}
