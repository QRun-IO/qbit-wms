/*******************************************************************************
 ** MetaData producer for the Inventory Accuracy widget.
 **
 ** Provides a STATISTICS view showing the accuracy percentage derived from
 ** recent cycle count tasks (COUNT type, COMPLETED status) by comparing
 ** counted_quantity against expected_quantity.
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


public class InventoryAccuracyWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "inventoryAccuracyWidget";



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
         .withLabel("Inventory Accuracy")
         .withTooltip("Accuracy percentage from recent cycle counts")
         .withShowReloadButton(true)
         .withIcon("topLeftInsideCard", new QIcon("fact_check").withColor("#7B1FA2"))
         .withDropdown(new WidgetDropdownData()
            .withName("warehouseId")
            .withPossibleValueSourceName(WmsWarehouse.TABLE_NAME)
            .withLabel("Warehouse")
            .withLabelForNullValue("All Warehouses")
            .withIsRequired(false))
         .withCodeReference(new QCodeReference(InventoryAccuracyRenderer.class));
   }
}
