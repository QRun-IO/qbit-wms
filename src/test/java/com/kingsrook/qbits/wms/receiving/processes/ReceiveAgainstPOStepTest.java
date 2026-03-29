/*******************************************************************************
 ** Unit tests for {@link ReceiveAgainstPOStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsReceipt;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ReceiveAgainstPOStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test loadPO step with valid PO number returns PO data.
    *******************************************************************************/
   @Test
   void testLoadPO_validPoNumber_returnsPOData() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId, "PO-LOAD-001");
      Integer itemId = insertItem();
      insertPurchaseOrderLine(poId, itemId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadPO");
      input.addValue("poNumber", "PO-LOAD-001");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReceiveAgainstPOStep().run(input, output);

      assertThat(output.getValueInteger("purchaseOrderId")).isEqualTo(poId);
      assertThat(output.getValueString("poNumber")).isEqualTo("PO-LOAD-001");
      assertThat(output.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test loadPO step with non-existent PO throws user-facing exception.
    *******************************************************************************/
   @Test
   void testLoadPO_missingPO_throwsUserFacingException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("loadPO");
      input.addValue("poNumber", "DOES-NOT-EXIST");

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReceiveAgainstPOStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Purchase order not found");
   }



   /*******************************************************************************
    ** Test processReceipt step creates receipt, receipt line, transaction, and
    ** PUTAWAY task.
    *******************************************************************************/
   @Test
   void testProcessReceipt_validInput_createsAllRecords() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId, "PO-RCV-001");
      Integer itemId = insertItemWithBarcode("SKU-RCV", "Receivable Item", "UPC-RCV-001");
      insertPurchaseOrderLine(poId, itemId, 100);
      Integer zoneId = insertZone(warehouseId);
      insertLocation(warehouseId, zoneId, "LOC-RCV-001");

      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("processReceipt");
      input.addValue("purchaseOrderId", poId);
      input.addValue("warehouseId", warehouseId);
      input.addValue("itemBarcode", "UPC-RCV-001");
      input.addValue("quantity", 10);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReceiveAgainstPOStep().run(input, output);

      assertThat(output.getValueString("resultMessage")).contains("successfully");
      assertThat(output.getValueInteger("receiptId")).isNotNull();
      assertThat(output.getValueInteger("receiptLineId")).isNotNull();

      ///////////////////////////////////////////////////////////////////
      // Verify receipt was created                                    //
      ///////////////////////////////////////////////////////////////////
      QueryOutput receiptQuery = new QueryAction().execute(new QueryInput(WmsReceipt.TABLE_NAME));
      assertThat(receiptQuery.getRecords()).hasSize(1);

      ///////////////////////////////////////////////////////////////////
      // Verify receipt line was created                               //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME));
      assertThat(lineQuery.getRecords()).hasSize(1);

      ///////////////////////////////////////////////////////////////////
      // Verify inventory transaction was created                      //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);

      ///////////////////////////////////////////////////////////////////
      // Verify PUTAWAY task was created                               //
      ///////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(taskQuery.getRecords()).hasSize(1);
      assertThat(taskQuery.getRecords().get(0).getValueInteger("taskTypeId"))
         .isEqualTo(TaskType.PUTAWAY.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test processReceipt with missing barcode throws user-facing exception.
    *******************************************************************************/
   @Test
   void testProcessReceipt_missingBarcode_throwsUserFacingException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("processReceipt");
      input.addValue("purchaseOrderId", 1);
      input.addValue("warehouseId", 1);
      input.addValue("quantity", 10);

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReceiveAgainstPOStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("barcode is required");
   }



   /*******************************************************************************
    ** Test processReceipt with zero quantity throws user-facing exception.
    *******************************************************************************/
   @Test
   void testProcessReceipt_zeroQuantity_throwsUserFacingException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setStepName("processReceipt");
      input.addValue("purchaseOrderId", 1);
      input.addValue("warehouseId", 1);
      input.addValue("itemBarcode", "SOME-UPC");
      input.addValue("quantity", 0);

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ReceiveAgainstPOStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Quantity must be greater than zero");
   }
}
