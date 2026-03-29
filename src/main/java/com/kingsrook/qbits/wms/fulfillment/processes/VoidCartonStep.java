/*******************************************************************************
 ** Backend step for VoidCarton.  Sets a carton's status to VOID, effectively
 ** removing it from the shipping pipeline.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class VoidCartonStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(VoidCartonStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer cartonId = input.getValueInteger("cartonId");
      String reason = input.getValueString("reason");

      if(cartonId == null)
      {
         throw new QUserFacingException("Carton ID is required.");
      }

      GetOutput cartonGet = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId));
      QRecord carton = cartonGet.getRecord();

      if(carton == null)
      {
         throw new QUserFacingException("Carton not found.");
      }

      new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", cartonId)
         .withValue("statusId", CartonStatus.VOID.getPossibleValueId())));

      LOG.info("Carton voided", logPair("cartonId", cartonId), logPair("reason", reason));

      output.addValue("resultMessage", "Carton " + cartonId + " voided.");
   }
}
