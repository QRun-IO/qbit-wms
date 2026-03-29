/*******************************************************************************
 ** Backend step for LowStockAlertCheck.  Finds items where total on-hand
 ** inventory is at or below the item's reorder point.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class LowStockAlertCheckStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(LowStockAlertCheckStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Query all items with a reorder point set                            //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput itemQuery = new QueryAction().execute(new QueryInput(WmsItem.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("reorderPoint", QCriteriaOperator.IS_NOT_BLANK))
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))));

      /////////////////////////////////////////////////////////////////////////
      // Query all inventory records                                         //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput inventoryQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("quantityOnHand", QCriteriaOperator.GREATER_THAN, 0))));

      /////////////////////////////////////////////////////////////////////////
      // Aggregate on-hand by itemId                                         //
      /////////////////////////////////////////////////////////////////////////
      Map<Integer, BigDecimal> onHandByItem = new LinkedHashMap<>();
      for(QRecord inv : inventoryQuery.getRecords())
      {
         Integer itemId = inv.getValueInteger("itemId");
         BigDecimal qoh = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityOnHand"));
         if(itemId != null && qoh != null)
         {
            onHandByItem.merge(itemId, qoh, BigDecimal::add);
         }
      }

      int alertCount = 0;

      for(QRecord item : itemQuery.getRecords())
      {
         Integer itemId = item.getValueInteger("id");
         Integer reorderPoint = item.getValueInteger("reorderPoint");

         if(reorderPoint == null)
         {
            continue;
         }

         BigDecimal totalOnHand = onHandByItem.getOrDefault(itemId, BigDecimal.ZERO);

         if(totalOnHand.compareTo(new BigDecimal(reorderPoint)) <= 0)
         {
            alertCount++;
            LOG.warn("Low stock alert",
               logPair("itemId", itemId),
               logPair("sku", item.getValueString("sku")),
               logPair("totalOnHand", totalOnHand),
               logPair("reorderPoint", reorderPoint));
         }
      }

      output.addValue("resultMessage", "Low stock check complete. " + alertCount + " items at or below reorder point.");
      output.addValue("alertCount", alertCount);
   }
}
