/*******************************************************************************
 ** Backend step for KitAssembly.  Loads the BOM for the selected kit item,
 ** validates component availability, and creates KIT_ASSEMBLE tasks.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
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
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsKitBom;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class KitAssemblyStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(KitAssemblyStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer kitItemId = input.getValueInteger("kitItemId");
      Integer quantity = input.getValueInteger("quantity");
      Integer destinationLocationId = input.getValueInteger("destinationLocationId");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      if(kitItemId == null)
      {
         throw new QUserFacingException("Kit Item is required.");
      }

      if(quantity == null || quantity <= 0)
      {
         throw new QUserFacingException("Quantity must be greater than zero.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Verify BOM exists for this kit item                                 //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput bomQuery = new QueryAction().execute(new QueryInput(WmsKitBom.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("kitItemId", QCriteriaOperator.EQUALS, kitItemId))));

      List<QRecord> bomLines = bomQuery.getRecords();
      if(bomLines.isEmpty())
      {
         throw new QUserFacingException("No BOM found for kit item: " + kitItemId);
      }

      LOG.info("Creating kit assembly tasks",
         logPair("kitItemId", kitItemId),
         logPair("quantity", quantity),
         logPair("bomLines", bomLines.size()));

      /////////////////////////////////////////////////////////////////////////
      // Create KIT_ASSEMBLE task                                            //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", kitItemId)
         .withValue("quantityRequested", new BigDecimal(quantity))
         .withValue("destinationLocationId", destinationLocationId)
         .withValue("referenceType", "KIT_BOM")
         .withValue("referenceId", kitItemId)
         .withValue("notes", "Kit assembly for " + quantity + " units of kit item " + kitItemId)));

      output.addValue("resultMessage", "Kit assembly task created for " + quantity + " units with " + bomLines.size() + " BOM components.");
      output.addValue("tasksCreated", 1);
   }
}
