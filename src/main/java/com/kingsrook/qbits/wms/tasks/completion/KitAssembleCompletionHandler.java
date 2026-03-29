/*******************************************************************************
 ** Completion handler for KIT_ASSEMBLE tasks.  Deducts component inventory per
 ** the kit BOM, inserts PICK transactions per component and a RECEIVE
 ** transaction for the finished kit, creates inventory for the finished kit,
 ** and optionally creates a billing activity for 3PL clients.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.fulfillment.model.WmsKitBom;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class KitAssembleCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(KitAssembleCompletionHandler.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void handle(QRecord task) throws QException
   {
      Integer taskId = task.getValueInteger("id");
      Integer warehouseId = task.getValueInteger("warehouseId");
      Integer clientId = task.getValueInteger("clientId");
      Integer kitItemId = task.getValueInteger("itemId");
      Integer destinationLocationId = task.getValueInteger("destinationLocationId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");

      LOG.info("Handling KIT_ASSEMBLE task completion",
         logPair("taskId", taskId),
         logPair("kitItemId", kitItemId),
         logPair("quantityCompleted", quantityCompleted));

      if(quantityCompleted == null || quantityCompleted.compareTo(BigDecimal.ZERO) <= 0)
      {
         LOG.warn("KIT_ASSEMBLE task has no quantity completed, skipping", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Look up the kit BOM                                        //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput bomQuery = new QueryAction().execute(new QueryInput(WmsKitBom.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("kitItemId", QCriteriaOperator.EQUALS, kitItemId))));

      List<QRecord> bomLines = bomQuery.getRecords();
      if(bomLines.isEmpty())
      {
         LOG.warn("No BOM found for kit item, skipping component deduction",
            logPair("taskId", taskId), logPair("kitItemId", kitItemId));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Deduct component inventory per BOM line                     //
      /////////////////////////////////////////////////////////////////////////
      for(QRecord bomLine : bomLines)
      {
         Integer componentItemId = bomLine.getValueInteger("componentItemId");
         BigDecimal componentQty = ValueUtils.getValueAsBigDecimal(bomLine.getValue("componentQuantity"));
         if(componentQty == null)
         {
            componentQty = BigDecimal.ONE;
         }

         BigDecimal totalComponentQty = componentQty.multiply(quantityCompleted);

         //////////////////////////////////////////////////////////////////
         // Insert PICK transaction per component (perpetual inventory)  //
         //////////////////////////////////////////////////////////////////
         createInventoryTransaction(
            warehouseId, clientId, componentItemId,
            TransactionType.PICK,
            destinationLocationId, null,
            totalComponentQty,
            null, null,
            taskId,
            null,
            completedBy,
            "Kit assembly component pick for kit item " + kitItemId
         );

         //////////////////////////////////////////////////////////////////
         // Deduct component inventory from location                    //
         //////////////////////////////////////////////////////////////////
         updateInventoryQuantity(warehouseId, clientId, componentItemId, destinationLocationId,
            totalComponentQty.negate(), null, null);
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Insert RECEIVE transaction for the finished kit             //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, kitItemId,
         TransactionType.RECEIVE,
         null, destinationLocationId,
         quantityCompleted,
         lotNumber, serialNumber,
         taskId,
         null,
         completedBy,
         "Kit assembly completed for task " + taskId
      );

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Create inventory for finished kit                           //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, kitItemId, destinationLocationId,
         quantityCompleted, lotNumber, serialNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 5: If clientId, create billing activity                        //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId, quantityCompleted);
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for KITTING_PER_UNIT.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.KITTING_PER_UNIT.getPossibleValueId())
            .withValue("activityDate", Instant.now())
            .withValue("quantity", quantity)
            .withValue("referenceType", "TASK")
            .withValue("referenceId", taskId)
            .withValue("taskId", taskId)
            .withValue("isBilled", false)));
      }
      catch(Exception e)
      {
         LOG.warn("Could not create billing activity (billing tables may not be available yet)",
            logPair("taskId", taskId), logPair("error", e.getMessage()));
      }
   }
}
