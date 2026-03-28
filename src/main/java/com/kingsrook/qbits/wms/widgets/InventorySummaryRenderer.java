/*******************************************************************************
 ** Renderer for the Inventory Summary widget.
 **
 ** Aggregates wms_inventory data to produce Total SKUs, Total Units on Hand,
 ** Total Locations Used, and a Utilization percentage. Supports warehouse
 ** filtering via the warehouseId dropdown.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.StatisticsData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsLocation;


public class InventorySummaryRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      /////////////////////////////////////
      // base filter for warehouse scope //
      /////////////////////////////////////
      QQueryFilter inventoryFilter = new QQueryFilter();
      if(StringUtils.hasContent(warehouseId))
      {
         inventoryFilter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      ////////////////////////////////////////////
      // count distinct SKUs (itemId) on hand   //
      ////////////////////////////////////////////
      Aggregate distinctSkus = new Aggregate("itemId", AggregateOperator.COUNT_DISTINCT);
      Aggregate totalOnHand  = new Aggregate("quantityOnHand", AggregateOperator.SUM);
      Aggregate distinctLocs = new Aggregate("locationId", AggregateOperator.COUNT_DISTINCT);

      AggregateInput aggInput = new AggregateInput(WmsInventory.TABLE_NAME);
      aggInput.setFilter(inventoryFilter);
      aggInput.withAggregates(List.of(distinctSkus, totalOnHand, distinctLocs));

      AggregateOutput aggOutput = new AggregateAction().execute(aggInput);

      Integer skuCount      = 0;
      BigDecimal unitsOnHand = BigDecimal.ZERO;
      Integer locationsUsed = 0;

      if(aggOutput.getResults() != null && !aggOutput.getResults().isEmpty())
      {
         AggregateResult row = aggOutput.getResults().get(0);
         Serializable skuVal = row.getAggregateValue(distinctSkus);
         Serializable qohVal = row.getAggregateValue(totalOnHand);
         Serializable locVal = row.getAggregateValue(distinctLocs);

         skuCount = skuVal != null ? ValueUtils.getValueAsInteger(skuVal) : 0;
         unitsOnHand = qohVal != null ? ValueUtils.getValueAsBigDecimal(qohVal) : BigDecimal.ZERO;
         locationsUsed = locVal != null ? ValueUtils.getValueAsInteger(locVal) : 0;
      }

      ///////////////////////////////////////////////////////////////
      // count total locations for utilization % calculation       //
      ///////////////////////////////////////////////////////////////
      QQueryFilter locationFilter = new QQueryFilter();
      if(StringUtils.hasContent(warehouseId))
      {
         locationFilter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      CountInput countInput = new CountInput(WmsLocation.TABLE_NAME);
      countInput.setFilter(locationFilter);
      CountOutput countOutput = new CountAction().execute(countInput);
      Integer totalLocations = countOutput.getCount();

      String utilization = "0.0%";
      if(totalLocations != null && totalLocations > 0)
      {
         BigDecimal pct = new BigDecimal(locationsUsed)
            .multiply(new BigDecimal(100))
            .divide(new BigDecimal(totalLocations), 1, RoundingMode.HALF_UP);
         utilization = pct.toPlainString() + "%";
      }

      //////////////////////////////////////////////////////////////
      // build the statistics data with the primary stat + context //
      //////////////////////////////////////////////////////////////
      StatisticsData data = new StatisticsData(
         String.valueOf(skuCount),
         null,
         "Total SKUs"
      );
      data.withCountContext(
         "Units on Hand: " + unitsOnHand.toPlainString()
         + " | Locations Used: " + locationsUsed
         + " | Utilization: " + utilization
      );

      return (new RenderWidgetOutput(data));
   }
}
