/*******************************************************************************
 ** MetaData producer for the Billing Dashboard widget.
 **
 ** Provides a MULTI_STATISTICS view showing total revenue this period,
 ** unbilled activities count, and invoices by status (Draft/Sent/Paid/Overdue).
 *******************************************************************************/
package com.kingsrook.qbits.wms.widgets;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;


public class BillingDashboardWidgetProducer extends MetaDataProducer<QWidgetMetaData>
{
   public static final String NAME = "billingDashboardWidget";



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
         .withLabel("Billing Dashboard")
         .withTooltip("Overview of billing revenue, unbilled activities, and invoice statuses")
         .withShowReloadButton(true)
         .withIcon("topLeftInsideCard", new QIcon("payments").withColor("#2E7D32"))
         .withCodeReference(new QCodeReference(BillingDashboardRenderer.class));
   }
}
