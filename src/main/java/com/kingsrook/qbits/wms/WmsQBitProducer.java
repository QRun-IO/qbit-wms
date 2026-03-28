/*******************************************************************************
 ** MetaData producer for the WMS QBit.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;


public class WmsQBitProducer implements QBitMetaDataProducer<WmsQBitConfig>
{
   private WmsQBitConfig qBitConfig;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      return new QBitMetaData()
         .withGroupId("com.kingsrook.qbits")
         .withArtifactId("qbit-wms")
         .withVersion("0.1.0")
         .withNamespace(getNamespace())
         .withConfig(getQBitConfig());
   }



   /*******************************************************************************
    ** Post-produce actions -- placeholder for table customizers, widgets, webhooks.
    *******************************************************************************/
   @Override
   public void postProduceActions(MetaDataProducerMultiOutput metaDataProducerMultiOutput, QInstance qInstance)
   {
      /////////////////////////////////////////////////////////////////////////
      // Placeholder for future phases:                                      //
      // - Register table customizers                                        //
      // - Register widgets                                                  //
      // - Register webhooks                                                 //
      /////////////////////////////////////////////////////////////////////////
   }



   /*******************************************************************************
    ** Getter for qBitConfig
    *******************************************************************************/
   @Override
   public WmsQBitConfig getQBitConfig()
   {
      return (this.qBitConfig);
   }



   /*******************************************************************************
    ** Setter for qBitConfig
    *******************************************************************************/
   public void setQBitConfig(WmsQBitConfig qBitConfig)
   {
      this.qBitConfig = qBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for qBitConfig
    *******************************************************************************/
   public WmsQBitProducer withQBitConfig(WmsQBitConfig qBitConfig)
   {
      this.qBitConfig = qBitConfig;
      return (this);
   }
}
