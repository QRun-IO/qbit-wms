/*******************************************************************************
 ** Tests that all Phase 7 advanced process MetaData producers register
 ** processes correctly in the QInstance.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class AdvancedProcessMetaDataProducerTest extends BaseTest
{

   @Test
   void testReplenishCheck_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ReplenishCheckProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Replenishment Check");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testAbcAnalysis_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(AbcAnalysisProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("ABC Analysis");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testExpirationAlertCheck_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(ExpirationAlertCheckProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Expiration Alert Check");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testLowStockAlertCheck_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(LowStockAlertCheckProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Low Stock Alert Check");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testAutoAssignTasks_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(AutoAssignTasksProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Auto-Assign Tasks");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testAutoAllocateAndRelease_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(AutoAllocateAndReleaseProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Auto Allocate and Release");
      assertThat(process.getStepList()).isNotEmpty();
   }
}
