/*******************************************************************************
 ** Unit tests for VoidCartonStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class VoidCartonStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that voiding a packed carton sets its status to VOID.
    *******************************************************************************/
   @Test
   void testRun_voidPackedCarton_setsVoidStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer orderId = insertOrder(warehouseId);
      Integer cartonId = insertCarton(orderId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("cartonId", cartonId);
      input.addValue("reason", "Damaged during packing");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new VoidCartonStep().run(input, output);

      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      assertThat(carton.getValueInteger("statusId")).isEqualTo(CartonStatus.VOID.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("voided");
   }
}
