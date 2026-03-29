/*******************************************************************************
 ** Completion handler for RETURN_PUTAWAY tasks.  Creates inventory at the
 ** destination location, inserts a RETURN inventory transaction (perpetual
 ** inventory principle), sets inventory status based on inspection grade
 ** (A_STOCK -> AVAILABLE, B_STOCK -> B_STOCK), updates the return
 ** authorization status, and optionally creates a billing activity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.InspectionGrade;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReturnPutawayCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(ReturnPutawayCompletionHandler.class);



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
      Integer destinationLocationId = task.getValueInteger("destinationLocationId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");
      Integer referenceId = task.getValueInteger("referenceId");
      Integer inspectionGradeId = task.getValueInteger("inspectionGradeId");

      LOG.info("Handling RETURN_PUTAWAY task completion",
         logPair("taskId", taskId),
         logPair("itemId", itemId),
         logPair("destinationLocationId", destinationLocationId),
         logPair("quantityCompleted", quantityCompleted));

      if(quantityCompleted == null || quantityCompleted.compareTo(BigDecimal.ZERO) <= 0)
      {
         LOG.warn("RETURN_PUTAWAY task has no quantity completed, skipping", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Insert RETURN transaction (perpetual inventory -- FIRST)    //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, itemId,
         TransactionType.RETURN,
         null, destinationLocationId,
         quantityCompleted,
         lotNumber, serialNumber,
         taskId,
         null,
         completedBy,
         "Return putaway task " + taskId + " completed"
      );

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Create inventory at destination location                    //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, itemId, destinationLocationId,
         quantityCompleted, lotNumber, serialNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Set inventory status based on inspection grade              //
      /////////////////////////////////////////////////////////////////////////
      if(inspectionGradeId != null && Objects.equals(inspectionGradeId, InspectionGrade.B_STOCK.getPossibleValueId()))
      {
         // For B_STOCK, update the newly created/updated inventory status
         // (The updateInventoryQuantity above creates with AVAILABLE status)
         LOG.info("Setting inventory status to B_STOCK for return putaway", logPair("taskId", taskId));
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Update return authorization status if reference provided    //
      /////////////////////////////////////////////////////////////////////////
      if(referenceId != null)
      {
         try
         {
            QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(referenceId)).getRecord();
            if(ra != null)
            {
               new UpdateAction().execute(new UpdateInput(WmsReturnAuthorization.TABLE_NAME).withRecord(new QRecord()
                  .withValue("id", referenceId)
                  .withValue("statusId", ReturnAuthorizationStatus.CLOSED.getPossibleValueId())
                  .withValue("closedDate", Instant.now())));
            }
         }
         catch(Exception e)
         {
            LOG.warn("Could not update return authorization status",
               logPair("taskId", taskId), logPair("referenceId", referenceId), logPair("error", e.getMessage()));
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 5: If clientId present, create billing activity                //
      /////////////////////////////////////////////////////////////////////////
      if(clientId != null)
      {
         createBillingActivity(warehouseId, clientId, taskId, quantityCompleted);
      }
   }



   /*******************************************************************************
    ** Create a billing activity record for RETURN_PER_UNIT.
    *******************************************************************************/
   private void createBillingActivity(Integer warehouseId, Integer clientId, Integer taskId, BigDecimal quantity) throws QException
   {
      try
      {
         new InsertAction().execute(new InsertInput("wmsBillingActivity").withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("activityTypeId", BillingActivityType.RETURN_PER_UNIT.getPossibleValueId())
            .withValue("activityDate", Instant.now())
            .withValue("quantity", quantity)
            .withValue("referenceType", "TASK")
            .withValue("referenceId", taskId)
            .withValue("taskId", taskId)
            .withValue("isBilled", false)));
      }
      catch(Exception e)
      {
         LOG.error("Failed to create billing activity for RETURN_PUTAWAY task", e,
            logPair("taskId", taskId));
      }
   }
}
