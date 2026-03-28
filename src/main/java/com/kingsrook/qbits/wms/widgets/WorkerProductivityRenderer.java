/*******************************************************************************
 ** Renderer for the Worker Productivity widget.
 **
 ** Queries wms_task where completedDate is today and groups by completedBy to
 ** produce a table of worker name, tasks completed, units processed, and an
 ** estimated lines-per-hour rate.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class WorkerProductivityRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      ////////////////////////////////////////////////////////
      // determine start-of-day in the system's time zone   //
      ////////////////////////////////////////////////////////
      ZoneId zoneId = ZoneId.systemDefault();
      ZonedDateTime startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId);
      Instant todayStart = startOfDay.toInstant();

      //////////////////////////////////////////////////////////
      // filter: completed tasks where completedDate >= today  //
      //////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.COMPLETED.getId()))
         .withCriteria(new QFilterCriteria("completedDate", QCriteriaOperator.GREATER_THAN_OR_EQUALS, todayStart));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      ///////////////////////////////////////////////////////////////////
      // aggregate: COUNT(id), SUM(quantityCompleted) GROUP BY completedBy //
      ///////////////////////////////////////////////////////////////////
      Aggregate countAgg = new Aggregate("id", AggregateOperator.COUNT);
      Aggregate unitsAgg = new Aggregate("quantityCompleted", AggregateOperator.SUM);
      GroupBy   workerGroup = new GroupBy(QFieldType.STRING, "completedBy");

      AggregateInput aggInput = new AggregateInput(WmsTask.TABLE_NAME);
      aggInput.setFilter(filter);
      aggInput.withAggregates(List.of(countAgg, unitsAgg));
      aggInput.withGroupBys(List.of(workerGroup));

      AggregateOutput aggOutput = new AggregateAction().execute(aggInput);

      //////////////////////////////////
      // calculate hours elapsed today //
      //////////////////////////////////
      Instant now = Instant.now();
      Long elapsedSeconds = now.getEpochSecond() - todayStart.getEpochSecond();
      BigDecimal hoursElapsed = new BigDecimal(elapsedSeconds).divide(new BigDecimal(3600), 4, RoundingMode.HALF_UP);
      if(hoursElapsed.compareTo(BigDecimal.ZERO) <= 0)
      {
         hoursElapsed = BigDecimal.ONE;
      }

      ///////////////////////////
      // build table columns   //
      ///////////////////////////
      List<TableData.Column> columns = List.of(
         new TableData.Column("html", "Worker", "worker", "2fr", null),
         new TableData.Column("html", "Tasks Completed", "tasksCompleted", "1fr", "right"),
         new TableData.Column("html", "Units Processed", "unitsProcessed", "1fr", "right"),
         new TableData.Column("html", "Lines/Hour", "linesPerHour", "1fr", "right")
      );

      //////////////////////////
      // build table rows     //
      //////////////////////////
      List<Map<String, Object>> rows = new ArrayList<>();
      if(aggOutput.getResults() != null)
      {
         for(AggregateResult result : aggOutput.getResults())
         {
            Serializable workerName = result.getGroupByValue(workerGroup);
            Integer tasksCompleted = ValueUtils.getValueAsInteger(result.getAggregateValue(countAgg));
            BigDecimal unitsProcessed = ValueUtils.getValueAsBigDecimal(result.getAggregateValue(unitsAgg));

            if(tasksCompleted == null)
            {
               tasksCompleted = 0;
            }
            if(unitsProcessed == null)
            {
               unitsProcessed = BigDecimal.ZERO;
            }

            BigDecimal linesPerHour = new BigDecimal(tasksCompleted)
               .divide(hoursElapsed, 1, RoundingMode.HALF_UP);

            rows.add(MapBuilder.of(
               "worker", workerName != null ? String.valueOf(workerName) : "Unknown",
               "tasksCompleted", String.valueOf(tasksCompleted),
               "unitsProcessed", unitsProcessed.toPlainString(),
               "linesPerHour", linesPerHour.toPlainString()
            ));
         }
      }

      TableData tableData = new TableData(null, columns, rows)
         .withRowsPerPage(50)
         .withHidePaginationDropdown(true);

      return (new RenderWidgetOutput(tableData));
   }
}
