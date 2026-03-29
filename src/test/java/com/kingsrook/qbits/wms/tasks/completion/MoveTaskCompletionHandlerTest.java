/*******************************************************************************
 ** Unit tests for {@link MoveTaskCompletionHandler}.
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
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class MoveTaskCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test successful move: deducts from source, adds to destination, creates
    ** transaction record.
    *******************************************************************************/
   @Test
   void testHandle_successfulMove_inventoryUpdated() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-001");
      Integer destLoc = insertLocation(warehouseId, null, "DST-001");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("100"));

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.MOVE.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", sourceLoc)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", new BigDecimal("30"))
         .withValue("completedBy", "testuser")
      )).getRecords().get(0);

      new MoveTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Verify source inventory was deducted                          //
      ///////////////////////////////////////////////////////////////////
      QueryOutput sourceQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, sourceLoc))));

      QRecord sourceInv = sourceQuery.getRecords().get(0);
      assertThat(sourceInv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("70"));

      ///////////////////////////////////////////////////////////////////
      // Verify destination inventory was added (created new record)   //
      ///////////////////////////////////////////////////////////////////
      QueryOutput destQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, destLoc))));

      assertThat(destQuery.getRecords()).hasSize(1);
      QRecord destInv = destQuery.getRecords().get(0);
      assertThat(destInv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("30"));
   }



   /*******************************************************************************
    ** Test that a MOVE transaction record is created.
    *******************************************************************************/
   @Test
   void testHandle_moveTask_createsTransactionRecord() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-002");
      Integer destLoc = insertLocation(warehouseId, null, "DST-002");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("50"));

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.MOVE.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", sourceLoc)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", new BigDecimal("20"))
         .withValue("completedBy", "mover")
      )).getRecords().get(0);

      new MoveTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Verify transaction record was created with correct type       //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);

      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.MOVE.getPossibleValueId());
      assertThat(txn.getValueInteger("fromLocationId")).isEqualTo(sourceLoc);
      assertThat(txn.getValueInteger("toLocationId")).isEqualTo(destLoc);
      assertThat(txn.getValueBigDecimal("quantity")).isEqualByComparingTo(new BigDecimal("20"));
      assertThat(txn.getValueString("performedBy")).isEqualTo("mover");
   }



   /*******************************************************************************
    ** Test that a move with null/zero quantity is skipped gracefully.
    *******************************************************************************/
   @Test
   void testHandle_zeroQuantity_noInventoryChanges() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-003");
      Integer destLoc = insertLocation(warehouseId, null, "DST-003");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("100"));

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.MOVE.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", sourceLoc)
         .withValue("destinationLocationId", destLoc)
         .withValue("quantityCompleted", BigDecimal.ZERO)
      )).getRecords().get(0);

      new MoveTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // No transaction should be created                              //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isEmpty();

      ///////////////////////////////////////////////////////////////////
      // Source inventory unchanged                                    //
      ///////////////////////////////////////////////////////////////////
      QueryOutput sourceQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, sourceLoc))));
      assertThat(sourceQuery.getRecords().get(0).getValueBigDecimal("quantityOnHand"))
         .isEqualByComparingTo(new BigDecimal("100"));
   }
}
