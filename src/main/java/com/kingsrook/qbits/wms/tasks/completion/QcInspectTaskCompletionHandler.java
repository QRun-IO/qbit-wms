/*******************************************************************************
 ** Completion handler for QC_INSPECT tasks.  Reads the inspection result from
 ** the countedQuantity field (1 = PASS, 2 = FAIL).  On pass, the associated
 ** PUTAWAY task is released from ON_HOLD to PENDING.  On fail, an inventory
 ** hold record is created and a HOLD transaction is logged.
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
import com.kingsrook.qbits.wms.core.enums.HoldType;
import com.kingsrook.qbits.wms.core.enums.QcStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.receiving.model.WmsReceiptLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class QcInspectTaskCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(QcInspectTaskCompletionHandler.class);

   /////////////////////////////////////////////////////////////////////////////
   // Inspection result codes stored in the countedQuantity field             //
   /////////////////////////////////////////////////////////////////////////////
   private static final BigDecimal RESULT_PASS = BigDecimal.ONE;
   private static final BigDecimal RESULT_FAIL = new BigDecimal("2");



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
      Integer receiptLineId = task.getValueInteger("receiptLineId");
      BigDecimal inspectionResult = task.getValueBigDecimal("countedQuantity");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");
      Integer sourceLocationId = task.getValueInteger("sourceLocationId");

      LOG.info("Handling QC_INSPECT task completion",
         logPair("taskId", taskId),
         logPair("receiptLineId", receiptLineId),
         logPair("inspectionResult", inspectionResult));

      if(inspectionResult == null)
      {
         LOG.warn("QC_INSPECT task has no inspection result (countedQuantity), skipping", logPair("taskId", taskId));
         return;
      }

      if(inspectionResult.compareTo(RESULT_PASS) == 0)
      {
         handlePass(taskId, receiptLineId);
      }
      else if(inspectionResult.compareTo(RESULT_FAIL) == 0)
      {
         handleFail(taskId, warehouseId, clientId, itemId, receiptLineId,
            lotNumber, serialNumber, completedBy, sourceLocationId);
      }
      else
      {
         LOG.warn("Unexpected QC inspection result value",
            logPair("taskId", taskId), logPair("inspectionResult", inspectionResult));
      }
   }



   /*******************************************************************************
    ** Handle a PASS result: update receipt line QC status to PASSED and release
    ** the associated PUTAWAY task from ON_HOLD to PENDING.
    *******************************************************************************/
   private void handlePass(Integer taskId, Integer receiptLineId) throws QException
   {
      LOG.info("QC inspection PASSED", logPair("taskId", taskId), logPair("receiptLineId", receiptLineId));

      /////////////////////////////////////////////////////////////////////////
      // Update receipt line QC status to PASSED                             //
      /////////////////////////////////////////////////////////////////////////
      if(receiptLineId != null)
      {
         new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", receiptLineId)
            .withValue("qcStatusId", QcStatus.PASSED.getPossibleValueId())));
      }

      /////////////////////////////////////////////////////////////////////////
      // Find the associated PUTAWAY task (ON_HOLD, same receiptLineId)      //
      // and release it to PENDING                                           //
      /////////////////////////////////////////////////////////////////////////
      if(receiptLineId != null)
      {
         releasePutawayTask(receiptLineId);
      }
   }



   /*******************************************************************************
    ** Handle a FAIL result: update receipt line QC status to FAILED, create an
    ** inventory hold record, and log a HOLD transaction.  The associated PUTAWAY
    ** task remains ON_HOLD for supervisor intervention.
    *******************************************************************************/
   private void handleFail(Integer taskId, Integer warehouseId, Integer clientId, Integer itemId,
      Integer receiptLineId, String lotNumber, String serialNumber, String completedBy,
      Integer sourceLocationId) throws QException
   {
      LOG.info("QC inspection FAILED", logPair("taskId", taskId), logPair("receiptLineId", receiptLineId));

      /////////////////////////////////////////////////////////////////////////
      // Update receipt line QC status to FAILED                             //
      /////////////////////////////////////////////////////////////////////////
      if(receiptLineId != null)
      {
         new UpdateAction().execute(new UpdateInput(WmsReceiptLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", receiptLineId)
            .withValue("qcStatusId", QcStatus.FAILED.getPossibleValueId())));
      }

      /////////////////////////////////////////////////////////////////////////
      // Create inventory hold record                                        //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("lotNumber", lotNumber)
         .withValue("locationId", sourceLocationId)
         .withValue("holdTypeId", HoldType.QC.getPossibleValueId())
         .withValue("reason", "QC inspection failed for task " + taskId)
         .withValue("placedBy", completedBy)
         .withValue("placedDate", Instant.now())
         .withValue("status", "ACTIVE")));

      /////////////////////////////////////////////////////////////////////////
      // Create HOLD inventory transaction                                   //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, itemId,
         TransactionType.HOLD,
         sourceLocationId, null,
         BigDecimal.ZERO,
         lotNumber, serialNumber,
         taskId,
         "QC_FAIL",
         completedBy,
         "QC inspection failed for task " + taskId
      );
   }



   /*******************************************************************************
    ** Find the PUTAWAY task that is ON_HOLD for the given receipt line and
    ** release it to PENDING status so it enters the work queue.
    *******************************************************************************/
   private void releasePutawayTask(Integer receiptLineId) throws QException
   {
      QueryOutput putawayQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("receiptLineId", QCriteriaOperator.EQUALS, receiptLineId))
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PUTAWAY.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.ON_HOLD.getPossibleValueId()))));

      List<QRecord> putawayTasks = putawayQuery.getRecords();
      if(putawayTasks.isEmpty())
      {
         LOG.warn("No ON_HOLD PUTAWAY task found for receipt line", logPair("receiptLineId", receiptLineId));
         return;
      }

      for(QRecord putawayTask : putawayTasks)
      {
         Integer putawayTaskId = putawayTask.getValueInteger("id");
         LOG.info("Releasing PUTAWAY task from ON_HOLD to PENDING",
            logPair("putawayTaskId", putawayTaskId), logPair("receiptLineId", receiptLineId));

         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", putawayTaskId)
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())));
      }
   }
}
