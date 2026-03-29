/*******************************************************************************
 ** Unit tests for ExpirationAlertCheckStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ExpirationAlertCheckStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that inventory expiring within the threshold triggers an alert.
    *******************************************************************************/
   @Test
   void testRun_inventoryExpiringWithinThreshold_alertsFound() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("50"));

      /////////////////////////////////////////////////////////////////////////
      // Set expiration date to 5 days from now                              //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("expirationDate", LocalDate.now().plusDays(5))));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("thresholdDays", 10);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new ExpirationAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(1);
      assertThat(output.getValueString("resultMessage")).contains("1 inventory records");
   }



   /*******************************************************************************
    ** Test that inventory not expiring within threshold returns zero alerts.
    *******************************************************************************/
   @Test
   void testRun_inventoryNotExpiring_noAlerts() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("50"));

      /////////////////////////////////////////////////////////////////////////
      // Set expiration date to 60 days from now                             //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("expirationDate", LocalDate.now().plusDays(60))));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("thresholdDays", 10);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new ExpirationAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test that null threshold throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_nullThreshold_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ExpirationAlertCheckStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("positive number");
   }



   /*******************************************************************************
    ** Test that negative threshold throws user-facing exception.
    *******************************************************************************/
   @Test
   void testRun_negativeThreshold_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("thresholdDays", -5);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new ExpirationAlertCheckStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("positive number");
   }



   /*******************************************************************************
    ** Test with no inventory at all returns zero alerts.
    *******************************************************************************/
   @Test
   void testRun_noInventory_noAlerts() throws Exception
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("thresholdDays", 30);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new ExpirationAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(0);
   }
}
