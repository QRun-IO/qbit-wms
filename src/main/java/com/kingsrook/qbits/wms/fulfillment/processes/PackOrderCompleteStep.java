/*******************************************************************************
 ** Backend step for PackOrder complete phase.  Marks the carton as PACKED
 ** and triggers the PACK task completion via the TaskCompletionDispatcher.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.tasks.completion.TaskCompletionDispatcher;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PackOrderCompleteStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(PackOrderCompleteStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer cartonId = input.getValueInteger("cartonId");
      Integer orderId = input.getValueInteger("orderId");
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer clientId = input.getValueInteger("clientId");

      if(cartonId == null)
      {
         throw new QUserFacingException("Carton ID is required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Update carton status to PACKED                                      //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", cartonId)
         .withValue("statusId", CartonStatus.PACKED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Create and complete a PACK task to trigger completion handler       //
      /////////////////////////////////////////////////////////////////////////
      InsertOutput taskInsert = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("taskTypeId", TaskType.PACK.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("orderId", orderId)
         .withValue("cartonId", cartonId)
         .withValue("quantityCompleted", BigDecimal.ONE)));

      QRecord completedTask = taskInsert.getRecords().get(0);
      TaskCompletionDispatcher.complete(completedTask);

      LOG.info("Pack order complete step finished", logPair("cartonId", cartonId), logPair("orderId", orderId));

      output.addValue("resultMessage", "Carton " + cartonId + " packed successfully.");
   }
}
