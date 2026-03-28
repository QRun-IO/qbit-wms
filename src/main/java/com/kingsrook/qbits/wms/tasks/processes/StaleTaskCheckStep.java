/*******************************************************************************
 ** Backend step for the StaleTaskCheck scheduled process.  Finds ASSIGNED tasks
 ** whose assignedDate is older than the configured threshold and unassigns them
 ** back to PENDING status.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class StaleTaskCheckStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(StaleTaskCheckStep.class);

   /////////////////////////////////////////////////////////////////////////////
   // Default threshold in minutes for tasks in ASSIGNED status               //
   /////////////////////////////////////////////////////////////////////////////
   private static final Integer DEFAULT_STALE_ASSIGNED_MINUTES = 30;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Instant staleThreshold = Instant.now().minus(DEFAULT_STALE_ASSIGNED_MINUTES, ChronoUnit.MINUTES);

      LOG.info("Checking for stale ASSIGNED tasks", logPair("threshold", staleThreshold));

      /////////////////////////////////////////////////////////////////////////
      // Find ASSIGNED tasks that have been assigned longer than threshold   //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.ASSIGNED.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("assignedDate", QCriteriaOperator.LESS_THAN, staleThreshold))));

      List<QRecord> staleTasks = queryOutput.getRecords();

      if(staleTasks.isEmpty())
      {
         LOG.info("No stale tasks found");
         return;
      }

      LOG.info("Found stale tasks to unassign", logPair("count", staleTasks.size()));

      Integer unassignedCount = 0;
      for(QRecord staleTask : staleTasks)
      {
         Integer taskId = staleTask.getValueInteger("id");

         String existingNotes = staleTask.getValueString("notes");
         String updatedNotes = (existingNotes != null ? existingNotes + "\n" : "")
            + "AUTO-UNASSIGNED: Task was stale (assigned for >" + DEFAULT_STALE_ASSIGNED_MINUTES + " minutes)";

         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", taskId)
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("assignedTo", null)
            .withValue("assignedDate", null)
            .withValue("notes", updatedNotes)));

         LOG.info("Unassigned stale task", logPair("taskId", taskId));
         unassignedCount++;
      }

      LOG.info("Stale task check complete", logPair("unassignedCount", unassignedCount));
   }
}
