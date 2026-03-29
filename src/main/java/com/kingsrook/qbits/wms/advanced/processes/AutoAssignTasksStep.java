/*******************************************************************************
 ** Backend step for AutoAssignTasks.  Queries PENDING tasks by priority,
 ** finds available workers (identified by distinct assignedTo values from
 ** completed tasks who do not currently have an IN_PROGRESS task), and assigns
 ** tasks to workers in a round-robin fashion.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class AutoAssignTasksStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(AutoAssignTasksStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Discover known workers from previously assigned/completed tasks     //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput assignedTaskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("assignedTo", QCriteriaOperator.IS_NOT_BLANK))));

      Set<String> knownWorkers = new LinkedHashSet<>();
      for(QRecord task : assignedTaskQuery.getRecords())
      {
         String assignedTo = task.getValueString("assignedTo");
         if(assignedTo != null)
         {
            knownWorkers.add(assignedTo);
         }
      }

      if(knownWorkers.isEmpty())
      {
         output.addValue("resultMessage", "No known workers found for auto-assignment. Assign at least one task manually first.");
         output.addValue("tasksAssigned", 0);
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Find workers that currently have IN_PROGRESS tasks (busy)           //
      /////////////////////////////////////////////////////////////////////////
      Set<String> busyWorkers = new HashSet<>();
      for(QRecord task : assignedTaskQuery.getRecords())
      {
         Integer statusId = task.getValueInteger("taskStatusId");
         if(statusId != null && statusId.equals(TaskStatus.IN_PROGRESS.getPossibleValueId()))
         {
            String assignedTo = task.getValueString("assignedTo");
            if(assignedTo != null)
            {
               busyWorkers.add(assignedTo);
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Build list of available workers                                     //
      /////////////////////////////////////////////////////////////////////////
      List<String> availableWorkers = new ArrayList<>();
      for(String worker : knownWorkers)
      {
         if(!busyWorkers.contains(worker))
         {
            availableWorkers.add(worker);
         }
      }

      if(availableWorkers.isEmpty())
      {
         output.addValue("resultMessage", "All workers are currently busy.");
         output.addValue("tasksAssigned", 0);
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Query PENDING tasks ordered by priority                             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput pendingTaskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.PENDING.getPossibleValueId()))
            .withOrderBy(new QFilterOrderBy("priority", true))));

      int tasksAssigned = 0;
      int workerIndex = 0;

      for(QRecord task : pendingTaskQuery.getRecords())
      {
         if(workerIndex >= availableWorkers.size())
         {
            break;
         }

         String workerName = availableWorkers.get(workerIndex);

         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", task.getValueInteger("id"))
            .withValue("taskStatusId", TaskStatus.ASSIGNED.getPossibleValueId())
            .withValue("assignedTo", workerName)));

         tasksAssigned++;
         workerIndex++;

         LOG.info("Auto-assigned task",
            logPair("taskId", task.getValueInteger("id")),
            logPair("assignedTo", workerName));
      }

      output.addValue("resultMessage", "Auto-assign complete. " + tasksAssigned + " tasks assigned to " + workerIndex + " workers.");
      output.addValue("tasksAssigned", tasksAssigned);
   }
}
