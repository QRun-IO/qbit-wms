/*******************************************************************************
 ** Unit tests for QualityInspectionStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class QualityInspectionStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test loadForInspection with missing receiptLineId throws exception.
    *******************************************************************************/
   @Test
   void testLoadForInspection_missingReceiptLineId_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadForInspection");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new QualityInspectionStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Receipt line ID is required");
   }



   /*******************************************************************************
    ** Test loadForInspection with non-existent receipt line throws exception.
    *******************************************************************************/
   @Test
   void testLoadForInspection_nonExistentReceiptLine_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadForInspection");
      input.addValue("receiptLineId", 99999);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new QualityInspectionStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("not found");
   }



   /*******************************************************************************
    ** Test loadForInspection with receipt line that has QC status PENDING works.
    *******************************************************************************/
   @Test
   void testLoadForInspection_pendingQcStatus_success() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 20);

      /////////////////////////////////////////////////////////////////////////
      // Set QC status to PENDING                                            //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("qcStatusId", QcStatus.PENDING.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadForInspection");
      input.addValue("receiptLineId", receiptLineId);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new QualityInspectionStep().run(input, output);

      assertThat(output.getValueInteger("receiptLineId")).isEqualTo(receiptLineId);
      assertThat(output.getValueInteger("itemId")).isEqualTo(itemId);
      assertThat(output.getValueInteger("receiptId")).isEqualTo(receiptId);
   }



   /*******************************************************************************
    ** Test loadForInspection with non-PENDING QC status throws exception.
    *******************************************************************************/
   @Test
   void testLoadForInspection_notPendingQcStatus_throwsException() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 20);

      /////////////////////////////////////////////////////////////////////////
      // Set QC status to NOT_REQUIRED (not PENDING)                        //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("qcStatusId", QcStatus.NOT_REQUIRED.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadForInspection");
      input.addValue("receiptLineId", receiptLineId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new QualityInspectionStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("must be Pending");
   }



   /*******************************************************************************
    ** Test recordInspection with missing result throws exception.
    *******************************************************************************/
   @Test
   void testRecordInspection_missingResult_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("recordInspection");
      input.addValue("receiptLineId", 1);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new QualityInspectionStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Inspection result is required");
   }



   /*******************************************************************************
    ** Test recordInspection with PASS result updates QC status and creates task.
    *******************************************************************************/
   @Test
   void testRecordInspection_passResult_updatesQcStatusToPassedAndCreatesTask() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 20);

      new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("qcStatusId", QcStatus.PENDING.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("recordInspection");
      input.addValue("receiptLineId", receiptLineId);
      input.addValue("receiptId", receiptId);
      input.addValue("itemId", itemId);
      input.addValue("warehouseId", warehouseId);
      input.addValue("inspectionResult", "pass");
      input.addValue("notes", "Looks good");
      RunBackendStepOutput output = new RunBackendStepOutput();
      new QualityInspectionStep().run(input, output);

      assertThat(output.getValueString("inspectionResult")).isEqualTo("pass");
      assertThat(output.getValueString("resultMessage")).contains("pass");

      /////////////////////////////////////////////////////////////////////////
      // Verify QC status updated to PASSED                                 //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsReceiptLine.TABLE_NAME).withPrimaryKey(receiptLineId)).getRecord();
      assertThat(line.getValueInteger("qcStatusId")).isEqualTo(QcStatus.PASSED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify QC_INSPECT task was created                                 //
      /////////////////////////////////////////////////////////////////////////
      var taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(taskQuery.getRecords()).isNotEmpty();
      assertThat(taskQuery.getRecords().get(0).getValueInteger("taskTypeId"))
         .isEqualTo(TaskType.QC_INSPECT.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test recordInspection with FAIL result updates QC status to FAILED.
    *******************************************************************************/
   @Test
   void testRecordInspection_failResult_updatesQcStatusToFailed() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 20);

      new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("qcStatusId", QcStatus.PENDING.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("recordInspection");
      input.addValue("receiptLineId", receiptLineId);
      input.addValue("receiptId", receiptId);
      input.addValue("itemId", itemId);
      input.addValue("warehouseId", warehouseId);
      input.addValue("inspectionResult", "fail");
      RunBackendStepOutput output = new RunBackendStepOutput();
      new QualityInspectionStep().run(input, output);

      assertThat(output.getValueString("inspectionResult")).isEqualTo("fail");

      QRecord line = new GetAction().execute(new GetInput(WmsReceiptLine.TABLE_NAME).withPrimaryKey(receiptLineId)).getRecord();
      assertThat(line.getValueInteger("qcStatusId")).isEqualTo(QcStatus.FAILED.getPossibleValueId());
   }
}
