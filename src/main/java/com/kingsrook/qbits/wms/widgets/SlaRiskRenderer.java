/*******************************************************************************
 ** Renderer for the SLA Risk widget.
 **
 ** Queries wms_order for non-shipped orders with a ship-by date within the
 ** next 24 hours and produces statistics showing counts by status.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;


public class SlaRiskRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      Instant now = Instant.now();
      Instant deadline = now.plus(24, ChronoUnit.HOURS);

      /////////////////////////////////////////////////////////////////////////
      // Count at-risk orders by status                                      //
      /////////////////////////////////////////////////////////////////////////
      OrderStatus[] activeStatuses = {
         OrderStatus.PENDING,
         OrderStatus.ALLOCATED,
         OrderStatus.PICK_RELEASED,
         OrderStatus.PICKING,
         OrderStatus.PICKED,
         OrderStatus.PACKING,
         OrderStatus.PACKED
      };

      List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();
      int totalAtRisk = 0;

      for(OrderStatus status : activeStatuses)
      {
         QQueryFilter filter = new QQueryFilter()
            .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, status.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("shipByDate", QCriteriaOperator.LESS_THAN_OR_EQUALS, deadline))
            .withCriteria(new QFilterCriteria("shipByDate", QCriteriaOperator.IS_NOT_BLANK));

         if(StringUtils.hasContent(warehouseId))
         {
            filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
         }

         CountOutput countOutput = new CountAction().execute(new CountInput(WmsOrder.TABLE_NAME).withFilter(filter));
         int count = countOutput.getCount();
         totalAtRisk += count;

         if(count > 0)
         {
            stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic(status.getLabel(), count, null));
         }
      }

      if(stats.isEmpty())
      {
         stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("No at-risk orders", 0, null));
      }

      MultiStatisticsData.StatisticsGroupData group = new MultiStatisticsData.StatisticsGroupData()
         .withIcon("schedule")
         .withIconColor("#E65100")
         .withHeader("At Risk: " + totalAtRisk + " orders due within 24h")
         .withStatisticList(stats);

      MultiStatisticsData data = new MultiStatisticsData()
         .withTitle("SLA Risk")
         .withStatisticsGroupData(List.of(group));

      return (new RenderWidgetOutput(data));
   }
}
