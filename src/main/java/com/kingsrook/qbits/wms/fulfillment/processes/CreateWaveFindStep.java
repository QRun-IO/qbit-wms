/*******************************************************************************
 ** Backend "find" step for CreateWave.  Queries ALLOCATED orders matching the
 ** wave criteria (carrier, service level, ship-by date) and makes them
 ** available for review on the next frontend step.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CreateWaveFindStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CreateWaveFindStep.class);

   public static final String ORDER_TABLE_NAME = "wmsOrder";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer carrierId = input.getValueInteger("carrierId");
      Integer shippingModeId = input.getValueInteger("shippingModeId");
      LocalDate shipByDate = input.getValueLocalDate("shipByDate");

      if(warehouseId == null)
      {
         throw new QUserFacingException("Warehouse is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Query ALLOCATED orders matching wave criteria                       //
      /////////////////////////////////////////////////////////////////////////
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, OrderStatus.ALLOCATED.getPossibleValueId()))
         .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId));

      if(carrierId != null)
      {
         filter.withCriteria(new QFilterCriteria("carrierId", QCriteriaOperator.EQUALS, carrierId));
      }
      if(shippingModeId != null)
      {
         filter.withCriteria(new QFilterCriteria("shippingModeId", QCriteriaOperator.EQUALS, shippingModeId));
      }
      if(shipByDate != null)
      {
         filter.withCriteria(new QFilterCriteria("shipByDate", QCriteriaOperator.LESS_THAN_OR_EQUALS, shipByDate));
      }

      QueryOutput queryOutput = new QueryAction().execute(new QueryInput(ORDER_TABLE_NAME).withFilter(filter));
      List<QRecord> matchingOrders = queryOutput.getRecords();

      LOG.info("Wave find step completed", logPair("matchingOrders", matchingOrders.size()));

      output.addValue("matchingOrderCount", matchingOrders.size());
      output.setRecords(matchingOrders);
   }
}
