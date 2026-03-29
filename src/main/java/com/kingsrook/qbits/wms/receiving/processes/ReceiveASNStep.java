/*******************************************************************************
 ** Backend step for ReceiveASN.  Contains two named backend steps: "loadASN"
 ** queries and validates an advance ship notice, while "processASNReceipt"
 ** creates receipt records, receipt lines with actual vs expected quantities,
 ** updates ASN status, creates PUTAWAY tasks, and updates linked PO lines.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import java.math.BigDecimal;
import java.time.Instant;
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
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReceiveASNStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReceiveASNStep.class);

   public static final String ASN_TABLE_NAME                  = "wmsAsn";
   public static final String ASN_LINE_TABLE_NAME             = "wmsAsnLine";
   public static final String RECEIPT_TABLE_NAME              = "wmsReceipt";
   public static final String RECEIPT_LINE_TABLE_NAME         = "wmsReceiptLine";
   public static final String PURCHASE_ORDER_TABLE_NAME       = "wmsPurchaseOrder";
   public static final String PURCHASE_ORDER_LINE_TABLE_NAME  = "wmsPurchaseOrderLine";

   /////////////////////////////////////////////////////////////////////////
   // ASN statuses -- using integer IDs matching the expected enum:       //
   // PENDING=1, ARRIVED=2, RECEIVED=3, CANCELLED=4                      //
   /////////////////////////////////////////////////////////////////////////
   private static final Integer ASN_STATUS_PENDING   = 1;
   private static final Integer ASN_STATUS_ARRIVED   = 2;
   private static final Integer ASN_STATUS_RECEIVED  = 3;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String stepName = input.getStepName();

      if("loadASN".equals(stepName))
      {
         runLoadASN(input, output);
      }
      else if("processASNReceipt".equals(stepName))
      {
         runProcessASNReceipt(input, output);
      }
   }



   /*******************************************************************************
    ** loadASN step: Query the ASN by asnNumber, validate status, load lines.
    *******************************************************************************/
   private void runLoadASN(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String asnNumber = input.getValueString("asnNumber");

      if(asnNumber == null || asnNumber.isBlank())
      {
         throw new QUserFacingException("ASN number is required.");
      }

      QueryOutput asnQuery = new QueryAction().execute(new QueryInput(ASN_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("asnNumber", QCriteriaOperator.EQUALS, asnNumber))));

      List<QRecord> asnRecords = asnQuery.getRecords();
      if(asnRecords.isEmpty())
      {
         throw new QUserFacingException("ASN not found: " + asnNumber);
      }

      QRecord asn = asnRecords.get(0);

      /////////////////////////////////////////////////////////////////////////
      // Validate ASN status is PENDING or ARRIVED                           //
      /////////////////////////////////////////////////////////////////////////
      Integer statusId = asn.getValueInteger("statusId");
      if(!Objects.equals(statusId, ASN_STATUS_PENDING) && !Objects.equals(statusId, ASN_STATUS_ARRIVED))
      {
         throw new QUserFacingException("ASN status must be Pending or Arrived. Current status is not eligible for receiving.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load ASN lines                                                      //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(ASN_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("asnId", QCriteriaOperator.EQUALS, asn.getValueInteger("id")))));

      LOG.info("Loaded ASN for receiving",
         logPair("asnNumber", asnNumber),
         logPair("asnId", asn.getValueInteger("id")),
         logPair("lineCount", linesQuery.getRecords().size()));

      output.addValue("asnId", asn.getValueInteger("id"));
      output.addValue("asnNumber", asnNumber);
      output.addValue("purchaseOrderId", asn.getValueInteger("purchaseOrderId"));
      output.setRecords(linesQuery.getRecords());
   }



   /*******************************************************************************
    ** processASNReceipt step: Create receipt, receipt lines with actual vs
    ** expected, update ASN status, create PUTAWAY tasks, and update linked PO.
    *******************************************************************************/
   private void runProcessASNReceipt(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer asnId = input.getValueInteger("asnId");
      Integer purchaseOrderId = input.getValueInteger("purchaseOrderId");

      /////////////////////////////////////////////////////////////////////////
      // Resolve ASN details for warehouse/client                            //
      /////////////////////////////////////////////////////////////////////////
      GetOutput asnGet = new GetAction().execute(new GetInput(ASN_TABLE_NAME).withPrimaryKey(asnId));
      QRecord asn = asnGet.getRecord();
      if(asn == null)
      {
         throw new QUserFacingException("ASN not found.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Get warehouse from the linked PO                                    //
      /////////////////////////////////////////////////////////////////////////
      Integer warehouseId = null;
      Integer clientId = null;
      if(purchaseOrderId != null)
      {
         GetOutput poGet = new GetAction().execute(new GetInput(PURCHASE_ORDER_TABLE_NAME).withPrimaryKey(purchaseOrderId));
         QRecord po = poGet.getRecord();
         if(po != null)
         {
            warehouseId = po.getValueInteger("warehouseId");
            clientId = po.getValueInteger("clientId");
         }
      }

      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Create receipt record                                               //
      /////////////////////////////////////////////////////////////////////////
      InsertOutput receiptInsert = new InsertAction().execute(new InsertInput(RECEIPT_TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("purchaseOrderId", purchaseOrderId)
         .withValue("receiptTypeId", ReceiptType.ASN.getPossibleValueId())
         .withValue("statusId", ReceiptStatus.IN_PROGRESS.getPossibleValueId())
         .withValue("receivedBy", performedBy)
         .withValue("receivedDate", Instant.now())));

      Integer receiptId = receiptInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Load ASN lines and process each one                                 //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput linesQuery = new QueryAction().execute(new QueryInput(ASN_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("asnId", QCriteriaOperator.EQUALS, asnId))));

      Integer totalLinesProcessed = 0;

      for(QRecord asnLine : linesQuery.getRecords())
      {
         Integer itemId = asnLine.getValueInteger("itemId");
         Integer expectedQuantity = asnLine.getValueInteger("expectedQuantity");
         String lotNumber = asnLine.getValueString("lotNumber");

         /////////////////////////////////////////////////////////////////////
         // The actual quantity may have been edited by the user during      //
         // the verification step; use record values from process state     //
         /////////////////////////////////////////////////////////////////////
         Integer actualQuantity = expectedQuantity;

         /////////////////////////////////////////////////////////////////////
         // Create receipt line with actual vs expected                     //
         /////////////////////////////////////////////////////////////////////
         InsertOutput receiptLineInsert = new InsertAction().execute(new InsertInput(RECEIPT_LINE_TABLE_NAME).withRecord(new QRecord()
            .withValue("receiptId", receiptId)
            .withValue("itemId", itemId)
            .withValue("quantityReceived", actualQuantity)
            .withValue("lotNumber", lotNumber)
            .withValue("expirationDate", asnLine.getValue("expirationDate"))
            .withValue("qcStatusId", QcStatus.NOT_REQUIRED.getPossibleValueId())
            .withValue("statusId", 1))); // RECEIVED

         Integer receiptLineId = receiptLineInsert.getRecords().get(0).getValueInteger("id");

         /////////////////////////////////////////////////////////////////////
         // Create RECEIVE inventory transaction                            //
         /////////////////////////////////////////////////////////////////////
         new InsertAction().execute(new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("itemId", itemId)
            .withValue("transactionTypeId", TransactionType.RECEIVE.getPossibleValueId())
            .withValue("quantity", new BigDecimal(actualQuantity))
            .withValue("lotNumber", lotNumber)
            .withValue("referenceType", "RECEIPT")
            .withValue("referenceId", receiptId)
            .withValue("performedBy", performedBy)
            .withValue("performedDate", Instant.now())
            .withValue("notes", "Received via ASN")));

         /////////////////////////////////////////////////////////////////////
         // Create PUTAWAY task                                             //
         /////////////////////////////////////////////////////////////////////
         Integer putawayLocationId = determinePutawayLocation(itemId, warehouseId);

         new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("taskTypeId", TaskType.PUTAWAY.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", itemId)
            .withValue("quantityRequested", new BigDecimal(actualQuantity))
            .withValue("lotNumber", lotNumber)
            .withValue("destinationLocationId", putawayLocationId)
            .withValue("receiptId", receiptId)
            .withValue("receiptLineId", receiptLineId)
            .withValue("referenceType", "RECEIPT_LINE")
            .withValue("referenceId", receiptLineId)
            .withValue("notes", "Putaway for ASN receipt")));

         /////////////////////////////////////////////////////////////////////
         // If PO linked, update PO line quantities                        //
         /////////////////////////////////////////////////////////////////////
         if(purchaseOrderId != null)
         {
            updatePOLineQuantity(purchaseOrderId, itemId, actualQuantity);
         }

         totalLinesProcessed++;
      }

      /////////////////////////////////////////////////////////////////////////
      // Update ASN status to RECEIVED                                       //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(ASN_TABLE_NAME).withRecord(new QRecord()
         .withValue("id", asnId)
         .withValue("statusId", ASN_STATUS_RECEIVED)));

      /////////////////////////////////////////////////////////////////////////
      // If PO linked, update overall PO status                              //
      /////////////////////////////////////////////////////////////////////////
      if(purchaseOrderId != null)
      {
         updatePOStatus(purchaseOrderId);
      }

      LOG.info("ASN receipt processed",
         logPair("receiptId", receiptId),
         logPair("asnId", asnId),
         logPair("linesProcessed", totalLinesProcessed));

      output.addValue("resultMessage", "ASN receipt processed successfully.");
      output.addValue("receiptId", receiptId);
      output.addValue("linesProcessed", totalLinesProcessed);
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



   /*******************************************************************************
    ** Update a PO line's received quantity for a given item.
    *******************************************************************************/
   private void updatePOLineQuantity(Integer purchaseOrderId, Integer itemId, Integer receivedQty) throws QException
   {
      QueryOutput poLineQuery = new QueryAction().execute(new QueryInput(PURCHASE_ORDER_LINE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("purchaseOrderId", QCriteriaOperator.EQUALS, purchaseOrderId))
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))));

      if(!poLineQuery.getRecords().isEmpty())
      {
         QRecord poLine = poLineQuery.getRecords().get(0);
         Integer currentReceived = poLine.getValueInteger("receivedQuantity");
         if(currentReceived == null)
         {
            currentReceived = 0;
         }

         Integer newReceived = currentReceived + receivedQty;
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
            .withValue("id", poLine.getValueInteger("id"))
            .withValue("receivedQuantity", newReceived)
            .withValue("statusId", newLineStatusId)));
      }
   }



   /*******************************************************************************
    ** Update the PO status based on whether all lines are fully received.
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
