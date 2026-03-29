/*******************************************************************************
 ** Unit tests for InspectReturnStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.InspectionGrade;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceipt;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class InspectReturnStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that inspecting a return receipt line updates the inspection details
    ** and sets the parent RMA status to INSPECTING.
    *******************************************************************************/
   @Test
   void testRun_inspectAndGrade_updatesReceiptLineAndRmaStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer raId = insertReturnAuthorization(warehouseId, orderId, null);

      /////////////////////////////////////////////////////////////////////////
      // Create a return receipt and receipt line                             //
      /////////////////////////////////////////////////////////////////////////
      Integer receiptId = new InsertAction().execute(new InsertInput(WmsReturnReceipt.TABLE_NAME).withRecord(new QRecord()
         .withValue("returnAuthorizationId", raId)
         .withValue("receiptNumber", "RRCPT-TEST")
         .withValue("receivedBy", "TestWorker"))).getRecords().get(0).getValueInteger("id");

      Integer receiptLineId = new InsertAction().execute(new InsertInput(WmsReturnReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("returnReceiptId", receiptId)
         .withValue("itemId", itemId)
         .withValue("quantityReceived", 5))).getRecords().get(0).getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("returnReceiptLineId", receiptLineId);
      input.addValue("inspectionGradeId", InspectionGrade.A_STOCK.getPossibleValueId());
      input.addValue("inspectionNotes", "Item in good condition");
      input.addValue("inspectedBy", "QC Inspector");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new InspectReturnStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify receipt line was updated with inspection details              //
      /////////////////////////////////////////////////////////////////////////
      QRecord receiptLine = new GetAction().execute(new GetInput(WmsReturnReceiptLine.TABLE_NAME).withPrimaryKey(receiptLineId)).getRecord();
      assertThat(receiptLine.getValueInteger("inspectionGradeId")).isEqualTo(InspectionGrade.A_STOCK.getPossibleValueId());
      assertThat(receiptLine.getValueString("inspectedBy")).isEqualTo("QC Inspector");

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA status updated to INSPECTING                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
      assertThat(ra.getValueInteger("statusId")).isEqualTo(ReturnAuthorizationStatus.INSPECTING.getPossibleValueId());

      assertThat(output.getValueString("resultMessage")).contains("Inspection recorded");
   }
}
