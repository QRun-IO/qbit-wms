/*******************************************************************************
 ** Completion handler for LOAD tasks.  Handles the final step in the outbound
 ** fulfillment pipeline: updates carton statuses to SHIPPED, sets
 ** order_line.quantity_shipped, inserts a SHIP inventory transaction (perpetual
 ** inventory), updates order.status to SHIPPED, and optionally creates a
 ** billing activity for 3PL clients.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class LoadCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(LoadCompletionHandler.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void handle(QRecord task) throws QException
   {
      Integer taskId = task.getValueInteger("id");
      Integer warehouseId = task.getValueInteger("warehouseId");
      Integer clientId = task.getValueInteger("clientId");
      Integer orderId = task.getValueInteger("orderId");
      String completedBy = task.getValueString("completedBy");

      LOG.info("Handling LOAD task completion",
         logPair("taskId", taskId),
         logPair("orderId", orderId));

      if(orderId == null)
      {
         LOG.warn("LOAD task has no orderId, skipping", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Update all cartons for this order to SHIPPED                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput cartonQuery = new QueryAction().execute(new QueryInput(WmsCarton.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      for(QRecord carton : cartonQuery.getRecords())
      {
         new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", carton.getValueInteger("id"))
            .withValue("statusId", CartonStatus.SHIPPED.getPossibleValueId())));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Update order_line.quantity_shipped and insert transactions   //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      BigDecimal totalShipped = BigDecimal.ZERO;

      for(QRecord line : lineQuery.getRecords())
      {
         Integer lineId = line.getValueInteger("id");
         Integer itemId = line.getValueInteger("itemId");
         BigDecimal quantityPacked = ValueUtils.getValueAsBigDecimal(line.getValue("quantityPacked"));
         if(quantityPacked == null || quantityPacked.compareTo(BigDecimal.ZERO) <= 0)
         {
            continue;
         }

         //////////////////////////////////////////////////////////////////
         // Insert SHIP transaction (perpetual inventory -- MUST be first)//
         //////////////////////////////////////////////////////////////////
         createInventoryTransaction(
            warehouseId, clientId, itemId,
            TransactionType.SHIP,
            null, null,
            quantityPacked,
            null, null,
            taskId,
            null,
            completedBy,
            "Load task " + taskId + " shipped for order " + orderId
         );

         //////////////////////////////////////////////////////////////////
         // Update order_line.quantity_shipped                           //
         //////////////////////////////////////////////////////////////////
         BigDecimal currentShipped = ValueUtils.getValueAsBigDecimal(line.getValue("quantityShipped"));
         if(currentShipped == null)
         {
            currentShipped = BigDecimal.ZERO;
         }

         new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", lineId)
            .withValue("quantityShipped", currentShipped.add(quantityPacked))));

         totalShipped = totalShipped.add(quantityPacked);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Update order status to SHIPPED                              //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.SHIPPED.getPossibleValueId())
         .withValue("shippedDate", Instant.now())));

      /////////////////////////////////////////////////////////////////////////
      // Step 4: If clientId present, create billing activity                //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId, totalShipped);
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for SHIP_PER_ORDER.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.SHIP_PER_ORDER.getPossibleValueId())
            .withValue("activityDate", Instant.now())
            .withValue("quantity", quantity)
            .withValue("referenceType", "TASK")
            .withValue("referenceId", taskId)
            .withValue("taskId", taskId)
            .withValue("isBilled", false)));
      }
      catch(Exception e)
      {
         LOG.error("Failed to create billing activity for LOAD task", e,
            logPair("taskId", taskId));
      }
   }
}
