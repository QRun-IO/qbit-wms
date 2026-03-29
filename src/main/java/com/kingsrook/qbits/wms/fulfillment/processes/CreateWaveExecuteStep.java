/*******************************************************************************
 ** Backend "execute" step for CreateWave.  Creates a wms_wave record and
 ** assigns the waveId to all matching ALLOCATED orders from the find step.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class CreateWaveExecuteStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(CreateWaveExecuteStep.class);

   public static final String WAVE_TABLE_NAME  = "wmsWave";
   public static final String ORDER_TABLE_NAME = "wmsOrder";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer waveTypeId = input.getValueInteger("waveTypeId");
      List<QRecord> matchingOrders = input.getRecords();

      if(matchingOrders == null || matchingOrders.isEmpty())
      {
         throw new QUserFacingException("No matching orders found for wave criteria.");
      }

      String createdBy = null;
      if(QContext.getQSession() != null && QContext.getQSession().getUser() != null)
      {
         createdBy = QContext.getQSession().getUser().getFullName();
      }

      /////////////////////////////////////////////////////////////////////////
      // Create the wave record                                              //
      /////////////////////////////////////////////////////////////////////////
      String waveNumber = "WAVE-" + System.currentTimeMillis();

      InsertOutput waveInsert = new InsertAction().execute(new InsertInput(WAVE_TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("waveNumber", waveNumber)
         .withValue("waveTypeId", waveTypeId)
         .withValue("statusId", WaveStatus.PLANNED.getPossibleValueId())
         .withValue("orderCount", matchingOrders.size())
         .withValue("createdBy", createdBy)
         .withValue("createdDate", Instant.now())));

      Integer waveId = waveInsert.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Assign waveId to each matching order                                //
      /////////////////////////////////////////////////////////////////////////
      for(QRecord order : matchingOrders)
      {
         new UpdateAction().execute(new UpdateInput(ORDER_TABLE_NAME).withRecord(new QRecord()
            .withValue("id", order.getValueInteger("id"))
            .withValue("waveId", waveId)));
      }

      LOG.info("Wave created", logPair("waveId", waveId), logPair("orderCount", matchingOrders.size()));

      output.addValue("resultMessage", "Wave " + waveNumber + " created with " + matchingOrders.size() + " orders.");
      output.addValue("waveId", waveId);
      output.addValue("ordersInWave", matchingOrders.size());
   }
}
