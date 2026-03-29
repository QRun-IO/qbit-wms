/*******************************************************************************
 ** Unit tests for all inventory process MetaDataProducers.
 ** Verifies each producer generates valid QProcessMetaData with correct names
 ** and steps.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class InventoryProcessMetaDataProducerTest extends BaseTest
{

   /*******************************************************************************
    ** Test InventoryAdjustmentProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testInventoryAdjustmentProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new InventoryAdjustmentProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test InventoryMoveProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testInventoryMoveProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new InventoryMoveProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test InventoryHoldProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testInventoryHoldProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new InventoryHoldProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test InventoryReleaseProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testInventoryReleaseProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new InventoryReleaseProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test CycleCountProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testCycleCountProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new CycleCountProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ApproveCountVarianceProcessMetaDataProducer.
    *******************************************************************************/
   @Test
   void testApproveCountVarianceProducer_produces_validMetaData() throws QException
   {
      QProcessMetaData process = new ApproveCountVarianceProcessMetaDataProducer().produce(QContext.getQInstance());
      assertThat(process).isNotNull();
      assertThat(process.getName()).isNotBlank();
      assertThat(process.getStepList()).isNotEmpty();
   }
}
