/*******************************************************************************
 ** Backend step for InventoryMove.  Creates a MOVE task and immediately
 ** completes it, triggering the MoveTaskCompletionHandler to update inventory.
 ** This is used for manual moves that bypass the normal task queue.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import java.util.List;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.tasks.completion.TaskCompletionDispatcher;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


public class InventoryMoveStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(InventoryMoveStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer sourceLocationId = input.getValueInteger("sourceLocationId");
      Integer destinationLocationId = input.getValueInteger("destinationLocationId");
      Integer itemId = input.getValueInteger("itemId");
      BigDecimal quantityToMove = ValueUtils.getValueAsBigDecimal(input.getValue("quantityToMove"));

      if(sourceLocationId == null || destinationLocationId == null || itemId == null || quantityToMove == null)
      {
         throw new QUserFacingException("Source location, destination location, item, and quantity are all required.");
      }

      if(quantityToMove.compareTo(BigDecimal.ZERO) <= 0)
      {
         throw new QUserFacingException("Quantity to move must be greater than zero.");
      }

      if(sourceLocationId.equals(destinationLocationId))
      {
         throw new QUserFacingException("Source and destination locations must be different.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Validate sufficient inventory at source                             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, sourceLocationId))));

      List<QRecord> invRecords = invQuery.getRecords();
      if(invRecords.isEmpty())
      {
         throw new QUserFacingException("No inventory found for this item at the source location.");
      }

      QRecord sourceInv = invRecords.get(0);
      BigDecimal available = sourceInv.getValueBigDecimal("quantityAvailable");
      if(available == null || available.compareTo(quantityToMove) < 0)
      {
         throw new QUserFacingException("Insufficient available quantity at source. Available: "
            + (available != null ? available : BigDecimal.ZERO));
      }

      Integer warehouseId = sourceInv.getValueInteger("warehouseId");
      Integer clientId = sourceInv.getValueInteger("clientId");

      /////////////////////////////////////////////////////////////////////////
      // Resolve performer                                                   //
      /////////////////////////////////////////////////////////////////////////
      String performedBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         performedBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Create a MOVE task                                                  //
      /////////////////////////////////////////////////////////////////////////
      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("taskTypeId", TaskType.MOVE.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("quantityRequested", quantityToMove)
         .withValue("quantityCompleted", quantityToMove)
         .withValue("sourceLocationId", sourceLocationId)
         .withValue("destinationLocationId", destinationLocationId)
         .withValue("assignedTo", performedBy)
         .withValue("assignedDate", Instant.now())
         .withValue("startedDate", Instant.now())
         .withValue("completedDate", Instant.now())
         .withValue("completedBy", performedBy)
         .withValue("notes", "Immediate move via Inventory Move process")))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      LOG.info("Created and completing immediate MOVE task",
         logPair("taskId", taskId),
         logPair("itemId", itemId),
         logPair("quantity", quantityToMove),
         logPair("from", sourceLocationId),
         logPair("to", destinationLocationId));

      /////////////////////////////////////////////////////////////////////////
      // Immediately complete the task via the dispatcher                    //
      /////////////////////////////////////////////////////////////////////////
      TaskCompletionDispatcher.complete(taskRecord);

      output.addValue("resultMessage", "Inventory moved successfully.");
      output.addValue("movedTaskId", taskId);
   }
}
