/*******************************************************************************
 ** MetaData producer for the Task Aging widget.
 **
 ** Provides a BAR_CHART showing the distribution of PENDING and ASSIGNED tasks
 ** by age bucket: 0-15min, 15-30min, 30-60min, 1-4h, and 4h+.
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


public class TaskAgingWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "taskAgingWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      return new QWidgetMetaData()
         .withName(NAME)
         .withType(WidgetType.BAR_CHART.getType())
         .withGridColumns(4)
         .withIsCard(true)
         .withLabel("Task Aging")
         .withTooltip("Distribution of pending/assigned tasks by age")
         .withShowReloadButton(true)
         .withIcon("topRightInsideCard", new QIcon("schedule").withColor("#E65100"))
         .withDropdown(new WidgetDropdownData()
            .withName("warehouseId")
            .withPossibleValueSourceName(WmsWarehouse.TABLE_NAME)
            .withLabel("Warehouse")
            .withLabelForNullValue("All Warehouses")
            .withIsRequired(false))
         .withCodeReference(new QCodeReference(TaskAgingRenderer.class));
   }
}
