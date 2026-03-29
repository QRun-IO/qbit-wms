/*******************************************************************************
 ** Unit tests for CancelWaveStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.WaveStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsWave;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class CancelWaveStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that cancelling a wave sets it to CANCELLED and returns orders to
    ** ALLOCATED status with null waveId.
    *******************************************************************************/
   @Test
   void testRun_cancelWave_reversesOrderAssignment() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer waveId = insertWave(warehouseId);
      Integer orderId = insertOrder(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Assign order to the wave                                            //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("waveId", waveId)
         .withValue("statusId", OrderStatus.PICK_RELEASED.getPossibleValueId())));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("waveId", waveId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new CancelWaveStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify wave status                                                  //
      /////////////////////////////////////////////////////////////////////////
      QRecord wave = new GetAction().execute(new GetInput(WmsWave.TABLE_NAME).withPrimaryKey(waveId)).getRecord();
      assertThat(wave.getValueInteger("statusId")).isEqualTo(WaveStatus.CANCELLED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify order reverted to ALLOCATED with null waveId                 //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.ALLOCATED.getPossibleValueId());
      assertThat(order.getValue("waveId")).isNull();

      assertThat(output.getValueString("resultMessage")).contains("cancelled");
   }
}
