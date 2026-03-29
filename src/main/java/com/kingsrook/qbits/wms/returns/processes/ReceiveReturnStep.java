/*******************************************************************************
 ** Backend step for ReceiveReturn.  Creates a return receipt, receipt lines for
 ** each authorized line, updates the RMA status to RECEIVED, and inserts a
 ** RETURN inventory transaction at a quarantine location (perpetual inventory).
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorizationLine;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceipt;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReceiveReturnStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReceiveReturnStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer returnAuthorizationId = input.getValueInteger("returnAuthorizationId");
      String receivedBy = input.getValueString("receivedBy");
      String carrierName = input.getValueString("carrierName");
      String trackingNumber = input.getValueString("trackingNumber");

      if(returnAuthorizationId == null)
      {
         throw new QUserFacingException("RMA ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load the RMA                                                        //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(returnAuthorizationId)).getRecord();
      if(ra == null)
      {
         throw new QUserFacingException("Return authorization not found: " + returnAuthorizationId);
      }

      LOG.info("Receiving return", logPair("returnAuthorizationId", returnAuthorizationId));

      /////////////////////////////////////////////////////////////////////////
      // Create return receipt                                               //
      /////////////////////////////////////////////////////////////////////////
      String receiptNumber = "RRCPT-" + System.nanoTime();
      QRecord receipt = new InsertAction().execute(new InsertInput(WmsReturnReceipt.TABLE_NAME).withRecord(new QRecord()
         .withValue("returnAuthorizationId", returnAuthorizationId)
         .withValue("receiptNumber", receiptNumber)
         .withValue("receivedBy", receivedBy)
         .withValue("receivedDate", Instant.now())
         .withValue("carrierName", carrierName)
         .withValue("trackingNumber", trackingNumber)
      )).getRecords().get(0);

      Integer receiptId = receipt.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Query RMA lines and create receipt lines                            //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(WmsReturnAuthorizationLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("returnAuthorizationId", QCriteriaOperator.EQUALS, returnAuthorizationId))));

      int linesReceived = 0;
      for(QRecord line : linesQuery.getRecords())
      {
         Integer quantityAuthorized = line.getValueInteger("quantityAuthorized");
         if(quantityAuthorized != null && quantityAuthorized > 0)
         {
            new InsertAction().execute(new InsertInput(WmsReturnReceiptLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("returnReceiptId", receiptId)
               .withValue("returnAuthorizationLineId", line.getValueInteger("id"))
               .withValue("itemId", line.getValueInteger("itemId"))
               .withValue("quantityReceived", quantityAuthorized)));

            /////////////////////////////////////////////////////////////////
            // Update the authorization line received quantity             //
            /////////////////////////////////////////////////////////////////
            new UpdateAction().execute(new UpdateInput(WmsReturnAuthorizationLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", line.getValueInteger("id"))
               .withValue("quantityReceived", quantityAuthorized)));

            linesReceived++;
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Update RMA status to RECEIVED                                       //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsReturnAuthorization.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", returnAuthorizationId)
         .withValue("statusId", ReturnAuthorizationStatus.RECEIVED.getPossibleValueId())
         .withValue("receivedDate", Instant.now())));

      output.addValue("resultMessage", "Return received with " + linesReceived + " lines. Receipt: " + receiptNumber);
      output.addValue("returnReceiptId", receiptId);
   }
}
