/*******************************************************************************
 ** Renderer for the Active Workers widget.
 **
 ** Queries wms_task where status is IN_PROGRESS and produces a table showing
 ** worker name, current task type, task ID, item, location, and duration since
 ** the task's startedDate.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class ActiveWorkersRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      /////////////////////////////////////////////////////////
      // filter: tasks with status IN_PROGRESS               //
      /////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.EQUALS, TaskStatus.IN_PROGRESS.getId()));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      QueryInput queryInput = new QueryInput(WmsTask.TABLE_NAME);
      queryInput.setFilter(filter);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      ///////////////////////////
      // build table columns   //
      ///////////////////////////
      List<TableData.Column> columns = List.of(
         new TableData.Column("html", "Worker", "worker", "2fr", null),
         new TableData.Column("html", "Task Type", "taskType", "1fr", null),
         new TableData.Column("html", "Task ID", "taskId", "1fr", "right"),
         new TableData.Column("html", "Item", "item", "1fr", null),
         new TableData.Column("html", "Location", "location", "1fr", null),
         new TableData.Column("html", "Duration", "duration", "1fr", "right")
      );

      //////////////////////////
      // build table rows     //
      //////////////////////////
      Instant now = Instant.now();
      List<Map<String, Object>> rows = new ArrayList<>();
      for(QRecord record : queryOutput.getRecords())
      {
         String worker = record.getValueString("assignedTo");
         if(!StringUtils.hasContent(worker))
         {
            worker = "Unassigned";
         }

         Integer taskTypeId = record.getValueInteger("taskTypeId");
         TaskType taskType = TaskType.getById(taskTypeId);
         String taskTypeLabel = taskType != null ? taskType.getLabel() : String.valueOf(taskTypeId);

         Integer taskId = record.getValueInteger("id");
         String itemDisplay = record.getValueString("itemId") != null ? String.valueOf(record.getValueInteger("itemId")) : "-";
         String locationDisplay = record.getValueString("sourceLocationId") != null ? String.valueOf(record.getValueInteger("sourceLocationId")) : "-";

         /////////////////////////////////////
         // compute duration since started  //
         /////////////////////////////////////
         Instant startedDate = record.getValueInstant("startedDate");
         String durationStr = "-";
         if(startedDate != null)
         {
            Duration elapsed = Duration.between(startedDate, now);
            Long totalMinutes = elapsed.toMinutes();
            Long hours = totalMinutes / 60;
            Long minutes = totalMinutes % 60;
            durationStr = String.format("%dh %02dm", hours, minutes);
         }

         rows.add(MapBuilder.of(
            "worker", worker,
            "taskType", taskTypeLabel,
            "taskId", String.valueOf(taskId),
            "item", itemDisplay,
            "location", locationDisplay,
            "duration", durationStr
         ));
      }

      TableData tableData = new TableData(null, columns, rows)
         .withRowsPerPage(50)
         .withHidePaginationDropdown(true);

      return (new RenderWidgetOutput(tableData));
   }
}
