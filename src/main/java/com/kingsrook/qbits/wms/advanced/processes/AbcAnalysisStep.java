/*******************************************************************************
 ** Backend step for AbcAnalysis.  Pulls order line history, calculates total
 ** quantity ordered per item, sorts by velocity descending, assigns A/B/C
 ** classification (top 20% = A, next 30% = B, bottom 50% = C), and updates
 ** wmsItem.velocityClassId.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.VelocityClass;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class AbcAnalysisStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(AbcAnalysisStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Query all order lines to calculate velocity per item                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput orderLineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()));

      Map<Integer, BigDecimal> itemVelocity = new LinkedHashMap<>();
      for(QRecord line : orderLineQuery.getRecords())
      {
         Integer itemId = line.getValueInteger("itemId");
         BigDecimal qtyOrdered = ValueUtils.getValueAsBigDecimal(line.getValue("quantityOrdered"));
         if(itemId != null && qtyOrdered != null)
         {
            itemVelocity.merge(itemId, qtyOrdered, BigDecimal::add);
         }
      }

      if(itemVelocity.isEmpty())
      {
         output.addValue("resultMessage", "No order line history found for analysis.");
         output.addValue("itemsClassified", 0);
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Sort items by velocity descending                                   //
      /////////////////////////////////////////////////////////////////////////
      List<Map.Entry<Integer, BigDecimal>> sorted = new ArrayList<>(itemVelocity.entrySet());
      sorted.sort(Comparator.comparing(Map.Entry<Integer, BigDecimal>::getValue).reversed());

      int totalItems = sorted.size();
      int aThreshold = Math.max(1, (int) Math.ceil(totalItems * 0.2));
      int bThreshold = Math.max(aThreshold + 1, (int) Math.ceil(totalItems * 0.5));

      int itemsClassified = 0;

      for(int i = 0; i < sorted.size(); i++)
      {
         Integer itemId = sorted.get(i).getKey();
         VelocityClass velocityClass;

         if(i < aThreshold)
         {
            velocityClass = VelocityClass.A;
         }
         else if(i < bThreshold)
         {
            velocityClass = VelocityClass.B;
         }
         else
         {
            velocityClass = VelocityClass.C;
         }

         new UpdateAction().execute(new UpdateInput(WmsItem.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", itemId)
            .withValue("velocityClassId", velocityClass.getPossibleValueId())));

         itemsClassified++;
      }

      LOG.info("ABC analysis complete", logPair("itemsClassified", itemsClassified));

      output.addValue("resultMessage", "ABC analysis complete. " + itemsClassified + " items classified.");
      output.addValue("itemsClassified", itemsClassified);
   }
}
