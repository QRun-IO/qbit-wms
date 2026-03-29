/*******************************************************************************
 ** Unit tests for {@link BlindReceiveStep}.
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


class BlindReceiveStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test successful blind receive creates receipt, receipt line, transaction,
    ** and PUTAWAY task.
    *******************************************************************************/
   @Test
   void testRun_validInput_createsAllRecords() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItemWithBarcode("SKU-BLIND", "Blind Item", "UPC-BLIND-001");
      Integer zoneId = insertZone(warehouseId);
      insertLocation(warehouseId, zoneId, "LOC-BLIND-001");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      input.addValue("itemBarcode", "UPC-BLIND-001");
      input.addValue("quantity", 5);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new BlindReceiveStep().run(input, output);

      assertThat(output.getValueString("resultMessage")).contains("successfully");
      assertThat(output.getValueInteger("receiptId")).isNotNull();
      assertThat(output.getValueInteger("receiptLineId")).isNotNull();

      ///////////////////////////////////////////////////////////////////
      // Verify receipt was created (blind type, no PO)                //
      ///////////////////////////////////////////////////////////////////
      QueryOutput receiptQuery = new QueryAction().execute(new QueryInput(WmsReceipt.TABLE_NAME));
      assertThat(receiptQuery.getRecords()).hasSize(1);
      assertThat(receiptQuery.getRecords().get(0).getValueInteger("purchaseOrderId")).isNull();

      ///////////////////////////////////////////////////////////////////
      // Verify receipt line was created                               //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME));
      assertThat(lineQuery.getRecords()).hasSize(1);
      assertThat(lineQuery.getRecords().get(0).getValueInteger("quantityReceived")).isEqualTo(5);

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
    ** Test missing barcode throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_missingBarcode_throwsUserFacingException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", 1);
      input.addValue("quantity", 10);

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new BlindReceiveStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("barcode is required");
   }



   /*******************************************************************************
    ** Test missing warehouse throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_missingWarehouse_throwsUserFacingException() throws QException
   {
      insertItemWithBarcode("SKU-NO-WH", "No Warehouse Item", "UPC-NO-WH");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemBarcode", "UPC-NO-WH");
      input.addValue("quantity", 10);

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new BlindReceiveStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Warehouse is required");
   }



   /*******************************************************************************
    ** Test zero quantity throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_zeroQuantity_throwsUserFacingException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", 1);
      input.addValue("itemBarcode", "SOME-UPC");
      input.addValue("quantity", 0);

      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new BlindReceiveStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Quantity must be greater than zero");
   }
}
