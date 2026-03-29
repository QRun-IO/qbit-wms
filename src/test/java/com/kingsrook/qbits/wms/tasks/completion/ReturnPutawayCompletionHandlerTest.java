/*******************************************************************************
 ** Unit tests for ReturnPutawayCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
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


class ReturnPutawayCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a RETURN_PUTAWAY task creates inventory and a
    ** RETURN transaction.
    *******************************************************************************/
   @Test
   void testHandle_normalReturnPutaway_createsInventory() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("destinationLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("5"))
         .withValue("taskTypeId", TaskType.RETURN_PUTAWAY.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new ReturnPutawayCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify inventory created at destination                             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId))));

      assertThat(invQuery.getRecords()).isNotEmpty();
      BigDecimal qoh = ValueUtils.getValueAsBigDecimal(invQuery.getRecords().get(0).getValue("quantityOnHand"));
      assertThat(qoh.compareTo(new BigDecimal("5"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify RETURN transaction created                                   //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME)
         .withFilter(new QQueryFilter()));
      assertThat(txnQuery.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test that zero quantity does not create inventory.
    *******************************************************************************/
   @Test
   void testHandle_zeroQuantity_skipsProcessing() throws Exception
   {
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", 1)
         .withValue("itemId", 1)
         .withValue("quantityCompleted", BigDecimal.ZERO)
         .withValue("taskTypeId", TaskType.RETURN_PUTAWAY.getPossibleValueId());

      // Should not throw
      new ReturnPutawayCompletionHandler().handle(task);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME)
         .withFilter(new QQueryFilter()));
      assertThat(txnQuery.getRecords()).isEmpty();
   }
}
