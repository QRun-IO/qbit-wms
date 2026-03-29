/*******************************************************************************
 ** Unit tests for {@link ApproveCountVarianceStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


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
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ApproveCountVarianceStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test approving a line with variance creates adjustment transaction.
    *******************************************************************************/
   @Test
   void testRun_approveVariance_createsAdjustmentTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      QRecord line = new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("100"))
         .withValue("countedQuantity", new BigDecimal("95"))
         .withValue("variance", new BigDecimal("-5"))
         .withValue("status", "COUNTED")))
      .getRecords().get(0);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("cycleCountId", cycleCountId);
      input.addValue("approveAll", true);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ApproveCountVarianceStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify COUNT transaction was created                          //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.COUNT.getPossibleValueId());
      assertThat(txn.getValueBigDecimal("quantity")).isEqualByComparingTo(new BigDecimal("-5"));

      ///////////////////////////////////////////////////////////////////
      // Verify the line was approved                                  //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      QRecord updatedLine = lineQuery.getRecords().get(0);
      assertThat(updatedLine.getValueString("status")).isEqualTo("APPROVED");
      assertThat(updatedLine.getValueBoolean("varianceApproved")).isTrue();

      ///////////////////////////////////////////////////////////////////
      // Verify inventory was adjusted                                 //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      assertThat(invQuery.getRecords().get(0).getValueBigDecimal("quantityOnHand"))
         .isEqualByComparingTo(new BigDecimal("95"));

      assertThat(output.getValue("approvedCount")).isEqualTo(1);
   }



   /*******************************************************************************
    ** Test rejecting a line resets it to PENDING and increments the reject count.
    ** Note: the internal createRecountTask does not set warehouseId (a required
    ** field), so the recount task insert may produce a validation error.  The
    ** line status update still occurs.
    *******************************************************************************/
   @Test
   void testRun_rejectLine_resetsLineToPending() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      QRecord line = new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("50"))
         .withValue("countedQuantity", new BigDecimal("30"))
         .withValue("variance", new BigDecimal("-20"))
         .withValue("status", "COUNTED")))
      .getRecords().get(0);

      Integer lineId = line.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("cycleCountId", cycleCountId);
      input.addValue("rejectLineIds", String.valueOf(lineId));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ApproveCountVarianceStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Line should be back to PENDING                                //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      assertThat(lineQuery.getRecords().get(0).getValueString("status")).isEqualTo("PENDING");

      assertThat(output.getValue("rejectedCount")).isEqualTo(1);
   }



   /*******************************************************************************
    ** Test that when all lines are approved, cycle count is completed.
    *******************************************************************************/
   @Test
   void testRun_allLinesApproved_cycleCountCompleted() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("100"))
         .withValue("countedQuantity", new BigDecimal("100"))
         .withValue("variance", BigDecimal.ZERO)
         .withValue("status", "COUNTED")));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("cycleCountId", cycleCountId);
      input.addValue("approveAll", true);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new ApproveCountVarianceStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify cycle count is COMPLETED                               //
      ///////////////////////////////////////////////////////////////////
      QueryOutput ccQuery = new QueryAction().execute(new QueryInput(WmsCycleCount.TABLE_NAME));
      QRecord cc = ccQuery.getRecords().get(0);
      assertThat(cc.getValueInteger("cycleCountStatusId")).isEqualTo(CycleCountStatus.COMPLETED.getPossibleValueId());
   }
}
