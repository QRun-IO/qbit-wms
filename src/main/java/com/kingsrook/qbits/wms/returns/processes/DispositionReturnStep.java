/*******************************************************************************
 ** Backend step for DispositionReturn.  Sets disposition on a return receipt
 ** line.  For RESTOCK dispositions, creates a RETURN_PUTAWAY task.  For SCRAP,
 ** inserts a SCRAP transaction (perpetual inventory).  Updates the parent RMA
 ** status to DISPOSITIONED.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.Disposition;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceipt;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class DispositionReturnStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(DispositionReturnStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer returnReceiptLineId = input.getValueInteger("returnReceiptLineId");
      Integer dispositionId = input.getValueInteger("dispositionId");
      Integer dispositionLocationId = input.getValueInteger("dispositionLocationId");

      if(returnReceiptLineId == null)
      {
         throw new QUserFacingException("Receipt Line ID is required.");
      }

      if(dispositionId == null)
      {
         throw new QUserFacingException("Disposition is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load the receipt line                                               //
      /////////////////////////////////////////////////////////////////////////
      QRecord receiptLine = new GetAction().execute(new GetInput(WmsReturnReceiptLine.TABLE_NAME).withPrimaryKey(returnReceiptLineId)).getRecord();
      if(receiptLine == null)
      {
         throw new QUserFacingException("Return receipt line not found: " + returnReceiptLineId);
      }

      Integer itemId = receiptLine.getValueInteger("itemId");
      Integer quantityReceived = receiptLine.getValueInteger("quantityReceived");

      LOG.info("Dispositioning return line", logPair("returnReceiptLineId", returnReceiptLineId), logPair("dispositionId", dispositionId));

      /////////////////////////////////////////////////////////////////////////
      // Update receipt line with disposition                                 //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsReturnReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", returnReceiptLineId)
         .withValue("dispositionId", dispositionId)
         .withValue("dispositionLocationId", dispositionLocationId)));

      /////////////////////////////////////////////////////////////////////////
      // Determine the warehouse from the parent receipt -> RMA              //
      /////////////////////////////////////////////////////////////////////////
      Integer returnReceiptId = receiptLine.getValueInteger("returnReceiptId");
      Integer warehouseId = null;
      Integer clientId = null;
      Integer raId = null;

      if(returnReceiptId != null)
      {
         QRecord receipt = new GetAction().execute(new GetInput(WmsReturnReceipt.TABLE_NAME).withPrimaryKey(returnReceiptId)).getRecord();
         if(receipt != null)
         {
            raId = receipt.getValueInteger("returnAuthorizationId");
            if(raId != null)
            {
               QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
               if(ra != null)
               {
                  warehouseId = ra.getValueInteger("warehouseId");
                  clientId = ra.getValueInteger("clientId");
               }
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Handle disposition action                                           //
      /////////////////////////////////////////////////////////////////////////
      if(Objects.equals(dispositionId, Disposition.RESTOCK.getPossibleValueId())
         || Objects.equals(dispositionId, Disposition.RESTOCK_SECONDARY.getPossibleValueId()))
      {
         //////////////////////////////////////////////////////////////////
         // Create a RETURN_PUTAWAY task                                //
         //////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.RETURN_PUTAWAY.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", itemId)
            .withValue("destinationLocationId", dispositionLocationId)
            .withValue("quantityRequested", quantityReceived != null ? new BigDecimal(quantityReceived) : BigDecimal.ONE)
            .withValue("referenceType", "RETURN_AUTHORIZATION")
            .withValue("referenceId", raId)
            .withValue("inspectionGradeId", receiptLine.getValueInteger("inspectionGradeId"))
            .withValue("notes", "Return putaway for receipt line " + returnReceiptLineId)));
      }
      else if(Objects.equals(dispositionId, Disposition.SCRAP.getPossibleValueId()))
      {
         //////////////////////////////////////////////////////////////////
         // Insert SCRAP transaction (perpetual inventory)              //
         //////////////////////////////////////////////////////////////////
         if(warehouseId != null && itemId != null && quantityReceived != null)
         {
            new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
               .withValue("warehouseId", warehouseId)
               .withValue("clientId", clientId)
               .withValue("itemId", itemId)
               .withValue("transactionTypeId", TransactionType.SCRAP.getPossibleValueId())
               .withValue("quantity", new BigDecimal(quantityReceived))
               .withValue("performedDate", Instant.now())
               .withValue("notes", "Scrap disposition for return receipt line " + returnReceiptLineId)));
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Update RMA status to DISPOSITIONED                                  //
      /////////////////////////////////////////////////////////////////////////
      if(raId != null)
      {
         new UpdateAction().execute(new UpdateInput(WmsReturnAuthorization.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", raId)
            .withValue("statusId", ReturnAuthorizationStatus.DISPOSITIONED.getPossibleValueId())));
      }

      output.addValue("resultMessage", "Disposition recorded for receipt line " + returnReceiptLineId + ".");
   }
}
