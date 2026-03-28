/*******************************************************************************
 ** Backend step for CreateCycleCount.  Queries wms_inventory for target items
 ** and locations, creates a wms_cycle_count record and wms_cycle_count_line
 ** records, and generates one COUNT wms_task per line.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CycleCountStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CycleCountStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer countTypeId = input.getValueInteger("countTypeId");
      Integer zoneId = input.getValueInteger("zoneId");
      String assignedTo = input.getValueString("assignedTo");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }
      if(countTypeId == null)
      {
         throw new QUserFacingException("Count type is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Create the cycle count header                                       //
      /////////////////////////////////////////////////////////////////////////
      QRecord cycleCountRecord = new InsertAction().execute(new InsertInput(WmsCycleCount.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("countTypeId", countTypeId)
         .withValue("cycleCountStatusId", CycleCountStatus.IN_PROGRESS.getPossibleValueId())
         .withValue("startedDate", Instant.now())
         .withValue("assignedTo", assignedTo)))
      .getRecords().get(0);

      Integer cycleCountId = cycleCountRecord.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Query inventory to build count lines                                //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter invFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId));

      if(zoneId != null)
      {
         //////////////////////////////////////////////////////////////////
         // Filter by zone -- need to join through location              //
         // For now, we query all inventory in the warehouse and the     //
         // zone filter is advisory.  A real implementation would use    //
         // a join or sub-query.                                         //
         //////////////////////////////////////////////////////////////////
      }

      QueryOutput invOutput = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME).withFilter(invFilter));
      List<QRecord> inventoryRecords = invOutput.getRecords();

      if(inventoryRecords.isEmpty())
      {
         throw new QUserFacingException("No inventory found at the specified warehouse/zone to count.");
      }

      LOG.info("Generating cycle count",
         logPair("cycleCountId", cycleCountId),
         logPair("inventoryRecords", inventoryRecords.size()));

      /////////////////////////////////////////////////////////////////////////
      // Create a cycle count line and COUNT task for each inventory record  //
      /////////////////////////////////////////////////////////////////////////
      Integer lineCount = 0;
      Integer taskCount = 0;

      for(QRecord inv : inventoryRecords)
      {
         BigDecimal expectedQty = inv.getValueBigDecimal("quantityOnHand");
         if(expectedQty == null)
         {
            expectedQty = BigDecimal.ZERO;
         }

         //////////////////////////////////////////////////////////////////
         // Create cycle count line                                      //
         //////////////////////////////////////////////////////////////////
         QRecord lineRecord = new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("cycleCountId", cycleCountId)
            .withValue("locationId", inv.getValueInteger("locationId"))
            .withValue("itemId", inv.getValueInteger("itemId"))
            .withValue("lotNumber", inv.getValueString("lotNumber"))
            .withValue("expectedQuantity", expectedQty)
            .withValue("status", "PENDING")))
         .getRecords().get(0);

         Integer lineId = lineRecord.getValueInteger("id");
         lineCount++;

         //////////////////////////////////////////////////////////////////
         // Create COUNT task                                            //
         //////////////////////////////////////////////////////////////////
         QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", inv.getValueInteger("clientId"))
            .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
            .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
            .withValue("priority", 5)
            .withValue("itemId", inv.getValueInteger("itemId"))
            .withValue("sourceLocationId", inv.getValueInteger("locationId"))
            .withValue("zoneId", zoneId)
            .withValue("cycleCountId", cycleCountId)
            .withValue("expectedQuantity", expectedQty)
            .withValue("isBlindCount", false)
            .withValue("recountRequired", false)
            .withValue("notes", "Count task for cycle count " + cycleCountId)))
         .getRecords().get(0);

         Integer taskId = taskRecord.getValueInteger("id");
         taskCount++;

         //////////////////////////////////////////////////////////////////
         // Link the task back to the cycle count line                   //
         //////////////////////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", lineId)
            .withValue("taskId", taskId)));
      }

      LOG.info("Cycle count created",
         logPair("cycleCountId", cycleCountId),
         logPair("lineCount", lineCount),
         logPair("taskCount", taskCount));

      output.addValue("resultMessage", "Cycle count created successfully.");
      output.addValue("cycleCountId", cycleCountId);
      output.addValue("lineCount", lineCount);
      output.addValue("taskCount", taskCount);
   }
}
