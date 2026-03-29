/*******************************************************************************
 ** Backend step for InspectReturn.  Updates a return receipt line with
 ** inspection grade, condition, and inspector details.  Updates the parent
 ** RMA status to INSPECTING.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


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
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceipt;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class InspectReturnStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(InspectReturnStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer returnReceiptLineId = input.getValueInteger("returnReceiptLineId");
      Integer inspectionGradeId = input.getValueInteger("inspectionGradeId");
      Integer actualConditionId = input.getValueInteger("actualConditionId");
      String inspectionNotes = input.getValueString("inspectionNotes");
      String inspectedBy = input.getValueString("inspectedBy");

      if(returnReceiptLineId == null)
      {
         throw new QUserFacingException("Receipt Line ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load the receipt line                                               //
      /////////////////////////////////////////////////////////////////////////
      QRecord receiptLine = new GetAction().execute(new GetInput(WmsReturnReceiptLine.TABLE_NAME).withPrimaryKey(returnReceiptLineId)).getRecord();
      if(receiptLine == null)
      {
         throw new QUserFacingException("Return receipt line not found: " + returnReceiptLineId);
      }

      LOG.info("Recording inspection", logPair("returnReceiptLineId", returnReceiptLineId), logPair("inspectionGradeId", inspectionGradeId));

      /////////////////////////////////////////////////////////////////////////
      // Update receipt line with inspection details                         //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsReturnReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", returnReceiptLineId)
         .withValue("inspectionGradeId", inspectionGradeId)
         .withValue("actualConditionId", actualConditionId)
         .withValue("inspectionNotes", inspectionNotes)
         .withValue("inspectedBy", inspectedBy)
         .withValue("inspectedDate", Instant.now())));

      /////////////////////////////////////////////////////////////////////////
      // Update the parent RMA status to INSPECTING                          //
      /////////////////////////////////////////////////////////////////////////
      Integer returnReceiptId = receiptLine.getValueInteger("returnReceiptId");
      if(returnReceiptId != null)
      {
         QRecord receipt = new GetAction().execute(new GetInput(WmsReturnReceipt.TABLE_NAME).withPrimaryKey(returnReceiptId)).getRecord();
         if(receipt != null)
         {
            Integer raId = receipt.getValueInteger("returnAuthorizationId");
            if(raId != null)
            {
               new UpdateAction().execute(new UpdateInput(WmsReturnAuthorization.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", raId)
                  .withValue("statusId", ReturnAuthorizationStatus.INSPECTING.getPossibleValueId())));
            }
         }
      }

      output.addValue("resultMessage", "Inspection recorded for receipt line " + returnReceiptLineId + ".");
   }
}
