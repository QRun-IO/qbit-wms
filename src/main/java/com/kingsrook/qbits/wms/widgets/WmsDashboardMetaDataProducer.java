/*******************************************************************************
 ** MetaData producer for the WMS Dashboard application.
 **
 ** Creates a QAppMetaData that assembles all Phase 1 dashboard widgets into
 ** a single dashboard view accessible from the navigation.
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


public class WmsDashboardMetaDataProducer extends MetaDataProducer<QAppMetaData>
{
   public static final String NAME = "wmsDashboard";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QAppMetaData produce(QInstance qInstance) throws QException
   {
      return (new QAppMetaData()
         .withName(NAME)
         .withLabel("WMS Dashboard")
         .withIcon(new QIcon("warehouse"))
         .withWidgets(List.of(
            TaskQueueSummaryWidgetProducer.NAME,
            InventorySummaryWidgetProducer.NAME,
            WorkerProductivityWidgetProducer.NAME,
            ActiveWorkersWidgetProducer.NAME,
            TaskAgingWidgetProducer.NAME,
            LowStockAlertsWidgetProducer.NAME,
            InventoryAccuracyWidgetProducer.NAME,
            BillingDashboardWidgetProducer.NAME,
            FulfillmentPipelineWidgetProducer.NAME,
            OrdersTodayWidgetProducer.NAME,
            SlaRiskWidgetProducer.NAME
         )));
   }
}
