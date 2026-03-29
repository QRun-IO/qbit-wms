/*******************************************************************************
 ** Unit tests for AutoAllocateAndReleaseStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AutoAllocateAndReleaseStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that a pending order with sufficient inventory gets fully allocated.
    *******************************************************************************/
   @Test
   void testRun_sufficientInventory_orderAllocated() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Create inventory with available quantity                            //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      /////////////////////////////////////////////////////////////////////////
      // Create a pending order with a line for 10 units                    //
      /////////////////////////////////////////////////////////////////////////
      Integer orderId = insertOrder(warehouseId);
      insertOrderLine(orderId, itemId, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AutoAllocateAndReleaseStep().run(input, output);

      assertThat(output.getValueInteger("ordersProcessed")).isEqualTo(1);
      assertThat(output.getValueString("resultMessage")).contains("allocated");

      /////////////////////////////////////////////////////////////////////////
      // Verify order status changed to ALLOCATED                           //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.ALLOCATED.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test that null warehouse throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_nullWarehouse_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new AutoAllocateAndReleaseStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Warehouse is required");
   }



   /*******************************************************************************
    ** Test with no pending orders processes zero.
    *******************************************************************************/
   @Test
   void testRun_noPendingOrders_zeroProcessed() throws Exception
   {
      Integer warehouseId = insertWarehouse();

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AutoAllocateAndReleaseStep().run(input, output);

      assertThat(output.getValueInteger("ordersProcessed")).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test that insufficient inventory results in partial allocation (order stays
    ** in PENDING status since not all lines are fully allocated).
    *******************************************************************************/
   @Test
   void testRun_insufficientInventory_orderNotFullyAllocated() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Only 5 units available, but order needs 10                         //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("5"));

      Integer orderId = insertOrder(warehouseId);
      insertOrderLine(orderId, itemId, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AutoAllocateAndReleaseStep().run(input, output);

      assertThat(output.getValueInteger("ordersProcessed")).isEqualTo(1);

      /////////////////////////////////////////////////////////////////////////
      // Order should still be PENDING since not fully allocated             //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PENDING.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // But order line should have partial allocation                      //
      /////////////////////////////////////////////////////////////////////////
      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(1)).getRecord();
      BigDecimal allocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));
      assertThat(allocated.compareTo(new BigDecimal("5"))).isEqualTo(0);
   }
}
