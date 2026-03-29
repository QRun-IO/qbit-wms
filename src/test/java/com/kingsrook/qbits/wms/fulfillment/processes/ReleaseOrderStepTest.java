/*******************************************************************************
 ** Unit tests for ReleaseOrderStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ReleaseOrderStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that releasing a held order sets it back to PENDING.
    *******************************************************************************/
   @Test
   void testRun_releaseHeldOrder_setsPendingStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Put order on hold first                                             //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.ON_HOLD.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("orderId", orderId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReleaseOrderStep().run(input, output);

      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PENDING.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("released");
   }



   /*******************************************************************************
    ** Test that releasing an order that is not on hold throws an exception.
    *******************************************************************************/
   @Test
   void testRun_releaseNonHeldOrder_throwsException() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("orderId", orderId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      assertThatThrownBy(() -> new ReleaseOrderStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("On Hold");
   }
}
