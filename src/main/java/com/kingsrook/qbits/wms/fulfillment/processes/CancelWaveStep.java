/*******************************************************************************
 ** Backend step for CancelWave.  Sets the wave status to CANCELLED and
 ** removes the wave assignment from all associated orders, returning them
 ** to ALLOCATED status.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsWave;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CancelWaveStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CancelWaveStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer waveId = input.getValueInteger("waveId");

      if(waveId == null)
      {
         throw new QUserFacingException("Wave ID is required.");
      }

      GetOutput waveGet = new GetAction().execute(new GetInput(WmsWave.TABLE_NAME).withPrimaryKey(waveId));
      QRecord wave = waveGet.getRecord();

      if(wave == null)
      {
         throw new QUserFacingException("Wave not found.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Cancel the wave                                                     //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsWave.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", waveId)
         .withValue("statusId", WaveStatus.CANCELLED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Remove wave assignment from orders, return to ALLOCATED             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput orderQuery = new QueryAction().execute(new QueryInput(WmsOrder.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("waveId", QCriteriaOperator.EQUALS, waveId))));

      List<QRecord> orders = orderQuery.getRecords();
      for(QRecord order : orders)
      {
         new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", order.getValueInteger("id"))
            .withValue("waveId", null)
            .withValue("statusId", OrderStatus.ALLOCATED.getPossibleValueId())));
      }

      LOG.info("Wave cancelled", logPair("waveId", waveId), logPair("ordersReleased", orders.size()));

      output.addValue("resultMessage", "Wave " + waveId + " cancelled. " + orders.size() + " orders returned to ALLOCATED.");
   }
}
