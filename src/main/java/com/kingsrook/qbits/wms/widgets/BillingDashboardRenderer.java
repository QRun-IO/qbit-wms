/*******************************************************************************
 ** Renderer for the Billing Dashboard widget.
 **
 ** Queries billing activities and invoices to produce a MultiStatisticsData
 ** structure showing total revenue, unbilled activity counts, and invoice
 ** counts by status (Draft/Sent/Paid/Overdue).
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.MultiStatisticsData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.billing.model.WmsInvoice;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;


public class BillingDashboardRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      List<MultiStatisticsData.StatisticsGroupData> groups = new ArrayList<>();

      ///////////////////////////////////////////////////////////////////////////
      // Group 1: Revenue summary from invoices                                //
      ///////////////////////////////////////////////////////////////////////////
      groups.add(buildRevenueSummary());

      ///////////////////////////////////////////////////////////////////////////
      // Group 2: Invoice counts by status                                     //
      ///////////////////////////////////////////////////////////////////////////
      groups.add(buildInvoicesByStatus());

      MultiStatisticsData data = new MultiStatisticsData()
         .withTitle("Billing Dashboard")
         .withStatisticsGroupData(groups);

      return (new RenderWidgetOutput(data));
   }



   /*******************************************************************************
    ** Build revenue summary group: total revenue and unbilled activities.
    *******************************************************************************/
   private MultiStatisticsData.StatisticsGroupData buildRevenueSummary() throws QException
   {
      List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();

      ///////////////////////////////////////////////////////////
      // Sum total from all paid invoices                      //
      ///////////////////////////////////////////////////////////
      Aggregate sumTotal = new Aggregate("total", AggregateOperator.SUM);
      AggregateInput paidAggInput = new AggregateInput(WmsInvoice.TABLE_NAME);
      paidAggInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, InvoiceStatus.PAID.getPossibleValueId())));
      paidAggInput.withAggregates(List.of(sumTotal));

      AggregateOutput paidAggOutput = new AggregateAction().execute(paidAggInput);
      BigDecimal totalPaidRevenue = BigDecimal.ZERO;
      if(!paidAggOutput.getResults().isEmpty())
      {
         BigDecimal val = ValueUtils.getValueAsBigDecimal(paidAggOutput.getResults().get(0).getAggregateValue(sumTotal));
         if(val != null)
         {
            totalPaidRevenue = val;
         }
      }

      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("Total Paid Revenue", totalPaidRevenue.intValue(), null));

      ///////////////////////////////////////////////////////////
      // Count unbilled activities                             //
      ///////////////////////////////////////////////////////////
      QueryOutput unbilledQuery = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("isBilled", QCriteriaOperator.EQUALS, false))));

      stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic("Unbilled Activities", unbilledQuery.getRecords().size(), null));

      return new MultiStatisticsData.StatisticsGroupData()
         .withIcon("payments")
         .withIconColor("#2E7D32")
         .withHeader("Revenue")
         .withStatisticList(stats);
   }



   /*******************************************************************************
    ** Build invoices-by-status group.
    *******************************************************************************/
   private MultiStatisticsData.StatisticsGroupData buildInvoicesByStatus() throws QException
   {
      List<MultiStatisticsData.StatisticsGroupData.Statistic> stats = new ArrayList<>();

      Aggregate countAggregate = new Aggregate("id", AggregateOperator.COUNT);
      GroupBy statusGroup = new GroupBy(QFieldType.INTEGER, "statusId");

      AggregateInput aggregateInput = new AggregateInput(WmsInvoice.TABLE_NAME);
      aggregateInput.withAggregates(List.of(countAggregate));
      aggregateInput.withGroupBys(List.of(statusGroup));

      AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

      Map<Integer, Integer> statusCounts = new LinkedHashMap<>();
      for(AggregateResult result : aggregateOutput.getResults())
      {
         Integer statusId = ValueUtils.getValueAsInteger(result.getGroupByValue(statusGroup));
         Integer count = ValueUtils.getValueAsInteger(result.getAggregateValue(countAggregate));
         statusCounts.put(statusId, count);
      }

      InvoiceStatus[] displayStatuses = { InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.PAID, InvoiceStatus.OVERDUE };
      for(InvoiceStatus status : displayStatuses)
      {
         Integer count = statusCounts.getOrDefault(status.getPossibleValueId(), 0);
         stats.add(new MultiStatisticsData.StatisticsGroupData.Statistic(status.getLabel(), count, null));
      }

      return new MultiStatisticsData.StatisticsGroupData()
         .withIcon("receipt_long")
         .withIconColor("#1565C0")
         .withHeader("Invoices")
         .withStatisticList(stats);
   }
}
