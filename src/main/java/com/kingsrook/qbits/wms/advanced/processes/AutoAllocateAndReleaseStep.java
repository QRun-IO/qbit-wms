/*******************************************************************************
 ** Backend step for AutoAllocateAndRelease.  Finds PENDING orders for a
 ** warehouse, invokes the allocation logic, and counts how many orders were
 ** fully allocated.  This is a simplified automated version of the manual
 ** AllocateOrders process.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
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
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class AutoAllocateAndReleaseStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(AutoAllocateAndReleaseStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Query PENDING orders for this warehouse                             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput orderQuery = new QueryAction().execute(new QueryInput(WmsOrder.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, OrderStatus.PENDING.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
            .withOrderBy(new QFilterOrderBy("priority", true))));

      int ordersProcessed = 0;
      int ordersAllocated = 0;

      for(QRecord order : orderQuery.getRecords())
      {
         Integer orderId = order.getValueInteger("id");
         Integer orderClientId = order.getValueInteger("clientId");
         ordersProcessed++;

         /////////////////////////////////////////////////////////////////////
         // Query order lines                                               //
         /////////////////////////////////////////////////////////////////////
         QueryOutput linesQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

         boolean allLinesAllocated = true;

         for(QRecord line : linesQuery.getRecords())
         {
            Integer lineId = line.getValueInteger("id");
            Integer itemId = line.getValueInteger("itemId");
            BigDecimal qtyOrdered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityOrdered"));
            BigDecimal qtyAllocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));
            if(qtyOrdered == null) { qtyOrdered = BigDecimal.ZERO; }
            if(qtyAllocated == null) { qtyAllocated = BigDecimal.ZERO; }

            BigDecimal remaining = qtyOrdered.subtract(qtyAllocated);
            if(remaining.compareTo(BigDecimal.ZERO) <= 0)
            {
               continue;
            }

            //////////////////////////////////////////////////////////////////
            // Query available inventory (FEFO)                             //
            //////////////////////////////////////////////////////////////////
            QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
                  .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
                  .withCriteria(new QFilterCriteria("quantityAvailable", QCriteriaOperator.GREATER_THAN, 0))
                  .withOrderBy(new QFilterOrderBy("expirationDate", true))
                  .withOrderBy(new QFilterOrderBy("receivedDate", true))));

            BigDecimal totalAllocated = BigDecimal.ZERO;

            for(QRecord inv : invQuery.getRecords())
            {
               if(remaining.compareTo(BigDecimal.ZERO) <= 0)
               {
                  break;
               }

               BigDecimal available = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAvailable"));
               if(available == null || available.compareTo(BigDecimal.ZERO) <= 0)
               {
                  continue;
               }

               BigDecimal allocateQty = available.min(remaining);

               new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
                  .withValue("warehouseId", warehouseId)
                  .withValue("clientId", orderClientId)
                  .withValue("itemId", itemId)
                  .withValue("transactionTypeId", TransactionType.PICK.getPossibleValueId())
                  .withValue("fromLocationId", inv.getValueInteger("locationId"))
                  .withValue("quantity", allocateQty)
                  .withValue("lotNumber", inv.getValueString("lotNumber"))
                  .withValue("referenceType", "ORDER_LINE")
                  .withValue("referenceId", lineId)
                  .withValue("performedBy", "AutoAllocate")
                  .withValue("performedDate", Instant.now())
                  .withValue("notes", "Auto-allocation for order " + orderId)));

               BigDecimal curAllocated = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAllocated"));
               BigDecimal curAvailable = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAvailable"));
               if(curAllocated == null) { curAllocated = BigDecimal.ZERO; }
               if(curAvailable == null) { curAvailable = BigDecimal.ZERO; }

               new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", inv.getValueInteger("id"))
                  .withValue("quantityAllocated", curAllocated.add(allocateQty))
                  .withValue("quantityAvailable", curAvailable.subtract(allocateQty))));

               totalAllocated = totalAllocated.add(allocateQty);
               remaining = remaining.subtract(allocateQty);
            }

            BigDecimal newLineAllocated = qtyAllocated.add(totalAllocated);
            new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", lineId)
               .withValue("quantityAllocated", newLineAllocated)));

            if(newLineAllocated.compareTo(qtyOrdered) < 0)
            {
               allLinesAllocated = false;
            }
         }

         if(allLinesAllocated)
         {
            new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", orderId)
               .withValue("statusId", OrderStatus.ALLOCATED.getPossibleValueId())));
            ordersAllocated++;
         }
      }

      LOG.info("Auto allocate and release complete",
         logPair("ordersProcessed", ordersProcessed),
         logPair("ordersAllocated", ordersAllocated));

      output.addValue("resultMessage", "Auto allocate complete. " + ordersAllocated + " orders fully allocated out of " + ordersProcessed + " processed.");
      output.addValue("ordersProcessed", ordersProcessed);
   }
}
