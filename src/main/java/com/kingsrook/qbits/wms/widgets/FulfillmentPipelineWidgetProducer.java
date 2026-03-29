/*******************************************************************************
 ** MetaData producer for the Fulfillment Pipeline widget.
 **
 ** Displays a stacked bar chart showing order counts at each stage of the
 ** fulfillment pipeline (Pending, Allocated, Picking, Picked, Packing,
 ** Packed, Shipped).
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.WidgetDropdownData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


public class FulfillmentPipelineWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "fulfillmentPipelineWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.MULTI_STATISTICS.getType())
         .withGridColumns(6)
         .withIsCard(true)
         .withLabel("Fulfillment Pipeline")
         .withTooltip("Order counts by fulfillment stage")
         .withShowReloadButton(true)
         .withIcon("topLeftInsideCard", new QIcon("timeline").withColor("#2E7D32"))
         .withDropdown(new WidgetDropdownData()
            .withName("warehouseId")
            .withPossibleValueSourceName(WmsWarehouse.TABLE_NAME)
            .withLabel("Warehouse")
            .withLabelForNullValue("All Warehouses")
            .withIsRequired(false))
         .withCodeReference(new QCodeReference(FulfillmentPipelineRenderer.class));
   }
}
