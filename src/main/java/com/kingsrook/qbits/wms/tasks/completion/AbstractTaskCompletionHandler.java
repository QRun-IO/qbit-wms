/*******************************************************************************
 ** Base class for all task-type-specific completion handlers.  Provides shared
 ** helper methods that enforce the perpetual inventory principle: every quantity
 ** change MUST first insert a wms_inventory_transaction, then update wms_inventory.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public abstract class AbstractTaskCompletionHandler
{
   private static final QLogger LOG = QLogger.getLogger(AbstractTaskCompletionHandler.class);



   /*******************************************************************************
    ** Subclasses implement this to perform type-specific completion logic.
    *******************************************************************************/
   public abstract void handle(QRecord task) throws QException;



   /*******************************************************************************
    ** Create an inventory transaction record.  This MUST be called before any
    ** update to wms_inventory quantities (perpetual inventory principle).
    *******************************************************************************/
   protected QRecord createInventoryTransaction(Integer warehouseId, Integer clientId, Integer itemId,
      TransactionType transactionType, Integer fromLocationId, Integer toLocationId,
      BigDecimal quantity, String lotNumber, String serialNumber, Integer taskId,
      String reasonCode, String performedBy, String notes) throws QException
   {
      LOG.info("Creating inventory transaction",
         logPair("transactionType", transactionType),
         logPair("itemId", itemId),
         logPair("quantity", quantity),
         logPair("taskId", taskId));

      QRecord transactionRecord = new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("transactionTypeId", transactionType.getPossibleValueId())
         .withValue("fromLocationId", fromLocationId)
         .withValue("toLocationId", toLocationId)
         .withValue("quantity", quantity)
         .withValue("lotNumber", lotNumber)
         .withValue("serialNumber", serialNumber)
         .withValue("taskId", taskId)
         .withValue("reasonCode", reasonCode)
         .withValue("performedBy", performedBy)
         .withValue("performedDate", Instant.now())
         .withValue("notes", notes);

      QRecord inserted = new InsertAction().execute(
         new InsertInput(WmsInventoryTransaction.TABLE_NAME).withRecord(transactionRecord)
      ).getRecords().get(0);

      return (inserted);
   }



   /*******************************************************************************
    ** Update inventory quantity at a specific location.  Follows the perpetual
    ** inventory rule: the caller MUST have already inserted a transaction before
    ** calling this method.
    **
    ** @param delta positive to add, negative to deduct
    *******************************************************************************/
   protected void updateInventoryQuantity(Integer warehouseId, Integer clientId, Integer itemId,
      Integer locationId, BigDecimal delta, String lotNumber, String serialNumber) throws QException
   {
      LOG.info("Updating inventory quantity",
         logPair("itemId", itemId),
         logPair("locationId", locationId),
         logPair("delta", delta));

      /////////////////////////////////////////////////////////////////////////
      // Query for existing inventory record at this item+location           //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
         .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, locationId))
         .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId));

      if(lotNumber != null)
      {
         filter.withCriteria(new QFilterCriteria("lotNumber", QCriteriaOperator.EQUALS, lotNumber));
      }
      if(serialNumber != null)
      {
         filter.withCriteria(new QFilterCriteria("serialNumber", QCriteriaOperator.EQUALS, serialNumber));
      }

      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME).withFilter(filter));
      List<QRecord> records = queryOutput.getRecords();

      if(!records.isEmpty())
      {
         ///////////////////////////////////////////////////////////////////
         // Update existing record                                        //
         ///////////////////////////////////////////////////////////////////
         QRecord existing = records.get(0);
         BigDecimal currentQoh = existing.getValueBigDecimal("quantityOnHand");
         if(currentQoh == null)
         {
            currentQoh = BigDecimal.ZERO;
         }

         BigDecimal newQoh = currentQoh.add(delta);
         BigDecimal allocated = existing.getValueBigDecimal("quantityAllocated");
         BigDecimal onHold = existing.getValueBigDecimal("quantityOnHold");
         if(allocated == null)
         {
            allocated = BigDecimal.ZERO;
         }
         if(onHold == null)
         {
            onHold = BigDecimal.ZERO;
         }

         BigDecimal newAvailable = newQoh.subtract(allocated).subtract(onHold);

         new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", existing.getValueInteger("id"))
            .withValue("quantityOnHand", newQoh)
            .withValue("quantityAvailable", newAvailable)));
      }
      else
      {
         ///////////////////////////////////////////////////////////////////
         // Create new inventory record at this location                  //
         ///////////////////////////////////////////////////////////////////
         BigDecimal available = delta.compareTo(BigDecimal.ZERO) > 0 ? delta : BigDecimal.ZERO;

         new InsertAction().execute(new InsertInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
            .withValue("warehouseId", warehouseId)
            .withValue("clientId", clientId)
            .withValue("itemId", itemId)
            .withValue("locationId", locationId)
            .withValue("lotNumber", lotNumber)
            .withValue("serialNumber", serialNumber)
            .withValue("quantityOnHand", delta)
            .withValue("quantityAllocated", BigDecimal.ZERO)
            .withValue("quantityAvailable", available)
            .withValue("quantityOnHold", BigDecimal.ZERO)
            .withValue("inventoryStatusId", InventoryStatus.AVAILABLE.getPossibleValueId())));
      }
   }
}
