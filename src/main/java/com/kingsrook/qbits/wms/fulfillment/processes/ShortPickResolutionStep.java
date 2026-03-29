/*******************************************************************************
 ** Backend step for ShortPickResolution.  Allows the user to choose how to
 ** resolve a short pick: backorder the remaining quantity, cancel the line,
 ** or reallocate from another location.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
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
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.OrderLineStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ShortPickResolutionStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ShortPickResolutionStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer orderLineId = input.getValueInteger("orderLineId");
      String resolution = input.getValueString("resolution");

      if(orderLineId == null)
      {
         throw new QUserFacingException("Order Line ID is required.");
      }
      if(resolution == null || resolution.isBlank())
      {
         throw new QUserFacingException("Resolution type is required.");
      }

      GetOutput lineGet = new GetAction().execute(new GetInput(WmsOrderLine.TABLE_NAME).withPrimaryKey(orderLineId));
      QRecord line = lineGet.getRecord();

      if(line == null)
      {
         throw new QUserFacingException("Order line not found.");
      }

      BigDecimal backordered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityBackordered"));
      if(backordered == null)
      {
         backordered = BigDecimal.ZERO;
      }

      if("BACKORDER".equalsIgnoreCase(resolution))
      {
         new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderLineId)
            .withValue("statusId", OrderLineStatus.BACKORDERED.getPossibleValueId())));

         output.addValue("resultMessage", "Order line " + orderLineId + " set to backordered.");
      }
      else if("CANCEL".equalsIgnoreCase(resolution))
      {
         new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderLineId)
            .withValue("statusId", OrderLineStatus.CANCELLED.getPossibleValueId())
            .withValue("quantityBackordered", 0)));

         output.addValue("resultMessage", "Order line " + orderLineId + " cancelled.");
      }
      else
      {
         output.addValue("resultMessage", "Unknown resolution type: " + resolution);
      }

      LOG.info("Short pick resolved", logPair("orderLineId", orderLineId), logPair("resolution", resolution));
   }
}
