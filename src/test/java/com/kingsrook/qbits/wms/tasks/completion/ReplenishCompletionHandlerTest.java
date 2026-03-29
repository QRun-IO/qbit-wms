/*******************************************************************************
 ** Unit tests for ReplenishCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReplenishCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a replenish task moves inventory from source to dest.
    *******************************************************************************/
   @Test
   void testHandle_movesInventory() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLocationId = insertLocation(warehouseId, null, "BULK-001");
      Integer destLocationId = insertLocation(warehouseId, null, "PICK-001");

      /////////////////////////////////////////////////////////////////////////
      // Set up inventory at source location                                 //
      /////////////////////////////////////////////////////////////////////////
      Integer invId = insertInventory(warehouseId, itemId, sourceLocationId, new BigDecimal("100"));

      /////////////////////////////////////////////////////////////////////////
      // Create and handle a REPLENISH task                                  //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", sourceLocationId)
         .withValue("destinationLocationId", destLocationId)
         .withValue("quantityCompleted", new BigDecimal("30"))
         .withValue("taskTypeId", TaskType.REPLENISH.getPossibleValueId())
         .withValue("completedBy", "TestWorker");

      new ReplenishCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify source deducted                                              //
      /////////////////////////////////////////////////////////////////////////
      QRecord sourceInv = new GetAction().execute(new GetInput(WmsInventory.TABLE_NAME).withPrimaryKey(invId)).getRecord();
      BigDecimal sourceQoh = ValueUtils.getValueAsBigDecimal(sourceInv.getValue("quantityOnHand"));
      assertThat(sourceQoh.compareTo(new BigDecimal("70"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify destination received (new inventory record created)          //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput destInvQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria("locationId",
               com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.EQUALS, destLocationId))));
      assertThat(destInvQuery.getRecords()).hasSize(1);
      BigDecimal destQoh = ValueUtils.getValueAsBigDecimal(destInvQuery.getRecords().get(0).getValue("quantityOnHand"));
      assertThat(destQoh.compareTo(new BigDecimal("30"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify transaction created                                          //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME)
         .withFilter(new QQueryFilter()));
      assertThat(txnQuery.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test that zero quantity is a no-op.
    *******************************************************************************/
   @Test
   void testHandle_zeroQuantity_noOp() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLocationId = insertLocation(warehouseId, null, "BULK-002");
      Integer destLocationId = insertLocation(warehouseId, null, "PICK-002");
      Integer invId = insertInventory(warehouseId, itemId, sourceLocationId, new BigDecimal("50"));

      QRecord task = new QRecord()
         .withValue("id", 2)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", sourceLocationId)
         .withValue("destinationLocationId", destLocationId)
         .withValue("quantityCompleted", BigDecimal.ZERO)
         .withValue("taskTypeId", TaskType.REPLENISH.getPossibleValueId());

      new ReplenishCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify no inventory change                                          //
      /////////////////////////////////////////////////////////////////////////
      QRecord inv = new GetAction().execute(new GetInput(WmsInventory.TABLE_NAME).withPrimaryKey(invId)).getRecord();
      BigDecimal qoh = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityOnHand"));
      assertThat(qoh.compareTo(new BigDecimal("50"))).isEqualTo(0);
   }
}
