/*******************************************************************************
 ** Backend step for PackOrder load phase.  Loads the order and its carton
 ** information for the pack station UI.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PackOrderLoadStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(PackOrderLoadStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer orderId = input.getValueInteger("orderId");

      if(orderId == null)
      {
         throw new QUserFacingException("Order ID is required.");
      }

      GetOutput orderGet = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId));
      QRecord order = orderGet.getRecord();

      if(order == null)
      {
         throw new QUserFacingException("Order not found.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Load order lines for display                                        //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      /////////////////////////////////////////////////////////////////////////
      // Load existing cartons                                               //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput cartonQuery = new QueryAction().execute(new QueryInput(WmsCarton.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

      LOG.info("Loaded order for packing", logPair("orderId", orderId),
         logPair("lineCount", lineQuery.getRecords().size()),
         logPair("cartonCount", cartonQuery.getRecords().size()));

      output.addValue("orderId", orderId);
      output.addValue("orderNumber", order.getValueString("orderNumber"));
      output.addValue("warehouseId", order.getValueInteger("warehouseId"));
      output.addValue("clientId", order.getValueInteger("clientId"));
      output.addValue("lineCount", lineQuery.getRecords().size());
      output.addValue("cartonCount", cartonQuery.getRecords().size());
      output.setRecords(lineQuery.getRecords());
   }
}
