/*******************************************************************************
 ** Unit tests for PickCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class PickCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a pick task deducts inventory and updates order line.
    *******************************************************************************/
   @Test
   void testHandle_normalPick_deductsInventoryAndUpdatesPicked() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Set up inventory with allocation                                    //
      /////////////////////////////////////////////////////////////////////////
      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("quantityAllocated", new BigDecimal("10"))
         .withValue("quantityAvailable", new BigDecimal("90"))));

      /////////////////////////////////////////////////////////////////////////
      // Set up order line with allocation                                   //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityAllocated", 10)));

      /////////////////////////////////////////////////////////////////////////
      // Create and dispatch a PICK task                                     //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("10"))
         .withValue("quantityRequested", new BigDecimal("10"))
         .withValue("orderId", orderId)
         .withValue("orderLineId", lineId)
         .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new PickCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify inventory deducted                                           //
      /////////////////////////////////////////////////////////////////////////
      QRecord inv = new GetAction().execute(new GetInput(WmsInventory.TABLE_NAME).withPrimaryKey(invId)).getRecord();
      BigDecimal qoh = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityOnHand"));
      assertThat(qoh.compareTo(new BigDecimal("90"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify order line picked quantity updated                           //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      BigDecimal qtyPicked = ValueUtils.getValueAsBigDecimal(line.getValue("quantityPicked"));
      assertThat(qtyPicked.compareTo(new BigDecimal("10"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify transaction created                                          //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME)
         .withFilter(new QQueryFilter()));
      assertThat(txnQuery.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test that a short pick updates backorder quantities.
    *******************************************************************************/
   @Test
   void testHandle_shortPick_incrementsBackordered() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("quantityAllocated", new BigDecimal("10"))
         .withValue("quantityAvailable", new BigDecimal("90"))));

      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityAllocated", 10)));

      /////////////////////////////////////////////////////////////////////////
      // Short pick: requested 10, completed 7                               //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("7"))
         .withValue("quantityRequested", new BigDecimal("10"))
         .withValue("orderId", orderId)
         .withValue("orderLineId", lineId)
         .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new PickCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify backordered quantity updated                                 //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      BigDecimal backordered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityBackordered"));
      assertThat(backordered.compareTo(new BigDecimal("3"))).isEqualTo(0);
   }
}
