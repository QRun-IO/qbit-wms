/*******************************************************************************
 ** Unit tests for {@link QcInspectTaskCompletionHandler}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class QcInspectTaskCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test PASS result: updates receipt line QC status to PASSED and releases
    ** the associated PUTAWAY task.
    *******************************************************************************/
   @Test
   void testHandle_passResult_releasesHeldPutawayTask() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 10);
      Integer destLoc = insertLocation(warehouseId, null, "QC-DEST-001");

      /////////////////////////////////////////////////////////////////////////
      // Create an ON_HOLD PUTAWAY task linked to the receipt line           //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.ON_HOLD.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("destinationLocationId", destLoc)
         .withValue("receiptLineId", receiptLineId)
      ));

      /////////////////////////////////////////////////////////////////////////
      // Create and handle the QC task with PASS result (1)                  //
      /////////////////////////////////////////////////////////////////////////
      QRecord qcTask = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.QC_INSPECT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("receiptLineId", receiptLineId)
         .withValue("countedQuantity", BigDecimal.ONE)
         .withValue("completedBy", "inspector")
      )).getRecords().get(0);

      new QcInspectTaskCompletionHandler().handle(qcTask);

      ///////////////////////////////////////////////////////////////////
      // Verify receipt line QC status is PASSED                       //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, receiptLineId))));

      assertThat(lineQuery.getRecords().get(0).getValueInteger("qcStatusId"))
         .isEqualTo(QcStatus.PASSED.getPossibleValueId());

      ///////////////////////////////////////////////////////////////////
      // Verify PUTAWAY task was released to PENDING                   //
      ///////////////////////////////////////////////////////////////////
      QueryOutput putawayQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PUTAWAY.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("receiptLineId", QCriteriaOperator.EQUALS, receiptLineId))));

      assertThat(putawayQuery.getRecords()).hasSize(1);
      assertThat(putawayQuery.getRecords().get(0).getValueInteger("taskStatusId"))
         .isEqualTo(TaskStatus.PENDING.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test FAIL result: updates receipt line QC status to FAILED, creates an
    ** inventory hold record and a HOLD transaction.
    *******************************************************************************/
   @Test
   void testHandle_failResult_createsHoldAndTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 10);
      Integer sourceLoc = insertLocation(warehouseId, null, "QC-SRC-001");

      QRecord qcTask = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.QC_INSPECT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("receiptLineId", receiptLineId)
         .withValue("sourceLocationId", sourceLoc)
         .withValue("countedQuantity", new BigDecimal("2"))
         .withValue("completedBy", "inspector")
      )).getRecords().get(0);

      new QcInspectTaskCompletionHandler().handle(qcTask);

      ///////////////////////////////////////////////////////////////////
      // Verify receipt line QC status is FAILED                       //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, receiptLineId))));

      assertThat(lineQuery.getRecords().get(0).getValueInteger("qcStatusId"))
         .isEqualTo(QcStatus.FAILED.getPossibleValueId());

      ///////////////////////////////////////////////////////////////////
      // Verify inventory hold was created                             //
      ///////////////////////////////////////////////////////////////////
      QueryOutput holdQuery = new QueryAction().execute(new QueryInput(WmsInventoryHold.TABLE_NAME));
      assertThat(holdQuery.getRecords()).hasSize(1);
      assertThat(holdQuery.getRecords().get(0).getValueString("status")).isEqualTo("ACTIVE");

      ///////////////////////////////////////////////////////////////////
      // Verify HOLD transaction was created                           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
   }



   /*******************************************************************************
    ** Test null inspection result is handled gracefully (skipped).
    *******************************************************************************/
   @Test
   void testHandle_nullResult_skipsGracefully() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();

      QRecord qcTask = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.QC_INSPECT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
      )).getRecords().get(0);

      new QcInspectTaskCompletionHandler().handle(qcTask);

      ///////////////////////////////////////////////////////////////////
      // No hold or transaction should be created                      //
      ///////////////////////////////////////////////////////////////////
      QueryOutput holdQuery = new QueryAction().execute(new QueryInput(WmsInventoryHold.TABLE_NAME));
      assertThat(holdQuery.getRecords()).isEmpty();

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isEmpty();
   }
}
