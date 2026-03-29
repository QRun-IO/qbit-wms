/*******************************************************************************
 ** Unit tests for LoadCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class LoadCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a LOAD task marks cartons SHIPPED, updates order to
    ** SHIPPED, and updates order line quantity_shipped.
    *******************************************************************************/
   @Test
   void testHandle_normalLoad_updatesCartonAndOrderStatuses() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Set order line as fully packed                                      //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityPacked", 10)));

      /////////////////////////////////////////////////////////////////////////
      // Create a carton in PACKED status                                    //
      /////////////////////////////////////////////////////////////////////////
      Integer cartonId = insertCarton(orderId);
      new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", cartonId)
         .withValue("statusId", CartonStatus.PACKED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Set order status to PACKED                                          //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.PACKED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Execute LOAD completion handler                                     //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("orderId", orderId)
         .withValue("taskTypeId", TaskType.LOAD.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new LoadCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify carton is SHIPPED                                            //
      /////////////////////////////////////////////////////////////////////////
      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      assertThat(carton.getValueInteger("statusId")).isEqualTo(CartonStatus.SHIPPED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify order is SHIPPED                                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.SHIPPED.getPossibleValueId());
      assertThat(order.getValue("shippedDate")).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify order line quantity_shipped updated                          //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      BigDecimal qtyShipped = ValueUtils.getValueAsBigDecimal(line.getValue("quantityShipped"));
      assertThat(qtyShipped.compareTo(new BigDecimal("10"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify SHIP transaction created                                     //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME)
         .withFilter(new QQueryFilter()));
      assertThat(txnQuery.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test that LOAD with no orderId does not throw.
    *******************************************************************************/
   @Test
   void testHandle_noOrderId_doesNotThrow() throws Exception
   {
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", 1)
         .withValue("taskTypeId", TaskType.LOAD.getPossibleValueId());

      // Should not throw -- just log a warning and return
      new LoadCompletionHandler().handle(task);
   }
}
