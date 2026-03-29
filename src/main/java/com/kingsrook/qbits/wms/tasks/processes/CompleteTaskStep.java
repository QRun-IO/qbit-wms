/*******************************************************************************
 ** Backend steps for the CompleteTask process.  Organized as inner classes
 ** corresponding to the validate-source, validate-item, validate-destination,
 ** and execute-completion stages of the process.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.tasks.completion.TaskCompletionDispatcher;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CompleteTaskStep
{
   private static final QLogger LOG = QLogger.getLogger(CompleteTaskStep.class);



   /*******************************************************************************
    ** Validate that the scanned source location matches the task's source.
    ** Transitions the task from ASSIGNED to IN_PROGRESS.
    *******************************************************************************/
   public static class ValidateSourceStep implements BackendStep
   {
      @Override
      public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         Integer taskId = input.getValueInteger("taskId");
         String scannedBarcode = input.getValueString("scannedSourceBarcode");

         QRecord task = getTask(taskId);
         Integer sourceLocationId = task.getValueInteger("sourceLocationId");

         if(sourceLocationId != null && scannedBarcode != null)
         {
            QRecord location = lookupLocationByBarcode(scannedBarcode);
            if(location == null || !sourceLocationId.equals(location.getValueInteger("id")))
            {
               throw new QUserFacingException("Scanned location does not match the expected source location for this task.");
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Transition to IN_PROGRESS                                       //
         /////////////////////////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", taskId)
            .withValue("taskStatusId", TaskStatus.IN_PROGRESS.getPossibleValueId())
            .withValue("startedDate", Instant.now())));

         LOG.info("Source validated, task now IN_PROGRESS", logPair("taskId", taskId));
      }
   }



   /*******************************************************************************
    ** Validate that the scanned item matches the task's item.
    *******************************************************************************/
   public static class ValidateItemStep implements BackendStep
   {
      @Override
      public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         Integer taskId = input.getValueInteger("taskId");
         String scannedItemBarcode = input.getValueString("scannedItemBarcode");

         QRecord task = getTask(taskId);
         Integer expectedItemId = task.getValueInteger("itemId");

         if(expectedItemId != null && scannedItemBarcode != null)
         {
            ///////////////////////////////////////////////////////////////////
            // Look up item by barcode_upc or barcode_secondary              //
            ///////////////////////////////////////////////////////////////////
            QueryOutput queryOutput = new QueryAction().execute(new QueryInput("wmsItem")
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("barcodeUpc", QCriteriaOperator.EQUALS, scannedItemBarcode))
                  .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
                  .withCriteria(new QFilterCriteria("barcodeSecondary", QCriteriaOperator.EQUALS, scannedItemBarcode))));

            List<QRecord> items = queryOutput.getRecords();
            if(items.isEmpty())
            {
               throw new QUserFacingException("Item not found for barcode: " + scannedItemBarcode);
            }

            boolean matchFound = false;
            for(QRecord item : items)
            {
               if(expectedItemId.equals(item.getValueInteger("id")))
               {
                  matchFound = true;
                  break;
               }
            }

            if(!matchFound)
            {
               throw new QUserFacingException("Scanned item does not match the expected item for this task.");
            }
         }

         LOG.info("Item validated", logPair("taskId", taskId));
      }
   }



   /*******************************************************************************
    ** Validate the scanned destination location.
    *******************************************************************************/
   public static class ValidateDestinationStep implements BackendStep
   {
      @Override
      public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         Integer taskId = input.getValueInteger("taskId");
         String scannedBarcode = input.getValueString("scannedDestinationBarcode");

         QRecord task = getTask(taskId);
         Integer destinationLocationId = task.getValueInteger("destinationLocationId");

         if(destinationLocationId != null && scannedBarcode != null)
         {
            QRecord location = lookupLocationByBarcode(scannedBarcode);
            if(location == null || !destinationLocationId.equals(location.getValueInteger("id")))
            {
               throw new QUserFacingException("Scanned location does not match the expected destination location for this task.");
            }
         }

         LOG.info("Destination validated", logPair("taskId", taskId));
      }
   }



   /*******************************************************************************
    ** Mark the task COMPLETED (or SHORT) and fire the TaskCompletionDispatcher.
    *******************************************************************************/
   public static class ExecuteCompletionStep implements BackendStep
   {
      @Override
      public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         Integer taskId = input.getValueInteger("taskId");
         BigDecimal quantityCompleted = ValueUtils.getValueAsBigDecimal(input.getValue("quantityCompleted"));

         QRecord task = getTask(taskId);
         BigDecimal quantityRequested = task.getValueBigDecimal("quantityRequested");

         /////////////////////////////////////////////////////////////////////
         // Determine final status                                          //
         /////////////////////////////////////////////////////////////////////
         Integer finalStatusId;
         if(quantityRequested != null && quantityCompleted != null
            && quantityCompleted.compareTo(quantityRequested) < 0)
         {
            finalStatusId = TaskStatus.SHORT.getPossibleValueId();
         }
         else
         {
            finalStatusId = TaskStatus.COMPLETED.getPossibleValueId();
         }

         /////////////////////////////////////////////////////////////////////
         // Resolve performer identity from session                         //
         /////////////////////////////////////////////////////////////////////
         String completedBy = null;
         if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
         {
            completedBy = QContext.getQSession().getUser().getFullName();
         }

         /////////////////////////////////////////////////////////////////////
         // Update task record                                              //
         /////////////////////////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", taskId)
            .withValue("taskStatusId", finalStatusId)
            .withValue("quantityCompleted", quantityCompleted)
            .withValue("completedDate", Instant.now())
            .withValue("completedBy", completedBy)));

         /////////////////////////////////////////////////////////////////////
         // Refresh the task record with updated values for the dispatcher  //
         /////////////////////////////////////////////////////////////////////
         QRecord updatedTask = getTask(taskId);
         if(quantityCompleted != null)
         {
            updatedTask.setValue("quantityCompleted", quantityCompleted);
         }
         updatedTask.setValue("completedBy", completedBy);

         /////////////////////////////////////////////////////////////////////
         // Fire the completion dispatcher                                  //
         /////////////////////////////////////////////////////////////////////
         TaskCompletionDispatcher.complete(updatedTask);

         TaskStatus finalStatus = TaskStatus.getById(finalStatusId);
         LOG.info("Task completed",
            logPair("taskId", taskId),
            logPair("status", finalStatus != null ? finalStatus.getLabel() : "Unknown"),
            logPair("quantityCompleted", quantityCompleted));

         /////////////////////////////////////////////////////////////////////
         // Populate output for the result screen                           //
         /////////////////////////////////////////////////////////////////////
         output.addValue("resultMessage", "Task completed successfully.");
         output.addValue("completedTaskId", taskId);
         output.addValue("completedStatus", finalStatus != null ? finalStatus.getLabel() : "Completed");
         output.addValue("completedQuantity", quantityCompleted);
      }
   }



   /*******************************************************************************
    ** Helper: fetch a task by ID.
    *******************************************************************************/
   private static QRecord getTask(Integer taskId) throws QException
   {
      if(taskId == null)
      {
         throw new QUserFacingException("Task ID is required.");
      }

      GetOutput getOutput = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      QRecord task = getOutput.getRecord();
      if(task == null)
      {
         throw new QUserFacingException("Task not found: " + taskId);
      }
      return (task);
   }



   /*******************************************************************************
    ** Helper: look up a location by barcode.
    *******************************************************************************/
   private static QRecord lookupLocationByBarcode(String barcode) throws QException
   {
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsLocation.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("barcode", QCriteriaOperator.EQUALS, barcode))));

      List<QRecord> locations = queryOutput.getRecords();
      return locations.isEmpty() ? null : locations.get(0);
   }
}
