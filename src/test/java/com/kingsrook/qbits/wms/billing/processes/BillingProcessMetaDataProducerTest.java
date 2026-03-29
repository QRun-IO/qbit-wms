/*******************************************************************************
 ** Tests that all Phase 6 billing process MetaData producers register processes
 ** correctly in the QInstance.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class BillingProcessMetaDataProducerTest extends BaseTest
{

   @Test
   void testGenerateInvoice_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(GenerateInvoiceProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Generate Invoice");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testStorageSnapshot_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(StorageSnapshotProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Storage Snapshot");
      assertThat(process.getStepList()).isNotEmpty();
   }



   @Test
   void testSyncInvoiceToAccounting_processRegistered()
   {
      QProcessMetaData process = QContext.getQInstance().getProcess(SyncInvoiceToAccountingProcessMetaDataProducer.NAME);
      assertThat(process).isNotNull();
      assertThat(process.getLabel()).isEqualTo("Sync Invoice to Accounting");
      assertThat(process.getStepList()).isNotEmpty();
   }
}
