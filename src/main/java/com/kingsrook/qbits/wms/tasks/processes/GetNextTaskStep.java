/*******************************************************************************
 ** Backend step for GetNextTask.  Queries for the highest-priority PENDING task
 ** matching the worker's optional zone and equipment filters, then assigns it
 ** to the worker.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class GetNextTaskStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(GetNextTaskStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      String workerUserId = input.getValueString("workerUserId");
      Integer zoneId = input.getValueInteger("zoneId");
      Integer equipmentTypeId = input.getValueInteger("equipmentTypeId");

      LOG.info("Finding next task",
         logPair("workerUserId", workerUserId),
         logPair("zoneId", zoneId),
         logPair("equipmentTypeId", equipmentTypeId));

      /////////////////////////////////////////////////////////////////////////
      // Build filter for PENDING tasks                                      //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.PENDING.getPossibleValueId()))
         .withOrderBy(new QFilterOrderBy("priority", true))
         .withOrderBy(new QFilterOrderBy("createDate", true))
         .withLimit(1);

      if(zoneId != null)
      {
         filter.withCriteria(new QFilterCriteria("zoneId", QCriteriaOperator.EQUALS, zoneId));
      }

      if(equipmentTypeId != null)
      {
         filter.withCriteria(new QFilterCriteria("equipmentTypeId", QCriteriaOperator.EQUALS, equipmentTypeId));
      }

      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME).withFilter(filter));
      List<QRecord> tasks = queryOutput.getRecords();

      if(tasks.isEmpty())
      {
         output.addValue("resultMessage", "No tasks available matching your criteria.");
         return;
      }

      QRecord task = tasks.get(0);
      Integer taskId = task.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Assign the task to the worker                                       //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", taskId)
         .withValue("taskStatusId", TaskStatus.ASSIGNED.getPossibleValueId())
         .withValue("assignedTo", workerUserId)
         .withValue("assignedDate", Instant.now())));

      LOG.info("Task assigned", logPair("taskId", taskId), logPair("workerUserId", workerUserId));

      /////////////////////////////////////////////////////////////////////////
      // Populate output fields for the showTask frontend step               //
      /////////////////////////////////////////////////////////////////////////
      Integer taskTypeId = task.getValueInteger("taskTypeId");
      TaskType taskType = TaskType.getById(taskTypeId);

      output.addValue("taskId", taskId);
      output.addValue("taskType", taskType != null ? taskType.getLabel() : "Unknown");
      output.addValue("sourceLocation", task.getValueString("sourceLocationId"));
      output.addValue("itemName", task.getValueString("itemId"));
      output.addValue("quantityRequested", task.getValueBigDecimal("quantityRequested"));
      output.addValue("destinationLocation", task.getValueString("destinationLocationId"));
      output.addValue("taskNotes", task.getValueString("notes"));
      output.addValue("resultMessage", "Task assigned successfully.");
   }
}
