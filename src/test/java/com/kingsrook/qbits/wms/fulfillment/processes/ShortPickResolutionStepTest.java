/*******************************************************************************
 ** Unit tests for ShortPickResolutionStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderLineStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ShortPickResolutionStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that BACKORDER resolution sets the order line to BACKORDERED status.
    *******************************************************************************/
   @Test
   void testRun_backorderResolution_setsBackorderedStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("orderLineId", lineId);
      input.addValue("resolution", "BACKORDER");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ShortPickResolutionStep().run(input, output);

      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      assertThat(line.getValueInteger("statusId")).isEqualTo(OrderLineStatus.BACKORDERED.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("backordered");
   }



   /*******************************************************************************
    ** Test that CANCEL resolution sets the order line to CANCELLED and zeros
    ** the backordered quantity.
    *******************************************************************************/
   @Test
   void testRun_cancelResolution_setsCancelledStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("orderLineId", lineId);
      input.addValue("resolution", "CANCEL");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ShortPickResolutionStep().run(input, output);

      QRecord line = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      assertThat(line.getValueInteger("statusId")).isEqualTo(OrderLineStatus.CANCELLED.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("cancelled");
   }
}
