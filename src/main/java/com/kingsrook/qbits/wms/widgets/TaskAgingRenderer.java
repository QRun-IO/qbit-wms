/*******************************************************************************
 ** Renderer for the Task Aging widget.
 **
 ** Queries wms_task for PENDING and ASSIGNED tasks and buckets them by age
 ** from createDate: 0-15 min, 15-30 min, 30-60 min, 1-4 h, and 4h+. Produces
 ** a bar chart with one bar per bucket.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.ChartData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class TaskAgingRenderer extends AbstractWidgetRenderer
{
   private static final String COLOR_0_15   = "#4CAF50";
   private static final String COLOR_15_30  = "#8BC34A";
   private static final String COLOR_30_60  = "#FFC107";
   private static final String COLOR_1_4H   = "#FF9800";
   private static final String COLOR_4H_PLUS = "#F44336";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      ///////////////////////////////////////////////////////////////////
      // filter: only PENDING and ASSIGNED tasks                       //
      ///////////////////////////////////////////////////////////////////
      List<Integer> statusIds = List.of(TaskStatus.PENDING.getId(), TaskStatus.ASSIGNED.getId());

      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("taskStatusId", QCriteriaOperator.IN, new ArrayList<>(statusIds)));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      QueryInput queryInput = new QueryInput(WmsTask.TABLE_NAME);
      queryInput.setFilter(filter);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      ////////////////////////////////////
      // bucket tasks by age            //
      ////////////////////////////////////
      Instant now = Instant.now();
      Integer bucket0to15  = 0;
      Integer bucket15to30 = 0;
      Integer bucket30to60 = 0;
      Integer bucket1to4h  = 0;
      Integer bucket4hPlus = 0;

      for(QRecord record : queryOutput.getRecords())
      {
         Instant createDate = record.getValueInstant("createDate");
         if(createDate == null)
         {
            bucket4hPlus++;
            continue;
         }

         Long minutesOld = Duration.between(createDate, now).toMinutes();
         if(minutesOld < 15)
         {
            bucket0to15++;
         }
         else if(minutesOld < 30)
         {
            bucket15to30++;
         }
         else if(minutesOld < 60)
         {
            bucket30to60++;
         }
         else if(minutesOld < 240)
         {
            bucket1to4h++;
         }
         else
         {
            bucket4hPlus++;
         }
      }

      ///////////////////////////
      // build chart data      //
      ///////////////////////////
      List<String> labels = List.of("0-15m", "15-30m", "30-60m", "1-4h", "4h+");
      List<String> colors = List.of(COLOR_0_15, COLOR_15_30, COLOR_30_60, COLOR_1_4H, COLOR_4H_PLUS);
      List<Number> data   = List.of(bucket0to15, bucket15to30, bucket30to60, bucket1to4h, bucket4hPlus);

      ChartData chartData = new ChartData()
         .withChartData(new ChartData.Data()
            .withLabels(labels)
            .withDatasets(List.of(
               new ChartData.Data.Dataset()
                  .withLabel("Tasks")
                  .withData(data)
                  .withBackgroundColors(colors)
            )));

      chartData.setTitle("Task Aging");

      return (new RenderWidgetOutput(chartData));
   }
}
