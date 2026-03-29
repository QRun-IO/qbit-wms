/*******************************************************************************
 ** Integration tests for returns processes: create RMA, receive return,
 ** inspect, and disposition.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReturnsIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** Test the create-RMA -> receive-return pipeline.
    *******************************************************************************/
   @Test
   void testReturnsPipeline_createAndReceive_endToEnd() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Set order line as shipped so CreateRMA can find shipped lines       //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityShipped", 10)));

      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.SHIPPED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create RMA                                                  //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput createInput = new RunBackendStepInput();
      createInput.addValue("warehouseId", warehouseId);
      createInput.addValue("orderId", orderId);
      createInput.addValue("customerName", "John Doe");
      createInput.addValue("notes", "Customer reported defective");
      RunBackendStepOutput createOutput = new RunBackendStepOutput();

      new CreateRMAStep().run(createInput, createOutput);

      Integer raId = createOutput.getValueInteger("returnAuthorizationId");
      assertThat(raId).isNotNull();
      assertThat(createOutput.getValueString("rmaNumber")).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA created with AWAITING_RECEIPT status                     //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
      assertThat(ra).isNotNull();
      assertThat(ra.getValueInteger("statusId")).isEqualTo(ReturnAuthorizationStatus.AWAITING_RECEIPT.getPossibleValueId());
      assertThat(ra.getValueString("customerName")).isEqualTo("John Doe");

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Receive return                                              //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput receiveInput = new RunBackendStepInput();
      receiveInput.addValue("returnAuthorizationId", raId);
      receiveInput.addValue("receivedBy", "Warehouse Worker");
      receiveInput.addValue("carrierName", "UPS");
      receiveInput.addValue("trackingNumber", "1Z999AA10123456784");
      RunBackendStepOutput receiveOutput = new RunBackendStepOutput();

      new ReceiveReturnStep().run(receiveInput, receiveOutput);

      assertThat(receiveOutput.getValueInteger("returnReceiptId")).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA status updated to RECEIVED                               //
      /////////////////////////////////////////////////////////////////////////
      QRecord updatedRa = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
      assertThat(updatedRa.getValueInteger("statusId")).isEqualTo(ReturnAuthorizationStatus.RECEIVED.getPossibleValueId());
   }
}
