/*******************************************************************************
 ** Renderer for the Fulfillment Pipeline widget.
 **
 ** Queries wms_order and groups counts by status, producing a
 ** MultiStatisticsData showing order counts at each fulfillment stage.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;


public class FulfillmentPipelineRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      QQueryFilter filter = new QQueryFilter();
      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      Aggregate countAggregate = new Aggregate("id", AggregateOperator.COUNT);
      GroupBy statusGroup = new GroupBy(QFieldType.INTEGER, "statusId");

      AggregateInput aggregateInput = new AggregateInput(WmsOrder.TABLE_NAME);
      aggregateInput.setFilter(filter);
      aggregateInput.withAggregates(List.of(countAggregate));
      aggregateInput.withGroupBys(List.of(statusGroup));

      AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

      /////////////////////////////////////////////////////////////////////////
      // Collect counts by status                                            //
      /////////////////////////////////////////////////////////////////////////
      Map<Integer, Integer> statusCounts = new LinkedHashMap<>();
      for(AggregateResult result : aggregateOutput.getResults())
      {
         Integer stId = ValueUtils.getValueAsInteger(result.getGroupByValue(statusGroup));
         Integer count = ValueUtils.getValueAsInteger(result.getAggregateValue(countAggregate));
         statusCounts.put(stId, count);
      }

      /////////////////////////////////////////////////////////////////////////
      // Build statistics in pipeline order                                  //
      /////////////////////////////////////////////////////////////////////////
      OrderStatus[] pipelineOrder = {
         OrderStatus.PENDING,
         OrderStatus.ALLOCATED,
         OrderStatus.PICK_RELEASED,
         OrderStatus.PICKING,
         OrderStatus.PICKED,
         OrderStatus.PACKING,
         OrderStatus.PACKED,
         OrderStatus.SHIPPED
      };

      List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();
      for(OrderStatus status : pipelineOrder)
      {
         Integer count = statusCounts.getOrDefault(status.getPossibleValueId(), 0);
         stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic(status.getLabel(), count, null));
      }

      MultiStatisticsData.StatisticsGroupData group = new MultiStatisticsData.StatisticsGroupData()
         .withIcon("timeline")
         .withIconColor("#2E7D32")
         .withHeader("Fulfillment Pipeline")
         .withStatisticList(stats);

      MultiStatisticsData data = new MultiStatisticsData()
         .withTitle("Fulfillment Pipeline")
         .withStatisticsGroupData(List.of(group));

      return (new RenderWidgetOutput(data));
   }
}
