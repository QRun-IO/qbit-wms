/*******************************************************************************
 ** Completion handler for PUTAWAY tasks.  Creates inventory at the destination
 ** location, logs a PUTAWAY inventory transaction, updates receipt line status
 ** to COMPLETED, cascades completion up to receipt and purchase order, and
 ** optionally creates a billing activity for 3PL clients.
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
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsPurchaseOrder;
import com.kingsrook.qbits.wms.receiving.model.WmsReceipt;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PutawayTaskCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(PutawayTaskCompletionHandler.class);



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
      Integer destinationLocationId = task.getValueInteger("destinationLocationId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");
      Integer receiptLineId = task.getValueInteger("receiptLineId");
      Integer receiptId = task.getValueInteger("receiptId");

      LOG.info("Handling PUTAWAY task completion",
         logPair("taskId", taskId),
         logPair("itemId", itemId),
         logPair("destinationLocationId", destinationLocationId),
         logPair("quantityCompleted", quantityCompleted));

      if(quantityCompleted == null || quantityCompleted.compareTo(BigDecimal.ZERO) <= 0)
      {
         LOG.warn("PUTAWAY task has no quantity completed, skipping inventory changes", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create the inventory transaction (MUST come first)          //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, itemId,
         TransactionType.PUTAWAY,
         null, destinationLocationId,
         quantityCompleted,
         lotNumber, serialNumber,
         taskId,
         null,
         completedBy,
         "Putaway task " + taskId + " completed"
      );

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Create or increment inventory at destination location       //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, itemId, destinationLocationId,
         quantityCompleted, lotNumber, serialNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Update receipt line status to COMPLETED                     //
      /////////////////////////////////////////////////////////////////////////
      if(receiptLineId != null)
      {
         updateReceiptLineStatus(receiptLineId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Check if all receipt lines are COMPLETED -> receipt done    //
      /////////////////////////////////////////////////////////////////////////
      if(receiptId != null)
      {
         checkReceiptCompletion(receiptId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 5: If task has clientId, create billing activity               //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId, quantityCompleted);
      }
   }



   /*******************************************************************************
    ** Mark the receipt line status as COMPLETED.
    *******************************************************************************/
   private void updateReceiptLineStatus(Integer receiptLineId) throws QException
   {
      new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", receiptLineId)
         .withValue("statusId", ReceiptStatus.COMPLETED.getPossibleValueId())));
   }



   /*******************************************************************************
    ** Check if all receipt lines for this receipt are COMPLETED.  If so, mark
    ** the receipt as COMPLETED, then check if the PO can be marked RECEIVED.
    *******************************************************************************/
   private void checkReceiptCompletion(Integer receiptId) throws QException
   {
      QueryOutput linesOutput = new QueryAction().execute(new QueryInput(WmsReceiptLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("receiptId", QCriteriaOperator.EQUALS, receiptId))));

      List<QRecord> lines = linesOutput.getRecords();
      if(lines.isEmpty())
      {
         return;
      }

      boolean allCompleted = true;
      for(QRecord line : lines)
      {
         if(!Objects.equals(line.getValueInteger("statusId"), ReceiptStatus.COMPLETED.getPossibleValueId()))
         {
            allCompleted = false;
            break;
         }
      }

      if(allCompleted)
      {
         LOG.info("All receipt lines completed, marking receipt as COMPLETED", logPair("receiptId", receiptId));
         new UpdateAction().execute(new UpdateInput(WmsReceipt.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", receiptId)
            .withValue("statusId", ReceiptStatus.COMPLETED.getPossibleValueId())));

         //////////////////////////////////////////////////////////////////
         // Now check if all receipts for the PO are completed           //
         //////////////////////////////////////////////////////////////////
         QueryOutput receiptOutput = new QueryAction().execute(new QueryInput(WmsReceipt.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, receiptId))));

         if(!receiptOutput.getRecords().isEmpty())
         {
            Integer purchaseOrderId = receiptOutput.getRecords().get(0).getValueInteger("purchaseOrderId");
            if(purchaseOrderId != null)
            {
               checkPurchaseOrderCompletion(purchaseOrderId);
            }
         }
      }
   }



   /*******************************************************************************
    ** Check if all receipts for a PO are COMPLETED.  If so, mark the PO as
    ** RECEIVED.
    *******************************************************************************/
   private void checkPurchaseOrderCompletion(Integer purchaseOrderId) throws QException
   {
      QueryOutput receiptsOutput = new QueryAction().execute(new QueryInput(WmsReceipt.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("purchaseOrderId", QCriteriaOperator.EQUALS, purchaseOrderId))));

      List<QRecord> receipts = receiptsOutput.getRecords();
      if(receipts.isEmpty())
      {
         return;
      }

      boolean allCompleted = true;
      for(QRecord receipt : receipts)
      {
         if(!Objects.equals(receipt.getValueInteger("statusId"), ReceiptStatus.COMPLETED.getPossibleValueId()))
         {
            allCompleted = false;
            break;
         }
      }

      if(allCompleted)
      {
         LOG.info("All receipts for PO completed, marking PO as RECEIVED", logPair("purchaseOrderId", purchaseOrderId));
         new UpdateAction().execute(new UpdateInput(WmsPurchaseOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", purchaseOrderId)
            .withValue("statusId", PurchaseOrderStatus.RECEIVED.getPossibleValueId())));
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for RECEIVING_PER_UNIT.  The billing
    ** tables are introduced in Phase 6; this method will log a warning if the
    ** table does not yet exist rather than failing the putaway completion.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.RECEIVING_PER_UNIT.getPossibleValueId())
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
