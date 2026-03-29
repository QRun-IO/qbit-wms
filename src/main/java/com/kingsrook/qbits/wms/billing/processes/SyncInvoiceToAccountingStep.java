/*******************************************************************************
 ** Backend step for SyncInvoiceToAccounting.  Updates invoice status to SENT
 ** and sets the sentDate.  This is a placeholder for actual accounting system
 ** integration (e.g., QuickBooks, Xero, NetSuite).
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.billing.model.WmsInvoice;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class SyncInvoiceToAccountingStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(SyncInvoiceToAccountingStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer invoiceId = input.getValueInteger("invoiceId");

      if(invoiceId == null)
      {
         throw new QUserFacingException("Invoice ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Fetch the invoice                                                   //
      /////////////////////////////////////////////////////////////////////////
      QRecord invoice = new GetAction().execute(new GetInput(WmsInvoice.TABLE_NAME).withPrimaryKey(invoiceId)).getRecord();
      if(invoice == null)
      {
         throw new QUserFacingException("Invoice not found with ID: " + invoiceId);
      }

      Integer currentStatus = invoice.getValueInteger("statusId");
      if(currentStatus != null && currentStatus.equals(InvoiceStatus.SENT.getPossibleValueId()))
      {
         output.addValue("resultMessage", "Invoice " + invoiceId + " has already been sent.");
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Update status to SENT and set sentDate                              //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsInvoice.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invoiceId)
         .withValue("statusId", InvoiceStatus.SENT.getPossibleValueId())
         .withValue("sentDate", Instant.now())));

      LOG.info("Invoice synced to accounting", logPair("invoiceId", invoiceId));

      output.addValue("resultMessage", "Invoice " + invoiceId + " has been marked as SENT and synced to accounting.");
   }
}
