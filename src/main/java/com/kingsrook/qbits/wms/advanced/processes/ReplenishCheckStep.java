/*******************************************************************************
 ** Backend step for ReplenishCheck.  Queries active replenishment rules,
 ** compares pick location inventory to min threshold, and creates REPLENISH
 ** tasks for items below min (filling to max).
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.advanced.model.WmsReplenishmentRule;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReplenishCheckStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReplenishCheckStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Query active replenishment rules, ordered by priority               //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput ruleQuery = new QueryAction().execute(new QueryInput(WmsReplenishmentRule.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))
            .withOrderBy(new QFilterOrderBy("priority", true))));

      int tasksCreated = 0;

      for(QRecord rule : ruleQuery.getRecords())
      {
         Integer ruleId = rule.getValueInteger("id");
         Integer warehouseId = rule.getValueInteger("warehouseId");
         Integer itemId = rule.getValueInteger("itemId");
         Integer pickLocationId = rule.getValueInteger("pickLocationId");
         Integer minQuantity = rule.getValueInteger("minQuantity");
         Integer maxQuantity = rule.getValueInteger("maxQuantity");
         Integer priority = rule.getValueInteger("priority");

         if(minQuantity == null || maxQuantity == null)
         {
            continue;
         }

         /////////////////////////////////////////////////////////////////////
         // Check current inventory at the pick location                    //
         /////////////////////////////////////////////////////////////////////
         QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
               .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, pickLocationId))
               .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))));

         BigDecimal currentQoh = BigDecimal.ZERO;
         for(QRecord inv : invQuery.getRecords())
         {
            BigDecimal qoh = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityOnHand"));
            if(qoh != null)
            {
               currentQoh = currentQoh.add(qoh);
            }
         }

         /////////////////////////////////////////////////////////////////////
         // If below min, check no pending replenish task already exists    //
         /////////////////////////////////////////////////////////////////////
         if(currentQoh.compareTo(new BigDecimal(minQuantity)) < 0)
         {
            QueryOutput existingTasks = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.REPLENISH.getPossibleValueId()))
                  .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
                  .withCriteria(new QFilterCriteria("destinationLocationId", QCriteriaOperator.EQUALS, pickLocationId))
                  .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.IN,
                     List.of(TaskStatus.PENDING.getPossibleValueId(), TaskStatus.ASSIGNED.getPossibleValueId(), TaskStatus.IN_PROGRESS.getPossibleValueId())))));

            if(!existingTasks.getRecords().isEmpty())
            {
               LOG.info("Replenish task already exists for rule", logPair("ruleId", ruleId));
               continue;
            }

            //////////////////////////////////////////////////////////////////
            // Create REPLENISH task: fill to max                           //
            //////////////////////////////////////////////////////////////////
            BigDecimal replenishQty = new BigDecimal(maxQuantity).subtract(currentQoh);

            new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
               .withValue("warehouseId", warehouseId)
               .withValue("taskTypeId", TaskType.REPLENISH.getPossibleValueId())
               .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
               .withValue("priority", priority != null ? priority : 5)
               .withValue("itemId", itemId)
               .withValue("destinationLocationId", pickLocationId)
               .withValue("quantityRequested", replenishQty)
               .withValue("referenceType", "REPLENISHMENT_RULE")
               .withValue("referenceId", ruleId)
               .withValue("notes", "Auto-replenish for rule " + ruleId + ", qty " + replenishQty)));

            tasksCreated++;

            LOG.info("Created replenish task",
               logPair("ruleId", ruleId),
               logPair("itemId", itemId),
               logPair("currentQoh", currentQoh),
               logPair("replenishQty", replenishQty));
         }
      }

      output.addValue("resultMessage", "Replenishment check complete. " + tasksCreated + " tasks created.");
      output.addValue("tasksCreated", tasksCreated);
   }
}
