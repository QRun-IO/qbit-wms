/*******************************************************************************
 ** Unit tests for {@link TaskCompletionDispatcher}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class TaskCompletionDispatcherTest extends BaseTest
{

   /*******************************************************************************
    ** Test that dispatching a COUNT task routes to CountTaskCompletionHandler.
    *******************************************************************************/
   @Test
   void testComplete_countTask_dispatchesToCountHandler() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("expectedQuantity", new BigDecimal("10"))
         .withValue("countedQuantity", new BigDecimal("10"))
      )).getRecords().get(0);

      TaskCompletionDispatcher.complete(task);

      //////////////////////////////////////////////////////////////////////////
      // Verify the handler ran: variance should be updated on the task       //
      //////////////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      QRecord updated = taskQuery.getRecords().get(0);
      assertThat(updated.getValueBigDecimal("variance")).isEqualByComparingTo(BigDecimal.ZERO);
   }



   /*******************************************************************************
    ** Test that dispatching a MOVE task routes to MoveTaskCompletionHandler.
    *******************************************************************************/
   @Test
   void testComplete_moveTask_dispatchesToMoveHandler() throws QException
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
         .withValue("quantityCompleted", new BigDecimal("25"))
         .withValue("completedBy", "testuser")
      )).getRecords().get(0);

      TaskCompletionDispatcher.complete(task);

      //////////////////////////////////////////////////////////////////////////
      // Verify transaction was created                                       //
      //////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
   }



   /*******************************************************************************
    ** Test that dispatching with an unknown task type throws QException.
    *******************************************************************************/
   @Test
   void testComplete_unknownTaskType_throwsException()
   {
      QRecord task = new QRecord()
         .withValue("id", 999)
         .withValue("taskTypeId", 999);

      assertThatThrownBy(() -> TaskCompletionDispatcher.complete(task))
         .isInstanceOf(QException.class)
         .hasMessageContaining("No completion handler registered for task type id: 999");
   }
}
