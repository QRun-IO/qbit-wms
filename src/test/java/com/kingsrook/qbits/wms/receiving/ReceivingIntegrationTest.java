/*******************************************************************************
 ** Integration test for the full receiving flow: create a PO, receive against
 ** it, run QC inspection (pass), complete putaway, and verify the end state
 ** of inventory and all records in the chain.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.ConditionCode;
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsPurchaseOrder;
import com.kingsrook.qbits.wms.receiving.model.WmsReceipt;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import com.kingsrook.qbits.wms.receiving.processes.ReceiveAgainstPOStep;
import com.kingsrook.qbits.wms.tasks.completion.PutawayTaskCompletionHandler;
import com.kingsrook.qbits.wms.tasks.completion.QcInspectTaskCompletionHandler;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReceivingIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** Full receiving flow without QC: PO -> receive -> putaway -> inventory
    ** at destination.
    *******************************************************************************/
   @Test
   void testFullFlow_noPOWithoutQC_endsWithInventoryAtDestination() throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Setup: warehouse, zone, location, vendor, PO, PO line, item        //
      /////////////////////////////////////////////////////////////////////////
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId, "Putaway Zone", "PZ01");
      Integer locationId = insertLocation(warehouseId, zoneId, "LOC-INT-001");
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId, "PO-INT-001");
      Integer itemId = insertItemWithBarcode("SKU-INT", "Integration Item", "UPC-INT-001");
      insertPurchaseOrderLine(poId, itemId, 50);

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Load PO                                                     //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput loadInput = new RunBackendStepInput();
      loadInput.setStepName("loadPO");
      loadInput.addValue("poNumber", "PO-INT-001");

      RunBackendStepOutput loadOutput = new RunBackendStepOutput();
      new ReceiveAgainstPOStep().run(loadInput, loadOutput);

      assertThat(loadOutput.getValueInteger("purchaseOrderId")).isEqualTo(poId);

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Process receipt (no damaged condition, so no QC required)   //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput receiveInput = new RunBackendStepInput();
      receiveInput.setStepName("processReceipt");
      receiveInput.addValue("purchaseOrderId", poId);
      receiveInput.addValue("warehouseId", warehouseId);
      receiveInput.addValue("itemBarcode", "UPC-INT-001");
      receiveInput.addValue("quantity", 50);

      RunBackendStepOutput receiveOutput = new RunBackendStepOutput();
      new ReceiveAgainstPOStep().run(receiveInput, receiveOutput);

      Integer receiptId = receiveOutput.getValueInteger("receiptId");
      assertThat(receiptId).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Complete the PUTAWAY task                                   //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PUTAWAY.getPossibleValueId()))));

      assertThat(taskQuery.getRecords()).hasSize(1);
      QRecord putawayTask = taskQuery.getRecords().get(0);
      assertThat(putawayTask.getValueInteger("taskStatusId"))
         .isEqualTo(TaskStatus.PENDING.getPossibleValueId());

      putawayTask.setValue("quantityCompleted", new BigDecimal("50"));
      putawayTask.setValue("completedBy", "putaway-user");
      putawayTask.setValue("receiptId", receiptId);
      putawayTask.setValue("receiptLineId", receiveOutput.getValueInteger("receiptLineId"));

      new PutawayTaskCompletionHandler().handle(putawayTask);

      /////////////////////////////////////////////////////////////////////////
      // Verify: inventory exists at the destination location                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      assertThat(invQuery.getRecords()).isNotEmpty();

      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("50"));

      /////////////////////////////////////////////////////////////////////////
      // Verify: PO status updated to RECEIVED                              //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput poQuery = new QueryAction().execute(new QueryInput(WmsPurchaseOrder.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, poId))));

      assertThat(poQuery.getRecords().get(0).getValueInteger("statusId"))
         .isEqualTo(PurchaseOrderStatus.RECEIVED.getPossibleValueId());
   }



   /*******************************************************************************
    ** Flow with QC: receive damaged item -> QC inspect PASS -> putaway released.
    *******************************************************************************/
   @Test
   void testFullFlow_withQCPass_putawayReleasedAndCompleted() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId, "QC Zone", "QZ01");
      Integer locationId = insertLocation(warehouseId, zoneId, "LOC-QC-001");
      Integer vendorId = insertVendor();
      Integer poId = insertPurchaseOrder(warehouseId, vendorId, "PO-QC-001");
      Integer itemId = insertItemWithBarcode("SKU-QC", "QC Item", "UPC-QC-001");
      insertPurchaseOrderLine(poId, itemId, 20);

      /////////////////////////////////////////////////////////////////////////
      // Receive with damaged condition (conditionId > 1 triggers QC)       //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput receiveInput = new RunBackendStepInput();
      receiveInput.setStepName("processReceipt");
      receiveInput.addValue("purchaseOrderId", poId);
      receiveInput.addValue("warehouseId", warehouseId);
      receiveInput.addValue("itemBarcode", "UPC-QC-001");
      receiveInput.addValue("quantity", 20);
      receiveInput.addValue("conditionId", 2);

      RunBackendStepOutput receiveOutput = new RunBackendStepOutput();
      new ReceiveAgainstPOStep().run(receiveInput, receiveOutput);

      /////////////////////////////////////////////////////////////////////////
      // Verify: Two tasks created (QC_INSPECT + PUTAWAY ON_HOLD)           //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput allTasksQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(allTasksQuery.getRecords()).hasSize(2);

      QueryOutput qcTaskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.QC_INSPECT.getPossibleValueId()))));
      assertThat(qcTaskQuery.getRecords()).hasSize(1);

      QueryOutput putawayTaskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PUTAWAY.getPossibleValueId()))));
      assertThat(putawayTaskQuery.getRecords()).hasSize(1);
      assertThat(putawayTaskQuery.getRecords().get(0).getValueInteger("taskStatusId"))
         .isEqualTo(TaskStatus.ON_HOLD.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Complete QC inspection with PASS result                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord qcTask = qcTaskQuery.getRecords().get(0);
      qcTask.setValue("countedQuantity", BigDecimal.ONE);
      qcTask.setValue("completedBy", "qc-user");

      new QcInspectTaskCompletionHandler().handle(qcTask);

      /////////////////////////////////////////////////////////////////////////
      // Verify: PUTAWAY task now PENDING (released from ON_HOLD)           //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput releasedQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PUTAWAY.getPossibleValueId()))));

      assertThat(releasedQuery.getRecords().get(0).getValueInteger("taskStatusId"))
         .isEqualTo(TaskStatus.PENDING.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify: Receipt line QC status is PASSED                           //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME));
      assertThat(lineQuery.getRecords()).hasSize(1);
      assertThat(lineQuery.getRecords().get(0).getValueInteger("qcStatusId"))
         .isEqualTo(QcStatus.PASSED.getPossibleValueId());
   }
}
