/*******************************************************************************
 ** Unit tests for ReleaseWaveGeneratePicksStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.fulfillment.model.WmsWave;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReleaseWaveStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that releasing a wave generates pick tasks and advances wave status.
    *******************************************************************************/
   @Test
   void testRun_validWave_generatesPickTasks() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);
      Integer waveId = insertWave(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Set up inventory with allocation                                    //
      /////////////////////////////////////////////////////////////////////////
      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("quantityAllocated", new BigDecimal("10"))
         .withValue("quantityAvailable", new BigDecimal("90"))));

      /////////////////////////////////////////////////////////////////////////
      // Set order to ALLOCATED and assign to wave                           //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.ALLOCATED.getPossibleValueId())
         .withValue("waveId", waveId)));

      /////////////////////////////////////////////////////////////////////////
      // Set order line allocated                                            //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityAllocated", 10)));

      /////////////////////////////////////////////////////////////////////////
      // Release the wave                                                    //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("waveId", waveId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReleaseWaveGeneratePicksStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify wave status is RELEASED                                      //
      /////////////////////////////////////////////////////////////////////////
      QRecord wave = new GetAction().execute(new GetInput(WmsWave.TABLE_NAME).withPrimaryKey(waveId)).getRecord();
      assertThat(wave.getValueInteger("statusId")).isEqualTo(WaveStatus.RELEASED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify PICK tasks were generated                                    //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PICK.getPossibleValueId()))));
      assertThat(taskQuery.getRecords()).isNotEmpty();

      /////////////////////////////////////////////////////////////////////////
      // Verify order status changed to PICK_RELEASED                        //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PICK_RELEASED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify result message                                               //
      /////////////////////////////////////////////////////////////////////////
      assertThat(output.getValueString("resultMessage")).contains("pick tasks generated");
   }
}
