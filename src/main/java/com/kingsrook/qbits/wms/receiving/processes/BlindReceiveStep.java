/*******************************************************************************
 ** Backend step for BlindReceive.  Creates a receipt without an associated
 ** purchase order (blind/unexpected receipt).  Creates RECEIVE inventory
 ** transactions and PUTAWAY tasks with directed putaway locations.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
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


public class BlindReceiveStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(BlindReceiveStep.class);

   public static final String RECEIPT_TABLE_NAME      = "wmsReceipt";
   public static final String RECEIPT_LINE_TABLE_NAME = "wmsReceiptLine";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String itemBarcode = input.getValueString("itemBarcode");
      Integer quantity = input.getValueInteger("quantity");
      String lotNumber = input.getValueString("lotNumber");
      LocalDate expirationDate = input.getValueLocalDate("expirationDate");
      Integer conditionId = input.getValueInteger("conditionId");
      Integer warehouseId = input.getValueInteger("warehouseId");

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
      Integer clientId = item.getValueInteger("clientId");

      /////////////////////////////////////////////////////////////////////////
      // Use session warehouse if not provided                               //
      /////////////////////////////////////////////////////////////////////////
      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required for blind receiving.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Resolve performer                                                   //
      /////////////////////////////////////////////////////////////////////////
      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Create wmsReceipt (blind, no PO)                                    //
      /////////////////////////////////////////////////////////////////////////
      String receiptNumber = "BLIND-" + System.currentTimeMillis();

      InsertOutput receiptInsert = new InsertAction().execute(new InsertInput(RECEIPT_TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("receiptNumber", receiptNumber)
         .withValue("receiptTypeId", ReceiptType.BLIND.getPossibleValueId())
         .withValue("statusId", ReceiptStatus.IN_PROGRESS.getPossibleValueId())
         .withValue("receivedBy", performedBy)
         .withValue("receivedDate", Instant.now())));

      Integer receiptId = receiptInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Create wmsReceiptLine                                               //
      /////////////////////////////////////////////////////////////////////////
      InsertOutput receiptLineInsert = new InsertAction().execute(new InsertInput(RECEIPT_LINE_TABLE_NAME).withRecord(new QRecord()
         .withValue("receiptId", receiptId)
         .withValue("itemId", itemId)
         .withValue("quantityReceived", quantity)
         .withValue("lotNumber", lotNumber)
         .withValue("expirationDate", expirationDate)
         .withValue("conditionId", conditionId)
         .withValue("qcStatusId", QcStatus.NOT_REQUIRED.getPossibleValueId())
         .withValue("statusId", 1))); // RECEIVED

      Integer receiptLineId = receiptLineInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Create RECEIVE inventory transaction (perpetual inventory)          //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("transactionTypeId", TransactionType.RECEIVE.getPossibleValueId())
         .withValue("quantity", new BigDecimal(quantity))
         .withValue("lotNumber", lotNumber)
         .withValue("referenceType", "RECEIPT")
         .withValue("referenceId", receiptId)
         .withValue("performedBy", performedBy)
         .withValue("performedDate", Instant.now())
         .withValue("notes", "Blind receipt")));

      /////////////////////////////////////////////////////////////////////////
      // Determine putaway location                                          //
      /////////////////////////////////////////////////////////////////////////
      Integer putawayLocationId = determinePutawayLocation(itemId, warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Create PUTAWAY task (PENDING)                                       //
      /////////////////////////////////////////////////////////////////////////
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
         .withValue("notes", "Putaway for blind receipt")));

      LOG.info("Blind receipt processed",
         logPair("receiptId", receiptId),
         logPair("itemId", itemId),
         logPair("quantity", quantity),
         logPair("putawayLocationId", putawayLocationId));

      output.addValue("resultMessage", "Blind receipt processed successfully.");
      output.addValue("receiptId", receiptId);
      output.addValue("receiptLineId", receiptLineId);
   }



   /*******************************************************************************
    ** Look up an item by its barcode (UPC, secondary, or SKU).
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
    ** Determine the best putaway location via directed putaway logic.
    *******************************************************************************/
   private Integer determinePutawayLocation(Integer itemId, Integer warehouseId) throws QException
   {
      DirectedPutawayStep putawayStep = new DirectedPutawayStep();
      RunBackendStepInput putawayInput = new RunBackendStepInput();
      putawayInput.addValue("itemId", itemId);
      putawayInput.addValue("warehouseId", warehouseId);

      RunBackendStepOutput putawayOutput = new RunBackendStepOutput();
      putawayStep.run(putawayInput, putawayOutput);

      return (putawayOutput.getValueInteger("locationId"));
   }
}
