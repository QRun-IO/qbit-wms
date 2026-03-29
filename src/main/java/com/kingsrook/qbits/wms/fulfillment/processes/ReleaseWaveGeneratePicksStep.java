/*******************************************************************************
 ** Backend step for ReleaseWave.  Loads a PLANNED wave, sets its status to
 ** RELEASED, then for each order line in the wave generates PICK tasks with
 ** source locations determined by the allocation records.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.fulfillment.model.WmsWave;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ReleaseWaveGeneratePicksStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ReleaseWaveGeneratePicksStep.class);



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

      /////////////////////////////////////////////////////////////////////////
      // Load wave                                                           //
      /////////////////////////////////////////////////////////////////////////
      GetOutput waveGet = new GetAction().execute(new GetInput(WmsWave.TABLE_NAME).withPrimaryKey(waveId));
      QRecord wave = waveGet.getRecord();

      if(wave == null)
      {
         throw new QUserFacingException("Wave not found.");
      }

      Integer warehouseId = wave.getValueInteger("warehouseId");

      /////////////////////////////////////////////////////////////////////////
      // Set wave status to RELEASED                                         //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsWave.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", waveId)
         .withValue("statusId", WaveStatus.RELEASED.getPossibleValueId())
         .withValue("releasedDate", Instant.now())));

      /////////////////////////////////////////////////////////////////////////
      // Query all orders in this wave                                       //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput orderQuery = new QueryAction().execute(new QueryInput(WmsOrder.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("waveId", QCriteriaOperator.EQUALS, waveId))));

      int picksGenerated = 0;

      for(QRecord order : orderQuery.getRecords())
      {
         Integer orderId = order.getValueInteger("id");
         Integer clientId = order.getValueInteger("clientId");

         /////////////////////////////////////////////////////////////////////
         // Query order lines                                               //
         /////////////////////////////////////////////////////////////////////
         QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))));

         for(QRecord line : lineQuery.getRecords())
         {
            Integer lineId = line.getValueInteger("id");
            Integer itemId = line.getValueInteger("itemId");
            BigDecimal qtyAllocated = ValueUtils.getValueAsBigDecimal(line.getValue("quantityAllocated"));

            if(qtyAllocated == null || qtyAllocated.compareTo(BigDecimal.ZERO) <= 0)
            {
               continue;
            }

            //////////////////////////////////////////////////////////////////
            // Find the inventory that was allocated                        //
            //////////////////////////////////////////////////////////////////
            QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))
                  .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
                  .withCriteria(new QFilterCriteria("quantityAllocated", QCriteriaOperator.GREATER_THAN, 0))
                  .withOrderBy(new QFilterOrderBy("id", true))));

            BigDecimal remaining = qtyAllocated;

            for(QRecord inv : invQuery.getRecords())
            {
               if(remaining.compareTo(BigDecimal.ZERO) <= 0)
               {
                  break;
               }

               BigDecimal invAllocated = ValueUtils.getValueAsBigDecimal(inv.getValue("quantityAllocated"));
               if(invAllocated == null || invAllocated.compareTo(BigDecimal.ZERO) <= 0)
               {
                  continue;
               }

               BigDecimal pickQty = invAllocated.min(remaining);

               //////////////////////////////////////////////////////////////
               // Create PICK task                                         //
               //////////////////////////////////////////////////////////////
               new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
                  .withValue("warehouseId", warehouseId)
                  .withValue("clientId", clientId)
                  .withValue("taskTypeId", TaskType.PICK.getPossibleValueId())
                  .withValue("taskStatusId", TaskStatus.PENDING.getPossibleValueId())
                  .withValue("priority", 5)
                  .withValue("itemId", itemId)
                  .withValue("quantityRequested", pickQty)
                  .withValue("sourceLocationId", inv.getValueInteger("locationId"))
                  .withValue("orderId", orderId)
                  .withValue("orderLineId", lineId)
                  .withValue("lotNumber", inv.getValueString("lotNumber"))
                  .withValue("referenceType", "ORDER_LINE")
                  .withValue("referenceId", lineId)
                  .withValue("notes", "Pick for wave " + waveId)));

               picksGenerated++;
               remaining = remaining.subtract(pickQty);
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Update order status to PICK_RELEASED                            //
         /////////////////////////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", orderId)
            .withValue("statusId", OrderStatus.PICK_RELEASED.getPossibleValueId())));
      }

      LOG.info("Wave released", logPair("waveId", waveId), logPair("picksGenerated", picksGenerated));

      output.addValue("resultMessage", "Wave released. " + picksGenerated + " pick tasks generated.");
      output.addValue("picksGenerated", picksGenerated);
   }
}
