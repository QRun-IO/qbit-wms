/*******************************************************************************
 ** Renderer for the Inventory Accuracy widget.
 **
 ** Queries completed COUNT-type tasks and calculates the percentage where
 ** countedQuantity matches expectedQuantity. Displays the result as a
 ** StatisticsData widget.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.StatisticsData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class InventoryAccuracyRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      ///////////////////////////////////////////////////////////////
      // filter: completed COUNT tasks with non-null expected qty  //
      ///////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.COUNT.getId()))
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.COMPLETED.getId()))
         .withCriteria(new QFilterCriteria("expectedQuantity", QCriteriaOperator.IS_NOT_BLANK));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      QueryInput queryInput = new QueryInput(WmsTask.TABLE_NAME);
      queryInput.setFilter(filter);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      ///////////////////////////////////////////////
      // calculate accuracy: matches / total       //
      ///////////////////////////////////////////////
      Integer totalCounts   = 0;
      Integer accurateCounts = 0;

      for(QRecord record : queryOutput.getRecords())
      {
         BigDecimal expected = record.getValueBigDecimal("expectedQuantity");
         BigDecimal counted  = record.getValueBigDecimal("countedQuantity");

         if(expected == null)
         {
            continue;
         }

         totalCounts++;
         if(counted != null && counted.compareTo(expected) == 0)
         {
            accurateCounts++;
         }
      }

      String accuracyStr;
      String context;
      if(totalCounts > 0)
      {
         BigDecimal pct = new BigDecimal(accurateCounts)
            .multiply(new BigDecimal(100))
            .divide(new BigDecimal(totalCounts), 1, RoundingMode.HALF_UP);
         accuracyStr = pct.toPlainString() + "%";
         context = accurateCounts + " accurate of " + totalCounts + " counts";
      }
      else
      {
         accuracyStr = "N/A";
         context = "No completed cycle counts";
      }

      StatisticsData data = new StatisticsData(accuracyStr, null, "Inventory Accuracy");
      data.withCountContext(context);

      return (new RenderWidgetOutput(data));
   }
}
