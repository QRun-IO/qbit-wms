/*******************************************************************************
 ** Backend step for CreateRMA.  Creates a return authorization with lines
 ** based on the original order's shipped lines.  Sets status to
 ** AWAITING_RECEIPT and generates an RMA number.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorizationLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CreateRMAStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CreateRMAStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer orderId = input.getValueInteger("orderId");
      String customerName = input.getValueString("customerName");
      Integer reasonCodeId = input.getValueInteger("reasonCodeId");
      String notes = input.getValueString("notes");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      if(orderId == null)
      {
         throw new QUserFacingException("Original Order ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Validate the original order exists                                  //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      if(order == null)
      {
         throw new QUserFacingException("Order not found: " + orderId);
      }

      Integer clientId = order.getValueInteger("clientId");

      /////////////////////////////////////////////////////////////////////////
      // Generate RMA number                                                 //
      /////////////////////////////////////////////////////////////////////////
      String rmaNumber = "RMA-" + System.nanoTime();

      LOG.info("Creating RMA", logPair("orderId", orderId), logPair("rmaNumber", rmaNumber));

      /////////////////////////////////////////////////////////////////////////
      // Create return authorization                                         //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new InsertAction().execute(new InsertInput(WmsReturnAuthorization.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("rmaNumber", rmaNumber)
         .withValue("originalOrderId", orderId)
         .withValue("customerName", customerName)
         .withValue("reasonCodeId", reasonCodeId)
         .withValue("statusId", ReturnAuthorizationStatus.AWAITING_RECEIPT.getPossibleValueId())
         .withValue("authorizedDate", Instant.now())
         .withValue("notes", notes)
      )).getRecords().get(0);

      Integer raId = ra.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Query order lines and create return authorization lines             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      int linesCreated = 0;
      for(QRecord line : linesQuery.getRecords())
      {
         Integer quantityShipped = line.getValueInteger("quantityShipped");
         if(quantityShipped != null && quantityShipped > 0)
         {
            new InsertAction().execute(new InsertInput(WmsReturnAuthorizationLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("returnAuthorizationId", raId)
               .withValue("itemId", line.getValueInteger("itemId"))
               .withValue("quantityAuthorized", quantityShipped)
               .withValue("quantityReceived", 0)));
            linesCreated++;
         }
      }

      output.addValue("resultMessage", "RMA created with " + linesCreated + " lines. RMA: " + rmaNumber);
      output.addValue("returnAuthorizationId", raId);
      output.addValue("rmaNumber", rmaNumber);
   }
}
