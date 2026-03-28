/*******************************************************************************
 ** MetaData producer for the Task Queue Summary widget.
 **
 ** Provides a MULTI_STATISTICS view of task counts by status, grouped by task
 ** type, with an optional warehouse dropdown filter.
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


public class TaskQueueSummaryWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "taskQueueSummaryWidget";



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
         .withLabel("Task Queue Summary")
         .withTooltip("Task counts by status grouped by task type")
         .withShowReloadButton(true)
         .withIcon("topLeftInsideCard", new QIcon("assignment").withColor("#1565C0"))
         .withDropdown(new WidgetDropdownData()
            .withName("warehouseId")
            .withPossibleValueSourceName(WmsWarehouse.TABLE_NAME)
            .withLabel("Warehouse")
            .withLabelForNullValue("All Warehouses")
            .withIsRequired(false))
         .withCodeReference(new QCodeReference(TaskQueueSummaryRenderer.class));
   }
}
