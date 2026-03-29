/*******************************************************************************
 ** Unit tests for LowStockAlertCheckStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class LowStockAlertCheckStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that items below reorder point trigger alerts.
    *******************************************************************************/
   @Test
   void testRun_itemBelowReorderPoint_alertFound() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Create item with reorder point of 100                               //
      /////////////////////////////////////////////////////////////////////////
      QRecord itemRecord = new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME).withRecordEntity(new WmsItem()
         .withSku("LOW-SKU-01")
         .withName("Low Stock Item")
         .withReorderPoint(100)
         .withIsActive(true)))
      .getRecords().get(0);

      Integer itemId = itemRecord.getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Insert inventory at 10, well below reorder point of 100             //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("10"));

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new LowStockAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(1);
      assertThat(output.getValueString("resultMessage")).contains("1 items");
   }



   /*******************************************************************************
    ** Test that items above reorder point do not trigger alerts.
    *******************************************************************************/
   @Test
   void testRun_itemAboveReorderPoint_noAlert() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer locationId = insertLocation(warehouseId);

      QRecord itemRecord = new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME).withRecordEntity(new WmsItem()
         .withSku("HIGH-SKU")
         .withName("Well Stocked Item")
         .withReorderPoint(10)
         .withIsActive(true)))
      .getRecords().get(0);

      Integer itemId = itemRecord.getValueInteger("id");
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("500"));

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new LowStockAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test with no items returns zero alerts.
    *******************************************************************************/
   @Test
   void testRun_noItemsWithReorderPoint_noAlerts() throws Exception
   {
      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new LowStockAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(0);
   }



   /*******************************************************************************
    ** Test that item with zero inventory triggers alert.
    *******************************************************************************/
   @Test
   void testRun_zeroInventory_alertFound() throws Exception
   {
      /////////////////////////////////////////////////////////////////////////
      // Create item with reorder point but no inventory at all              //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsItem.TABLE_NAME).withRecordEntity(new WmsItem()
         .withSku("ZERO-SKU")
         .withName("Out of Stock Item")
         .withReorderPoint(50)
         .withIsActive(true)));

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new LowStockAlertCheckStep().run(input, output);

      assertThat(output.getValueInteger("alertCount")).isEqualTo(1);
   }
}
