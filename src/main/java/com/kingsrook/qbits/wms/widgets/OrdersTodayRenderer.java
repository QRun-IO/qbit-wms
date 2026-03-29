/*******************************************************************************
 ** Renderer for the Orders Today widget.
 **
 ** Queries wms_order for orders created/picked/packed/shipped today and
 ** produces a MultiStatisticsData with those four metrics.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;


public class OrdersTodayRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      Instant startOfDay = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
      Instant endOfDay = startOfDay.plusSeconds(86400);

      int newOrders = countOrders(warehouseId, "createDate", startOfDay, endOfDay, null);
      int pickedOrders = countOrders(warehouseId, "pickedDate", startOfDay, endOfDay, null);
      int packedOrders = countOrders(warehouseId, "packedDate", startOfDay, endOfDay, null);
      int shippedOrders = countOrders(warehouseId, "shippedDate", startOfDay, endOfDay, null);

      List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();
      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("New Orders", newOrders, null));
      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("Picked", pickedOrders, null));
      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("Packed", packedOrders, null));
      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("Shipped", shippedOrders, null));

      MultiStatisticsData.StatisticsGroupData group = new MultiStatisticsData.StatisticsGroupData()
         .withIcon("today")
         .withIconColor("#7B1FA2")
         .withHeader("Today's Orders")
         .withStatisticList(stats);

      MultiStatisticsData data = new MultiStatisticsData()
         .withTitle("Orders Today")
         .withStatisticsGroupData(List.of(group));

      return (new RenderWidgetOutput(data));
   }



   /*******************************************************************************
    ** Count orders matching a date range on the given date field.
    *******************************************************************************/
   private int countOrders(String warehouseId, String dateField, Instant from, Instant to,
      Integer statusId) throws QException
   {
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria(dateField, QCriteriaOperator.GREATER_THAN_OR_EQUALS, from))
         .withCriteria(new QFilterCriteria(dateField, QCriteriaOperator.LESS_THAN, to));

      if(StringUtils.hasContent(warehouseId))
      {
         filter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }
      if(statusId != null)
      {
         filter.withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, statusId));
      }

      CountOutput countOutput = new CountAction().execute(new CountInput(WmsOrder.TABLE_NAME).withFilter(filter));
      return (countOutput.getCount());
   }
}
