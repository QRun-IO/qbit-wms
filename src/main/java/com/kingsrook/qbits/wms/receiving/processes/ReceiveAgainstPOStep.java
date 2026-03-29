/*******************************************************************************
 ** Backend step for ReceiveAgainstPO.  Contains two named backend steps:
 ** "loadPO" queries and validates a purchase order, while "processReceipt"
 ** creates receipt records, receipt lines, updates PO line quantities, and
 ** creates PUTAWAY and QC_INSPECT tasks following the perpetual inventory
 ** and task-centric design principles.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.PurchaseOrderStatus;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptStatus;
import com.kingsrook.qbits.wms.core.enums.ReceiptType;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReceiveAgainstPOStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReceiveAgainstPOStep.class);

   public static final String PURCHASE_ORDER_TABLE_NAME      = "wmsPurchaseOrder";
   public static final String PURCHASE_ORDER_LINE_TABLE_NAME = "wmsPurchaseOrderLine";
   public static final String RECEIPT_TABLE_NAME             = "wmsReceipt";
   public static final String RECEIPT_LINE_TABLE_NAME        = "wmsReceiptLine";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String stepName = input.getStepName();

      if("loadPO".equals(stepName))
      {
         runLoadPO(input, output);
      }
      else if("processReceipt".equals(stepName))
      {
         runProcessReceipt(input, output);
      }
   }



   /*******************************************************************************
    ** loadPO step: Query the purchase order by poNumber, validate its status,
    ** and load PO lines for review.
    *******************************************************************************/
   private void runLoadPO(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String poNumber = input.getValueString("poNumber");
      Integer purchaseOrderId = input.getValueInteger("purchaseOrderId");

      QRecord po = null;

      if(purchaseOrderId != null)
      {
         GetOutput getOutput = new GetAction().execute(new GetInput(PURCHASE_ORDER_TABLE_NAME).withPrimaryKey(purchaseOrderId));
         po = getOutput.getRecord();
      }
      else if(poNumber != null && !poNumber.isBlank())
      {
         QueryOutput queryOutput = new QueryAction().execute(new QueryInput(PURCHASE_ORDER_TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("poNumber", QCriteriaOperator.EQUALS, poNumber))));

         List<QRecord> records = queryOutput.getRecords();
         if(!records.isEmpty())
         {
            po = records.get(0);
         }
      }

      if(po == null)
      {
         throw new QUserFacingException("Purchase order not found.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Validate PO status is OPEN or PARTIALLY_RECEIVED                    //
      /////////////////////////////////////////////////////////////////////////
      Integer statusId = po.getValueInteger("statusId");
      if(!Objects.equals(statusId, PurchaseOrderStatus.OPEN.getPossibleValueId())
         && !Objects.equals(statusId, PurchaseOrderStatus.PARTIALLY_RECEIVED.getPossibleValueId()))
      {
         throw new QUserFacingException("Purchase order status must be Open or Partially Received. Current status is not eligible for receiving.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load PO lines                                                       //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(PURCHASE_ORDER_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("purchaseOrderId", QCriteriaOperator.EQUALS, po.getValueInteger("id")))));

      LOG.info("Loaded PO for receiving",
         logPair("poNumber", po.getValueString("poNumber")),
         logPair("poId", po.getValueInteger("id")),
         logPair("lineCount", linesQuery.getRecords().size()));

      output.addValue("purchaseOrderId", po.getValueInteger("id"));
      output.addValue("poNumber", po.getValueString("poNumber"));
      output.addValue("vendorId", po.getValueInteger("vendorId"));
      output.addValue("warehouseId", po.getValueInteger("warehouseId"));
      output.addValue("clientId", po.getValueInteger("clientId"));
      output.setRecords(linesQuery.getRecords());
   }



   /*******************************************************************************
    ** processReceipt step: Create receipt, receipt lines, update PO quantities,
    ** and create PUTAWAY / QC_INSPECT tasks.
    *******************************************************************************/
   private void runProcessReceipt(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer purchaseOrderId = input.getValueInteger("purchaseOrderId");
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer clientId = input.getValueInteger("clientId");
      String itemBarcode = input.getValueString("itemBarcode");
      Integer quantity = input.getValueInteger("quantity");
      String lotNumber = input.getValueString("lotNumber");
      LocalDate expirationDate = input.getValueLocalDate("expirationDate");
      Integer conditionId = input.getValueInteger("conditionId");

      if(itemBarcode == null || itemBarcode.isBlank())
      {
         throw new QUserFacingException("Item barcode is required.");
      }
      if(quantity == null || quantity <= 0)
      {
         throw new QUserFacingException("Quantity must be greater than zero.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Resolve item by barcode                                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord item = lookupItemByBarcode(itemBarcode);
      if(item == null)
      {
         throw new QUserFacingException("Item not found for barcode: " + itemBarcode);
      }
      Integer itemId = item.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Resolve performer                                                   //
      /////////////////////////////////////////////////////////////////////////
      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Create wmsReceipt record                                            //
      /////////////////////////////////////////////////////////////////////////
      String receiptNumber = "RCV-" + purchaseOrderId + "-" + System.currentTimeMillis();

      InsertOutput receiptInsert = new InsertAction().execute(new InsertInput(RECEIPT_TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("purchaseOrderId", purchaseOrderId)
         .withValue("receiptNumber", receiptNumber)
         .withValue("receiptTypeId", ReceiptType.PO_BASED.getPossibleValueId())
         .withValue("statusId", ReceiptStatus.IN_PROGRESS.getPossibleValueId())
         .withValue("receivedBy", performedBy)
         .withValue("receivedDate", Instant.now())));

      Integer receiptId = receiptInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Match to PO line                                                    //
      /////////////////////////////////////////////////////////////////////////
      QRecord poLine = findPOLineForItem(purchaseOrderId, itemId);
      Integer poLineId = poLine != null ? poLine.getValueInteger("id") : null;

      /////////////////////////////////////////////////////////////////////////
      // Determine if item requires QC                                       //
      /////////////////////////////////////////////////////////////////////////
      Boolean requiresQc = determineQcRequired(item, conditionId);

      /////////////////////////////////////////////////////////////////////////
      // Create wmsReceiptLine                                               //
      /////////////////////////////////////////////////////////////////////////
      InsertOutput receiptLineInsert = new InsertAction().execute(new InsertInput(RECEIPT_LINE_TABLE_NAME).withRecord(new QRecord()
         .withValue("receiptId", receiptId)
         .withValue("purchaseOrderLineId", poLineId)
         .withValue("itemId", itemId)
         .withValue("quantityReceived", quantity)
         .withValue("lotNumber", lotNumber)
         .withValue("expirationDate", expirationDate)
         .withValue("conditionId", conditionId)
         .withValue("qcStatusId", requiresQc ? QcStatus.PENDING.getPossibleValueId() : QcStatus.NOT_REQUIRED.getPossibleValueId())
         .withValue("statusId", requiresQc ? 2 : 1))); // QC_PENDING or RECEIVED

      Integer receiptLineId = receiptLineInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create RECEIVE inventory transaction (perpetual inventory)  //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("transactionTypeId", TransactionType.RECEIVE.getPossibleValueId())
         .withValue("toLocationId", null)
         .withValue("quantity", new BigDecimal(quantity))
         .withValue("lotNumber", lotNumber)
         .withValue("referenceType", "RECEIPT")
         .withValue("referenceId", receiptId)
         .withValue("performedBy", performedBy)
         .withValue("performedDate", Instant.now())
         .withValue("notes", "Received against PO")));

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Determine putaway location via directed putaway             //
      /////////////////////////////////////////////////////////////////////////
      Integer putawayLocationId = determinePutawayLocation(itemId, warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Create tasks based on QC requirements                       //
      /////////////////////////////////////////////////////////////////////////
      if(requiresQc)
      {
         ///////////////////////////////////////////////////////////////////
         // Create QC_INSPECT task (PENDING)                              //
         ///////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.QC_INSPECT.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", itemId)
            .withValue("quantityRequested", new BigDecimal(quantity))
            .withValue("lotNumber", lotNumber)
            .withValue("receiptId", receiptId)
            .withValue("receiptLineId", receiptLineId)
            .withValue("referenceType", "RECEIPT_LINE")
            .withValue("referenceId", receiptLineId)
            .withValue("notes", "QC inspection required for received item")));

         ///////////////////////////////////////////////////////////////////
         // Create PUTAWAY task (ON_HOLD pending QC pass)                 //
         ///////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.ON_HOLD.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", itemId)
            .withValue("quantityRequested", new BigDecimal(quantity))
            .withValue("lotNumber", lotNumber)
            .withValue("destinationLocationId", putawayLocationId)
            .withValue("receiptId", receiptId)
            .withValue("receiptLineId", receiptLineId)
            .withValue("referenceType", "RECEIPT_LINE")
            .withValue("referenceId", receiptLineId)
            .withValue("notes", "Putaway on hold pending QC inspection")));
      }
      else
      {
         ///////////////////////////////////////////////////////////////////
         // Create PUTAWAY task (PENDING) with directed putaway location  //
         ///////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", itemId)
            .withValue("quantityRequested", new BigDecimal(quantity))
            .withValue("lotNumber", lotNumber)
            .withValue("destinationLocationId", putawayLocationId)
            .withValue("receiptId", receiptId)
            .withValue("receiptLineId", receiptLineId)
            .withValue("referenceType", "RECEIPT_LINE")
            .withValue("referenceId", receiptLineId)
            .withValue("notes", "Putaway for received item")));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Update PO line received quantity                            //
      /////////////////////////////////////////////////////////////////////////
      if(poLine != null)
      {
         Integer currentReceived = poLine.getValueInteger("receivedQuantity");
         if(currentReceived == null)
         {
            currentReceived = 0;
         }
         Integer newReceived = currentReceived + quantity;
         Integer expectedQuantity = poLine.getValueInteger("expectedQuantity");

         Integer newLineStatusId;
         if(expectedQuantity != null && newReceived >= expectedQuantity)
         {
            newLineStatusId = PurchaseOrderStatus.RECEIVED.getPossibleValueId();
         }
         else
         {
            newLineStatusId = PurchaseOrderStatus.PARTIALLY_RECEIVED.getPossibleValueId();
         }

         new UpdateAction().execute(new UpdateInput(PURCHASE_ORDER_LINE_TABLE_NAME).withRecord(new QRecord()
            .withValue("id", poLineId)
            .withValue("receivedQuantity", newReceived)
            .withValue("statusId", newLineStatusId)));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 5: Update PO status based on line totals                       //
      /////////////////////////////////////////////////////////////////////////
      updatePOStatus(purchaseOrderId);

      LOG.info("PO receipt processed",
         logPair("receiptId", receiptId),
         logPair("itemId", itemId),
         logPair("quantity", quantity),
         logPair("requiresQc", requiresQc),
         logPair("putawayLocationId", putawayLocationId));

      output.addValue("resultMessage", "Receipt processed successfully.");
      output.addValue("receiptId", receiptId);
      output.addValue("receiptLineId", receiptLineId);
   }



   /*******************************************************************************
    ** Look up an item by its barcode (UPC or secondary barcode).
    *******************************************************************************/
   private QRecord lookupItemByBarcode(String barcode) throws QException
   {
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsItem.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("barcodeUpc", QCriteriaOperator.EQUALS, barcode))
            .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
            .withCriteria(new QFilterCriteria("barcodeSecondary", QCriteriaOperator.EQUALS, barcode))
            .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
            .withCriteria(new QFilterCriteria("sku", QCriteriaOperator.EQUALS, barcode))));

      if(!queryOutput.getRecords().isEmpty())
      {
         return (queryOutput.getRecords().get(0));
      }

      return (null);
   }



   /*******************************************************************************
    ** Find the PO line that matches a given item.
    *******************************************************************************/
   private QRecord findPOLineForItem(Integer purchaseOrderId, Integer itemId) throws QException
   {
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(PURCHASE_ORDER_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("purchaseOrderId", QCriteriaOperator.EQUALS, purchaseOrderId))
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))));

      if(!queryOutput.getRecords().isEmpty())
      {
         return (queryOutput.getRecords().get(0));
      }

      return (null);
   }



   /*******************************************************************************
    ** Determine whether QC is required for a received item, based on condition
    ** or item attributes.
    *******************************************************************************/
   private Boolean determineQcRequired(QRecord item, Integer conditionId)
   {
      /////////////////////////////////////////////////////////////////////////
      // If condition is DAMAGED or DEFECTIVE, always require QC             //
      /////////////////////////////////////////////////////////////////////////
      if(conditionId != null && conditionId > 1)
      {
         return (true);
      }

      /////////////////////////////////////////////////////////////////////////
      // Future: check vendor QC rules, item-level QC flags, etc.           //
      /////////////////////////////////////////////////////////////////////////
      return (false);
   }



   /*******************************************************************************
    ** Use the DirectedPutawayStep logic to determine the best putaway location.
    *******************************************************************************/
   private Integer determinePutawayLocation(Integer itemId, Integer warehouseId) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Inline the directed putaway logic for simplicity                    //
      /////////////////////////////////////////////////////////////////////////
      DirectedPutawayStep putawayStep = new DirectedPutawayStep();
      RunBackendStepInput putawayInput = new RunBackendStepInput();
      putawayInput.addValue("itemId", itemId);
      putawayInput.addValue("warehouseId", warehouseId);

      RunBackendStepOutput putawayOutput = new RunBackendStepOutput();
      putawayStep.run(putawayInput, putawayOutput);

      return (putawayOutput.getValueInteger("locationId"));
   }



   /*******************************************************************************
    ** Update the PO status to PARTIALLY_RECEIVED or RECEIVED based on whether
    ** all lines have been fully received.
    *******************************************************************************/
   private void updatePOStatus(Integer purchaseOrderId) throws QException
   {
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(PURCHASE_ORDER_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("purchaseOrderId", QCriteriaOperator.EQUALS, purchaseOrderId))));

      Boolean allReceived = true;
      for(QRecord line : linesQuery.getRecords())
      {
         Integer expected = line.getValueInteger("expectedQuantity");
         Integer received = line.getValueInteger("receivedQuantity");
         if(expected != null && (received == null || received < expected))
         {
            allReceived = false;
            break;
         }
      }

      Integer newStatusId;
      if(allReceived)
      {
         newStatusId = PurchaseOrderStatus.RECEIVED.getPossibleValueId();
      }
      else
      {
         newStatusId = PurchaseOrderStatus.PARTIALLY_RECEIVED.getPossibleValueId();
      }

      new UpdateAction().execute(new UpdateInput(PURCHASE_ORDER_TABLE_NAME).withRecord(new QRecord()
         .withValue("id", purchaseOrderId)
         .withValue("statusId", newStatusId)));
   }
}
