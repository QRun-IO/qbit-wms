/*******************************************************************************
 ** Backend step for QualityInspection.  Contains two named backend steps:
 ** "loadForInspection" loads a receipt line and validates it is pending QC,
 ** while "recordInspection" creates a QC_INSPECT task with the result and
 ** immediately completes it via the TaskCompletionDispatcher.  The
 ** QcInspectTaskCompletionHandler handles releasing or holding the PUTAWAY.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.tasks.completion.TaskCompletionDispatcher;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class QualityInspectionStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(QualityInspectionStep.class);

   public static final String RECEIPT_LINE_TABLE_NAME = "wmsReceiptLine";
   public static final String RECEIPT_TABLE_NAME      = "wmsReceipt";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String stepName = input.getStepName();

      if("loadForInspection".equals(stepName))
      {
         runLoadForInspection(input, output);
      }
      else if("recordInspection".equals(stepName))
      {
         runRecordInspection(input, output);
      }
   }



   /*******************************************************************************
    ** loadForInspection step: Load the receipt line and validate qcStatus is
    ** PENDING.
    *******************************************************************************/
   private void runLoadForInspection(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer receiptLineId = input.getValueInteger("receiptLineId");

      if(receiptLineId == null)
      {
         throw new QUserFacingException("Receipt line ID is required.");
      }

      GetOutput receiptLineGet = new GetAction().execute(new GetInput(RECEIPT_LINE_TABLE_NAME).withPrimaryKey(receiptLineId));
      QRecord receiptLine = receiptLineGet.getRecord();

      if(receiptLine == null)
      {
         throw new QUserFacingException("Receipt line not found.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Validate qcStatus is PENDING                                        //
      /////////////////////////////////////////////////////////////////////////
      Integer qcStatusId = receiptLine.getValueInteger("qcStatusId");
      if(!Objects.equals(qcStatusId, QcStatus.PENDING.getPossibleValueId()))
      {
         throw new QUserFacingException("Receipt line QC status must be Pending. Current status is not eligible for inspection.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load receipt for warehouse/client context                           //
      /////////////////////////////////////////////////////////////////////////
      Integer receiptId = receiptLine.getValueInteger("receiptId");
      GetOutput receiptGet = new GetAction().execute(new GetInput(RECEIPT_TABLE_NAME).withPrimaryKey(receiptId));
      QRecord receipt = receiptGet.getRecord();

      LOG.info("Loaded receipt line for QC inspection",
         logPair("receiptLineId", receiptLineId),
         logPair("itemId", receiptLine.getValueInteger("itemId")),
         logPair("qcStatusId", qcStatusId));

      output.addValue("receiptLineId", receiptLineId);
      output.addValue("receiptId", receiptId);
      output.addValue("itemId", receiptLine.getValueInteger("itemId"));
      output.addValue("quantityReceived", receiptLine.getValueInteger("quantityReceived"));
      output.addValue("lotNumber", receiptLine.getValueString("lotNumber"));
      output.addValue("warehouseId", receipt != null ? receipt.getValueInteger("warehouseId") : null);
      output.addValue("clientId", receipt != null ? receipt.getValueInteger("clientId") : null);
   }



   /*******************************************************************************
    ** recordInspection step: Create a QC_INSPECT task with the inspection result
    ** and immediately complete it via the TaskCompletionDispatcher.
    *******************************************************************************/
   private void runRecordInspection(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer receiptLineId = input.getValueInteger("receiptLineId");
      Integer receiptId = input.getValueInteger("receiptId");
      Integer itemId = input.getValueInteger("itemId");
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer clientId = input.getValueInteger("clientId");
      String inspectionResult = input.getValueString("inspectionResult");
      String notes = input.getValueString("notes");

      if(inspectionResult == null || inspectionResult.isBlank())
      {
         throw new QUserFacingException("Inspection result is required (pass or fail).");
      }

      Boolean passed = "pass".equalsIgnoreCase(inspectionResult);

      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Update receipt line QC status                                       //
      /////////////////////////////////////////////////////////////////////////
      Integer newQcStatusId = passed
         ? QcStatus.PASSED.getPossibleValueId()
         : QcStatus.FAILED.getPossibleValueId();

      new UpdateAction().execute(new UpdateInput(RECEIPT_LINE_TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("qcStatusId", newQcStatusId)));

      /////////////////////////////////////////////////////////////////////////
      // Create QC_INSPECT task and immediately complete it                   //
      // (QC is an office action, not a floor task)                          //
      /////////////////////////////////////////////////////////////////////////
      InsertOutput taskInsert = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("taskTypeId", TaskType.QC_INSPECT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("receiptId", receiptId)
         .withValue("receiptLineId", receiptLineId)
         .withValue("referenceType", "RECEIPT_LINE")
         .withValue("referenceId", receiptLineId)
         .withValue("completedDate", Instant.now())
         .withValue("completedBy", performedBy)
         .withValue("notes", "QC Inspection: " + inspectionResult + (notes != null ? " - " + notes : ""))));

      QRecord taskRecord = taskInsert.getRecords().get(0);

      LOG.info("QC inspection recorded, dispatching completion",
         logPair("receiptLineId", receiptLineId),
         logPair("result", inspectionResult),
         logPair("taskId", taskRecord.getValueInteger("id")));

      /////////////////////////////////////////////////////////////////////////
      // Dispatch to the QcInspectTaskCompletionHandler, which handles       //
      // releasing PUTAWAY tasks (if passed) or keeping them on hold         //
      /////////////////////////////////////////////////////////////////////////
      TaskCompletionDispatcher.complete(taskRecord);

      output.addValue("resultMessage", "Quality inspection recorded: " + inspectionResult);
      output.addValue("inspectionResult", inspectionResult);
      output.addValue("receiptLineId", receiptLineId);
   }
}
