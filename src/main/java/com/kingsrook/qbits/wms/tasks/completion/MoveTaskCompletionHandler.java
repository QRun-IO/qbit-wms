/*******************************************************************************
 ** Completion handler for MOVE tasks.  Creates a MOVE inventory transaction,
 ** deducts quantity from the source location, and adds it to the destination
 ** location (creating the inventory record if it does not already exist).
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class MoveTaskCompletionHandler extends AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(MoveTaskCompletionHandler.class);



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
      Integer sourceLocationId = task.getValueInteger("sourceLocationId");
      Integer destinationLocationId = task.getValueInteger("destinationLocationId");
      BigDecimal quantityCompleted = task.getValueBigDecimal("quantityCompleted");
      String lotNumber = task.getValueString("lotNumber");
      String serialNumber = task.getValueString("serialNumber");
      String completedBy = task.getValueString("completedBy");

      LOG.info("Handling MOVE task completion",
         logPair("taskId", taskId),
         logPair("itemId", itemId),
         logPair("sourceLocationId", sourceLocationId),
         logPair("destinationLocationId", destinationLocationId),
         logPair("quantityCompleted", quantityCompleted));

      if(quantityCompleted == null || quantityCompleted.compareTo(BigDecimal.ZERO) <= 0)
      {
         LOG.warn("MOVE task has no quantity completed, skipping inventory changes", logPair("taskId", taskId));
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create the inventory transaction (MUST come first)          //
      /////////////////////////////////////////////////////////////////////////
      createInventoryTransaction(
         warehouseId, clientId, itemId,
         TransactionType.MOVE,
         sourceLocationId, destinationLocationId,
         quantityCompleted,
         lotNumber, serialNumber,
         taskId,
         null,
         completedBy,
         "Move task " + taskId + " completed"
      );

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Deduct from source location                                 //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, itemId, sourceLocationId,
         quantityCompleted.negate(), lotNumber, serialNumber);

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Add to destination location                                 //
      /////////////////////////////////////////////////////////////////////////
      updateInventoryQuantity(warehouseId, clientId, itemId, destinationLocationId,
         quantityCompleted, lotNumber, serialNumber);
   }
}
