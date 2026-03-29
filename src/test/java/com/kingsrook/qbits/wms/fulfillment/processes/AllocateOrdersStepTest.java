/*******************************************************************************
 ** Unit tests for AllocateOrdersStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class AllocateOrdersStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that allocating an order with sufficient inventory allocates the line
    ** and advances the order to ALLOCATED status.
    *******************************************************************************/
   @Test
   void testRun_sufficientInventory_orderAllocated() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Set up available inventory                                          //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      /////////////////////////////////////////////////////////////////////////
      // Run allocation                                                      //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new AllocateOrdersStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify order status is ALLOCATED                                    //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.ALLOCATED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify order line allocation                                        //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      BigDecimal allocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));
      assertThat(allocated.compareTo(new BigDecimal("10"))).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify result message                                               //
      /////////////////////////////////////////////////////////////////////////
      assertThat(output.getValueString("resultMessage")).contains("1 orders fully allocated");
   }



   /*******************************************************************************
    ** Test that allocating with no inventory does not advance the order.
    *******************************************************************************/
   @Test
   void testRun_noInventory_orderStaysPending() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      insertOrderLine(orderId, itemId, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new AllocateOrdersStep().run(input, output);

      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PENDING.getPossibleValueId());
   }
}
