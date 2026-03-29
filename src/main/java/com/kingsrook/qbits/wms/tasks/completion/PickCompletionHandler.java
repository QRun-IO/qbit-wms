/*******************************************************************************
 ** Completion handler for PICK tasks.  Inserts a PICK transaction, deducts
 ** from inventory quantity_on_hand at the source location, decrements
 ** quantity_allocated, updates order_line.quantity_picked, and when all picks
 ** for an order complete, advances the order to PICKED and creates a PACK task.
 ** Optionally creates a billing activity for 3PL clients.
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
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PickCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(PickCompletionHandler.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void handle(QRecord task) throws QException
   {
      Integer taskId = task.getValueInteger("id");
      Integer warehouseId = task.getValueInteger("warehouseId");
      Integer clientId = task.getValueInteger("clientId");
      Integer itemId = task.getValueInteger("itemId");
      Integer sourceLocationId = task.getValueInteger("sourceLocationId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      BigDecimal quantityRequested = task.getValueBigDecimal("quantityRequested");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");
      Integer orderId = task.getValueInteger("orderId");
      Integer orderLineId = task.getValueInteger("orderLineId");

      LOG.info("Handling PICK task completion",
         logPair("taskId", taskId),
         logPair("itemId", itemId),
         logPair("sourceLocationId", sourceLocationId),
         logPair("quantityCompleted", quantityCompleted));

      if(quantityCompleted == null || quantityCompleted.compareTo(BigDecimal.ZERO) <= 0)
      {
         LOG.warn("PICK task has no quantity completed, skipping inventory changes", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Insert PICK transaction (MUST come first)                   //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, itemId,
         TransactionType.PICK,
         sourceLocationId, null,
         quantityCompleted,
         lotNumber, serialNumber,
         taskId,
         null,
         completedBy,
         "Pick task " + taskId + " completed"
      );

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Deduct from inventory.quantity_on_hand at source            //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, itemId, sourceLocationId,
         quantityCompleted.negate(), lotNumber, serialNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Decrement inventory.quantity_allocated                      //
      /////////////////////////////////////////////////////////////////////////
      decrementAllocated(warehouseId, itemId, sourceLocationId, quantityCompleted, lotNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Update order_line.quantity_picked                           //
      /////////////////////////////////////////////////////////////////////////
      if(orderLineId != null)
      {
         updateOrderLinePicked(orderLineId, quantityCompleted);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 5: Handle SHORT pick                                          //
      /////////////////////////////////////////////////////////////////////////
      boolean isShort = false;
      if(quantityRequested != null && quantityCompleted.compareTo(quantityRequested) < 0)
      {
         isShort = true;
         BigDecimal shortQty = quantityRequested.subtract(quantityCompleted);
         handleShortPick(orderLineId, shortQty);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 6: If all picks for order complete, advance to PICKED         //
      /////////////////////////////////////////////////////////////////////////
      if(orderId != null)
      {
         checkOrderPickComplete(orderId, warehouseId, clientId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 7: If clientId, create billing activity                        //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId, quantityCompleted);
      }
   }



   /*******************************************************************************
    ** Decrement quantity_allocated on the inventory record at the source location.
    *******************************************************************************/
   private void decrementAllocated(Integer warehouseId, Integer itemId, Integer locationId,
      BigDecimal quantity, String lotNumber) throws QException
   {
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
         .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId))
         .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId));

      if(lotNumber != null)
      {
         filter.withCriteria(new QFilterCriteria("lotNumber", QCriteriaOperator.EQUALS, lotNumber));
      }

      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME).withFilter(filter));
      List<QRecord> records = queryOutput.getRecords();

      if(!records.isEmpty())
      {
         QRecord inv = records.get(0);
         BigDecimal currentAllocated = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAllocated"));
         if(currentAllocated == null)
         {
            currentAllocated = BigDecimal.ZERO;
         }

         BigDecimal newAllocated = currentAllocated.subtract(quantity);
         if(newAllocated.compareTo(BigDecimal.ZERO) < 0)
         {
            newAllocated = BigDecimal.ZERO;
         }

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", inv.getValueInteger("id"))
            .withValue("quantityAllocated", newAllocated)));
      }
   }



   /*******************************************************************************
    ** Increment order_line.quantity_picked by the completed quantity.
    *******************************************************************************/
   private void updateOrderLinePicked(Integer orderLineId, BigDecimal quantityCompleted) throws QException
   {
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, orderLineId))));

      if(!lineQuery.getRecords().isEmpty())
      {
         QRecord line = lineQuery.getRecords().get(0);
         BigDecimal currentPicked = ValueUtils.getValueAsBigDecimal(line.getValue("quantityPicked"));
         if(currentPicked == null)
         {
            currentPicked = BigDecimal.ZERO;
         }

         new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderLineId)
            .withValue("quantityPicked", currentPicked.add(quantityCompleted))));
      }
   }



   /*******************************************************************************
    ** Handle a short pick: update order_line.quantity_allocated and
    ** quantity_backordered for the shorted quantity.
    *******************************************************************************/
   private void handleShortPick(Integer orderLineId, BigDecimal shortQty) throws QException
   {
      if(orderLineId == null)
      {
         return;
      }

      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, orderLineId))));

      if(!lineQuery.getRecords().isEmpty())
      {
         QRecord line = lineQuery.getRecords().get(0);
         BigDecimal currentAllocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));
         BigDecimal currentBackordered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityBackordered"));
         if(currentAllocated == null) { currentAllocated = BigDecimal.ZERO; }
         if(currentBackordered == null) { currentBackordered = BigDecimal.ZERO; }

         new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderLineId)
            .withValue("quantityAllocated", currentAllocated.subtract(shortQty))
            .withValue("quantityBackordered", currentBackordered.add(shortQty))));
      }
   }



   /*******************************************************************************
    ** Check if all pick tasks for an order are complete.  If so, update the
    ** order to PICKED and create a PACK task.
    *******************************************************************************/
   private void checkOrderPickComplete(Integer orderId, Integer warehouseId, Integer clientId) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Query all PICK tasks for this order                                 //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PICK.getPossibleValueId()))));

      boolean allComplete = true;
      for(QRecord t : taskQuery.getRecords())
      {
         Integer statusId = t.getValueInteger("taskStatusId");
         if(!Objects.equals(statusId, TaskStatus.COMPLETED.getPossibleValueId())
            && !Objects.equals(statusId, TaskStatus.SHORT.getPossibleValueId()))
         {
            allComplete = false;
            break;
         }
      }

      if(allComplete && !taskQuery.getRecords().isEmpty())
      {
         LOG.info("All picks complete for order, advancing to PICKED", logPair("orderId", orderId));

         new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderId)
            .withValue("statusId", OrderStatus.PICKED.getPossibleValueId())
            .withValue("pickedDate", Instant.now())));

         /////////////////////////////////////////////////////////////////////
         // Create a PACK task for this order                               //
         /////////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.PACK.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("orderId", orderId)
            .withValue("referenceType", "ORDER")
            .withValue("referenceId", orderId)
            .withValue("notes", "Pack task for order " + orderId)));
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for PICK_PER_UNIT.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.PICK_PER_UNIT.getPossibleValueId())
            .withValue("activityDate", Instant.now())
            .withValue("quantity", quantity)
            .withValue("referenceType", "TASK")
            .withValue("referenceId", taskId)
            .withValue("taskId", taskId)
            .withValue("isBilled", false)));
      }
      catch(Exception e)
      {
         LOG.warn("Could not create billing activity (billing tables may not be available yet)",
            logPair("taskId", taskId), logPair("error", e.getMessage()));
      }
   }
}
