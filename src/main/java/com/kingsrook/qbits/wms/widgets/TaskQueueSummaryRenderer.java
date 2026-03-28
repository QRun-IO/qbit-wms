/*******************************************************************************
 ** Renderer for the Task Queue Summary widget.
 **
 ** Queries wms_task and groups counts by task type and status, producing a
 ** MultiStatisticsData structure with one statistics group per task type.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.io.Serializable;
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
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class TaskQueueSummaryRenderer extends AbstractWidgetRenderer
{
   private static final List<TaskStatus> ACTIVE_STATUSES = List.of(
      TaskStatus.PENDING,
      TaskStatus.ASSIGNED,
      TaskStatus.IN_PROGRESS,
      TaskStatus.PAUSED,
      TaskStatus.ON_HOLD
   );



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      ///////////////////////////////////////////////////////////
      // build filter for only active (non-terminal) statuses  //
      ///////////////////////////////////////////////////////////
      List<Integer> statusIds = new ArrayList<>();
      for(TaskStatus status : ACTIVE_STATUSES)
      {
         statusIds.add(status.getId());
      }

      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.IN, new ArrayList<>(statusIds)));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      //////////////////////////////////////////////////////////
      // aggregate: COUNT(id) GROUP BY taskTypeId, taskStatusId //
      //////////////////////////////////////////////////////////
      Aggregate countAggregate = new Aggregate("id", AggregateOperator.COUNT);
      GroupBy   taskTypeGroup  = new GroupBy(QFieldType.INTEGER, "taskTypeId");
      GroupBy   statusGroup    = new GroupBy(QFieldType.INTEGER, "taskStatusId");

      AggregateInput aggregateInput = new AggregateInput(WmsTask.TABLE_NAME);
      aggregateInput.setFilter(filter);
      aggregateInput.withAggregates(List.of(countAggregate));
      aggregateInput.withGroupBys(List.of(taskTypeGroup, statusGroup));

      AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

      /////////////////////////////////////////////////////////////////
      // organize results into a map: taskTypeId -> statusId -> count //
      /////////////////////////////////////////////////////////////////
      Map<Integer, Map<Integer, Integer>> typeStatusCounts = new LinkedHashMap<>();
      for(AggregateResult result : aggregateOutput.getResults())
      {
         Integer typeId  = ValueUtils.getValueAsInteger(result.getGroupByValue(taskTypeGroup));
         Integer stId    = ValueUtils.getValueAsInteger(result.getGroupByValue(statusGroup));
         Integer count   = ValueUtils.getValueAsInteger(result.getAggregateValue(countAggregate));
         typeStatusCounts.computeIfAbsent(typeId, k -> new LinkedHashMap<>()).put(stId, count);
      }

      ////////////////////////////////////////////////////////
      // build a StatisticsGroupData per task type           //
      ////////////////////////////////////////////////////////
      List<MultiStatisticsData.StatisticsGroupData> groups = new ArrayList<>();
      for(TaskType taskType : TaskType.values())
      {
         Map<Integer, Integer> statusCounts = typeStatusCounts.get(taskType.getId());
         if(statusCounts == null || statusCounts.isEmpty())
         {
            continue;
         }

         List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();
         for(TaskStatus status : ACTIVE_STATUSES)
         {
            Integer count = statusCounts.getOrDefault(status.getId(), 0);
            stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic(status.getLabel(), count, null));
         }

         MultiStatisticsData.StatisticsGroupData group = new MultiStatisticsData.StatisticsGroupData()
            .withIcon("assignment")
            .withIconColor("#1565C0")
            .withHeader(taskType.getLabel())
            .withStatisticList(stats);

         groups.add(group);
      }

      MultiStatisticsData data = new MultiStatisticsData()
         .withTitle("Task Queue Summary")
         .withStatisticsGroupData(groups);

      return (new RenderWidgetOutput(data));
   }
}
