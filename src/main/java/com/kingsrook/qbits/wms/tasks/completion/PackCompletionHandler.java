/*******************************************************************************
 ** Completion handler for PACK tasks.  Updates carton status to PACKED,
 ** increments order_line.quantity_packed, inserts a PACK transaction, checks
 ** if all cartons for the order are packed, and if so advances the order to
 ** PACKED and creates a LOAD task.  Optionally creates a billing activity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
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
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCartonLine;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PackCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(PackCompletionHandler.class);



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
      Integer cartonId = task.getValueInteger("cartonId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      String completedBy = task.getValueString("completedBy");

      LOG.info("Handling PACK task completion",
         logPair("taskId", taskId),
         logPair("orderId", orderId),
         logPair("cartonId", cartonId));

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Update carton status to PACKED                              //
      /////////////////////////////////////////////////////////////////////////
      if(cartonId != null)
      {
         new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", cartonId)
            .withValue("statusId", CartonStatus.PACKED.getPossibleValueId())));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Update order_line.quantity_packed from carton lines         //
      /////////////////////////////////////////////////////////////////////////
      if(cartonId != null)
      {
         updateOrderLinePackedFromCarton(cartonId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Insert PACK transaction                                     //
      /////////////////////////////////////////////////////////////////////////
      if(quantityCompleted != null && quantityCompleted.compareTo(BigDecimal.ZERO) > 0)
      {
         Integer itemId = task.getValueInteger("itemId");
         createInventoryTransaction(
            warehouseId, clientId, itemId,
            TransactionType.PACK,
            null, null,
            quantityCompleted,
            null, null,
            taskId,
            null,
            completedBy,
            "Pack task " + taskId + " completed"
         );
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 4: If all cartons for order PACKED, advance order to PACKED    //
      /////////////////////////////////////////////////////////////////////////
      if(orderId != null)
      {
         checkOrderPackComplete(orderId, warehouseId, clientId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 5: If clientId, create billing activity                        //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId,
            quantityCompleted != null ? quantityCompleted : BigDecimal.ONE);
      }
   }



   /*******************************************************************************
    ** Update order_line.quantity_packed based on carton line quantities.
    *******************************************************************************/
   private void updateOrderLinePackedFromCarton(Integer cartonId) throws QException
   {
      QueryOutput cartonLineQuery = new QueryAction().execute(new QueryInput(WmsCartonLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("cartonId", QCriteriaOperator.EQUALS, cartonId))));

      for(QRecord cartonLine : cartonLineQuery.getRecords())
      {
         Integer orderLineId = cartonLine.getValueInteger("orderLineId");
         Integer qty = cartonLine.getValueInteger("quantity");

         if(orderLineId != null && qty != null)
         {
            QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, orderLineId))));

            if(!lineQuery.getRecords().isEmpty())
            {
               QRecord line = lineQuery.getRecords().get(0);
               BigDecimal currentPacked = ValueUtils.getValueAsBigDecimal(line.getValue("quantityPacked"));
               if(currentPacked == null)
               {
                  currentPacked = BigDecimal.ZERO;
               }

               new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", orderLineId)
                  .withValue("quantityPacked", currentPacked.add(new BigDecimal(qty)))));
            }
         }
      }
   }



   /*******************************************************************************
    ** Check if all cartons for an order are PACKED.  If so, advance the order
    ** to PACKED status and create a LOAD task.
    *******************************************************************************/
   private void checkOrderPackComplete(Integer orderId, Integer warehouseId, Integer clientId) throws QException
   {
      QueryOutput cartonQuery = new QueryAction().execute(new QueryInput(WmsCarton.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      List<QRecord> cartons = cartonQuery.getRecords();
      if(cartons.isEmpty())
      {
         return;
      }

      boolean allPacked = true;
      for(QRecord carton : cartons)
      {
         Integer statusId = carton.getValueInteger("statusId");
         if(!Objects.equals(statusId, CartonStatus.PACKED.getPossibleValueId()))
         {
            allPacked = false;
            break;
         }
      }

      if(allPacked)
      {
         LOG.info("All cartons packed for order, advancing to PACKED", logPair("orderId", orderId));

         new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderId)
            .withValue("statusId", OrderStatus.PACKED.getPossibleValueId())
            .withValue("packedDate", Instant.now())));

         /////////////////////////////////////////////////////////////////////
         // Create a LOAD task for this order                               //
         /////////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.LOAD.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("orderId", orderId)
            .withValue("referenceType", "ORDER")
            .withValue("referenceId", orderId)
            .withValue("notes", "Load task for order " + orderId)));
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for PACK_PER_ORDER.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.PACK_PER_ORDER.getPossibleValueId())
            .withValue("activityDate", Instant.now())
            .withValue("quantity", quantity)
            .withValue("referenceType", "TASK")
            .withValue("referenceId", taskId)
            .withValue("taskId", taskId)
            .withValue("isBilled", false)));
      }
      catch(Exception e)
      {
         LOG.error("Failed to create billing activity for PACK task", e,
            logPair("taskId", taskId));
      }
   }
}
