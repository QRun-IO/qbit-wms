/*******************************************************************************
 ** Backend step for HoldOrder.  Places an order on hold by updating its
 ** status to ON_HOLD.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class HoldOrderStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(HoldOrderStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer orderId = input.getValueInteger("orderId");
      String reason = input.getValueString("reason");

      if(orderId == null)
      {
         throw new QUserFacingException("Order ID is required.");
      }

      GetOutput orderGet = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId));
      QRecord order = orderGet.getRecord();

      if(order == null)
      {
         throw new QUserFacingException("Order not found.");
      }

      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.ON_HOLD.getPossibleValueId())));

      LOG.info("Order placed on hold", logPair("orderId", orderId), logPair("reason", reason));

      output.addValue("resultMessage", "Order " + orderId + " placed on hold.");
   }
}
