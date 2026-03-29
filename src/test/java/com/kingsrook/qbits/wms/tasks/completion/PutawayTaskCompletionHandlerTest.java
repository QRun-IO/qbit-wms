/*******************************************************************************
 ** Unit tests for {@link PutawayTaskCompletionHandler}.
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
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsReceipt;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class PutawayTaskCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test successful putaway creates inventory at destination and logs a
    ** PUTAWAY transaction.
    *******************************************************************************/
   @Test
   void testHandle_successfulPutaway_createsInventoryAndTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer destLoc = insertLocation(warehouseId, null, "PUT-001");

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", new BigDecimal("25"))
         .withValue("completedBy", "testuser")
      )).getRecords().get(0);

      new PutawayTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Verify inventory was created at destination                   //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, destLoc))));

      assertThat(invQuery.getRecords()).hasSize(1);
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("25"));

      ///////////////////////////////////////////////////////////////////
      // Verify transaction record was created                         //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.PUTAWAY.getPossibleValueId());
      assertThat(txn.getValueInteger("toLocationId")).isEqualTo(destLoc);
   }



   /*******************************************************************************
    ** Test that zero quantity skips inventory changes gracefully.
    *******************************************************************************/
   @Test
   void testHandle_zeroQuantity_noInventoryChanges() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer destLoc = insertLocation(warehouseId, null, "PUT-002");

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", BigDecimal.ZERO)
      )).getRecords().get(0);

      new PutawayTaskCompletionHandler().handle(task);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isEmpty();
   }



   /*******************************************************************************
    ** Test that putaway with receipt line updates the receipt line status to
    ** COMPLETED.
    *******************************************************************************/
   @Test
   void testHandle_withReceiptLine_updatesReceiptLineStatus() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer destLoc = insertLocation(warehouseId, null, "PUT-003");
      Integer receiptId = insertReceipt(warehouseId);
      Integer receiptLineId = insertReceiptLine(receiptId, itemId, 25);

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", new BigDecimal("25"))
         .withValue("completedBy", "testuser")
         .withValue("receiptLineId", receiptLineId)
         .withValue("receiptId", receiptId)
      )).getRecords().get(0);

      new PutawayTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Verify receipt line was marked COMPLETED                      //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, receiptLineId))));

      assertThat(lineQuery.getRecords()).hasSize(1);
      assertThat(lineQuery.getRecords().get(0).getValueInteger("statusId"))
         .isEqualTo(ReceiptStatus.COMPLETED.getPossibleValueId());
   }
}
