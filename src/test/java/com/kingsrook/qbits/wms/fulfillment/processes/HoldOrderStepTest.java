/*******************************************************************************
 ** Unit tests for HoldOrderStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class HoldOrderStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that holding an order sets its status to ON_HOLD.
    *******************************************************************************/
   @Test
   void testRun_holdOrder_setsOnHoldStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("orderId", orderId);
      input.addValue("reason", "Customer requested hold");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new HoldOrderStep().run(input, output);

      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.ON_HOLD.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("on hold");
   }
}
