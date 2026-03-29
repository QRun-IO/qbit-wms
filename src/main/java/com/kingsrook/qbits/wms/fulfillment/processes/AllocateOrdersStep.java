/*******************************************************************************
 ** Backend step for AllocateOrders.  Queries PENDING orders matching the
 ** user-supplied criteria, then for each order line queries available inventory
 ** using FEFO (First Expired, First Out) ordering.  Increments
 ** wms_inventory.quantity_allocated, updates order_line.quantity_allocated,
 ** and sets order.status to ALLOCATED.  All inventory changes go through
 ** transaction records first (perpetual inventory).
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class AllocateOrdersStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(AllocateOrdersStep.class);

   public static final String ORDER_TABLE_NAME      = "wmsOrder";
   public static final String ORDER_LINE_TABLE_NAME  = "wmsOrderLine";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer minPriority = input.getValueInteger("minPriority");
      LocalDate shipByDate = input.getValueLocalDate("shipByDate");
      Integer clientId = input.getValueInteger("clientId");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Query PENDING orders matching criteria                              //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter orderFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, OrderStatus.PENDING.getPossibleValueId()))
         .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId));

      if(minPriority != null)
      {
         orderFilter.withCriteria(new QFilterCriteria("priority", QCriteriaOperator.LESS_THAN_OR_EQUALS, minPriority));
      }
      if(shipByDate != null)
      {
         orderFilter.withCriteria(new QFilterCriteria("shipByDate", QCriteriaOperator.LESS_THAN_OR_EQUALS, shipByDate));
      }
      if(clientId != null)
      {
         orderFilter.withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId));
      }

      orderFilter.withOrderBy(new QFilterOrderBy("priority", true));
      orderFilter.withOrderBy(new QFilterOrderBy("shipByDate", true));

      QueryOutput orderQuery = new QueryAction().execute(new QueryInput(ORDER_TABLE_NAME).withFilter(orderFilter));
      List<QRecord> orders = orderQuery.getRecords();

      int ordersAllocated = 0;

      for(QRecord order : orders)
      {
         Integer orderId = order.getValueInteger("id");
         Integer orderWarehouseId = order.getValueInteger("warehouseId");
         Integer orderClientId = order.getValueInteger("clientId");

         /////////////////////////////////////////////////////////////////////
         // Query order lines for this order                                //
         /////////////////////////////////////////////////////////////////////
         QueryOutput linesQuery = new QueryAction().execute(new QueryInput(ORDER_LINE_TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

         boolean allLinesAllocated = true;

         for(QRecord line : linesQuery.getRecords())
         {
            Integer lineId = line.getValueInteger("id");
            Integer itemId = line.getValueInteger("itemId");
            BigDecimal quantityOrdered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityOrdered"));
            BigDecimal alreadyAllocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));

            if(quantityOrdered == null)
            {
               quantityOrdered = BigDecimal.ZERO;
            }
            if(alreadyAllocated == null)
            {
               alreadyAllocated = BigDecimal.ZERO;
            }

            BigDecimal remainingToAllocate = quantityOrdered.subtract(alreadyAllocated);
            if(remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0)
            {
               continue;
            }

            //////////////////////////////////////////////////////////////////
            // Query available inventory using FEFO ordering                //
            //////////////////////////////////////////////////////////////////
            QQueryFilter inventoryFilter = new QQueryFilter()
               .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
               .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, orderWarehouseId))
               .withCriteria(new QFilterCriteria("quantityAvailable", QCriteriaOperator.GREATER_THAN, 0));

            inventoryFilter.withOrderBy(new QFilterOrderBy("expirationDate", true));
            inventoryFilter.withOrderBy(new QFilterOrderBy("receivedDate", true));

            QueryOutput inventoryQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
               .withFilter(inventoryFilter));

            BigDecimal totalAllocatedThisLine = BigDecimal.ZERO;

            for(QRecord inv : inventoryQuery.getRecords())
            {
               if(remainingToAllocate.compareTo(BigDecimal.ZERO) <= 0)
               {
                  break;
               }

               BigDecimal available = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAvailable"));
               if(available == null || available.compareTo(BigDecimal.ZERO) <= 0)
               {
                  continue;
               }

               BigDecimal allocateQty = available.min(remainingToAllocate);

               ///////////////////////////////////////////////////////////////
               // Create ALLOCATE transaction (perpetual inventory)        //
               ///////////////////////////////////////////////////////////////
               new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
                  .withValue("warehouseId", orderWarehouseId)
                  .withValue("clientId", orderClientId)
                  .withValue("itemId", itemId)
                  .withValue("transactionTypeId", TransactionType.ALLOCATE.getPossibleValueId())
                  .withValue("fromLocationId", inv.getValueInteger("locationId"))
                  .withValue("quantity", allocateQty)
                  .withValue("lotNumber", inv.getValueString("lotNumber"))
                  .withValue("referenceType", "ORDER_LINE")
                  .withValue("referenceId", lineId)
                  .withValue("performedBy", performedBy)
                  .withValue("performedDate", Instant.now())
                  .withValue("notes", "Allocation for order " + orderId)));

               ///////////////////////////////////////////////////////////////
               // Update inventory: increment allocated, decrement available //
               ///////////////////////////////////////////////////////////////
               BigDecimal currentAllocated = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAllocated"));
               BigDecimal currentAvailable = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAvailable"));
               if(currentAllocated == null) { currentAllocated = BigDecimal.ZERO; }
               if(currentAvailable == null) { currentAvailable = BigDecimal.ZERO; }

               new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", inv.getValueInteger("id"))
                  .withValue("quantityAllocated", currentAllocated.add(allocateQty))
                  .withValue("quantityAvailable", currentAvailable.subtract(allocateQty))));

               totalAllocatedThisLine = totalAllocatedThisLine.add(allocateQty);
               remainingToAllocate = remainingToAllocate.subtract(allocateQty);
            }

            /////////////////////////////////////////////////////////////////
            // Update order line allocated quantity                        //
            /////////////////////////////////////////////////////////////////
            BigDecimal newLineAllocated = alreadyAllocated.add(totalAllocatedThisLine);
            new UpdateAction().execute(new UpdateInput(ORDER_LINE_TABLE_NAME).withRecord(new QRecord()
               .withValue("id", lineId)
               .withValue("quantityAllocated", newLineAllocated)));

            if(newLineAllocated.compareTo(quantityOrdered) < 0)
            {
               allLinesAllocated = false;
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Update order status to ALLOCATED                                //
         /////////////////////////////////////////////////////////////////////
         if(allLinesAllocated)
         {
            new UpdateAction().execute(new UpdateInput(ORDER_TABLE_NAME).withRecord(new QRecord()
               .withValue("id", orderId)
               .withValue("statusId", OrderStatus.ALLOCATED.getPossibleValueId())));
            ordersAllocated++;
         }

         LOG.info("Order allocation processed", logPair("orderId", orderId), logPair("allLinesAllocated", allLinesAllocated));
      }

      output.addValue("resultMessage", "Allocation complete. " + ordersAllocated + " orders fully allocated out of " + orders.size() + " processed.");
      output.addValue("ordersAllocated", ordersAllocated);
   }
}
