/*******************************************************************************
 ** Renderer for the Low Stock Alerts widget.
 **
 ** Aggregates wms_inventory by itemId to get total quantity on hand per item,
 ** then joins with wms_item to compare against the reorder point. Displays a
 ** table of SKU, item name, on-hand quantity, reorder point, and deficit.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.io.Serializable;
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
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.TableData;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;


public class LowStockAlertsRenderer extends AbstractWidgetRenderer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String warehouseId = input.getQueryParams().get("warehouseId");

      ///////////////////////////////////////////////////////////////
      // aggregate inventory: SUM(quantityOnHand) GROUP BY itemId  //
      ///////////////////////////////////////////////////////////////
      QQueryFilter inventoryFilter = new QQueryFilter();
      if(StringUtils.hasContent(warehouseId))
      {
         inventoryFilter.withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, ValueUtils.getValueAsInteger(warehouseId)));
      }

      Aggregate sumOnHand    = new Aggregate("quantityOnHand", AggregateOperator.SUM);
      GroupBy   itemGroup    = new GroupBy(QFieldType.INTEGER, "itemId");

      AggregateInput aggInput = new AggregateInput(WmsInventory.TABLE_NAME);
      aggInput.setFilter(inventoryFilter);
      aggInput.withAggregates(List.of(sumOnHand));
      aggInput.withGroupBys(List.of(itemGroup));

      AggregateOutput aggOutput = new AggregateAction().execute(aggInput);

      //////////////////////////////////////////////////////////
      // collect item ids and their on-hand quantities         //
      //////////////////////////////////////////////////////////
      Map<Integer, BigDecimal> itemOnHand = new LinkedHashMap<>();
      if(aggOutput.getResults() != null)
      {
         for(AggregateResult result : aggOutput.getResults())
         {
            Integer itemId = ValueUtils.getValueAsInteger(result.getGroupByValue(itemGroup));
            BigDecimal qoh = ValueUtils.getValueAsBigDecimal(result.getAggregateValue(sumOnHand));
            if(itemId != null)
            {
               itemOnHand.put(itemId, qoh != null ? qoh : BigDecimal.ZERO);
            }
         }
      }

      ///////////////////////////////////////////////////////////////
      // query items that have a reorder point to compare against  //
      ///////////////////////////////////////////////////////////////
      QQueryFilter itemFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("reorderPoint", QCriteriaOperator.IS_NOT_BLANK))
         .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true));

      QueryInput itemQueryInput = new QueryInput(WmsItem.TABLE_NAME);
      itemQueryInput.setFilter(itemFilter);
      QueryOutput itemOutput = new QueryAction().execute(itemQueryInput);

      ///////////////////////////
      // build table columns   //
      ///////////////////////////
      List<TableData.Column> columns = List.of(
         new TableData.Column("html", "SKU", "sku", "1fr", null),
         new TableData.Column("html", "Item Name", "itemName", "2fr", null),
         new TableData.Column("html", "On Hand", "onHand", "1fr", "right"),
         new TableData.Column("html", "Reorder Point", "reorderPoint", "1fr", "right"),
         new TableData.Column("html", "Deficit", "deficit", "1fr", "right")
      );

      //////////////////////////
      // build table rows     //
      //////////////////////////
      List<Map<String, Object>> rows = new ArrayList<>();
      for(QRecord item : itemOutput.getRecords())
      {
         Integer itemId = item.getValueInteger("id");
         Integer reorderPoint = item.getValueInteger("reorderPoint");
         if(reorderPoint == null || reorderPoint <= 0)
         {
            continue;
         }

         BigDecimal onHand = itemOnHand.getOrDefault(itemId, BigDecimal.ZERO);
         if(onHand.compareTo(new BigDecimal(reorderPoint)) <= 0)
         {
            BigDecimal deficit = new BigDecimal(reorderPoint).subtract(onHand);
            rows.add(MapBuilder.of(
               "sku", item.getValueString("sku"),
               "itemName", item.getValueString("name"),
               "onHand", onHand.toPlainString(),
               "reorderPoint", String.valueOf(reorderPoint),
               "deficit", deficit.toPlainString()
            ));
         }
      }

      TableData tableData = new TableData(null, columns, rows)
         .withRowsPerPage(50)
         .withHidePaginationDropdown(true);

      return (new RenderWidgetOutput(tableData));
   }
}
