/*******************************************************************************
 ** MetaData producer for the Inventory Summary widget.
 **
 ** Provides a STATISTICS view showing Total SKUs, Total Units on Hand,
 ** Total Locations Used, and Utilization percentage.
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


public class InventorySummaryWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "inventorySummaryWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.STATISTICS.getType())
         .withGridColumns(3)
         .withIsCard(true)
         .withLabel("Inventory Summary")
         .withTooltip("Overview of current inventory levels")
         .withShowReloadButton(true)
         .withIcon("topLeftInsideCard", new QIcon("inventory").withColor("#2E7D32"))
         .withDropdown(new WidgetDropdownData()
            .withName("warehouseId")
            .withPossibleValueSourceName(WmsWarehouse.TABLE_NAME)
            .withLabel("Warehouse")
            .withLabelForNullValue("All Warehouses")
            .withIsRequired(false))
         .withCodeReference(new QCodeReference(InventorySummaryRenderer.class));
   }
}
