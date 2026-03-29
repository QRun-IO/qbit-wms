/*******************************************************************************
 ** Unit tests for {@link DirectedPutawayStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class DirectedPutawayStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that when a matching putaway rule exists, the location from that
    ** rule's target zone is returned.
    *******************************************************************************/
   @Test
   void testRun_matchingRule_returnsRuleTargetLocation() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId, "Target Zone", "TZ01");
      Integer locationId = insertLocation(warehouseId, zoneId, "LOC-TARGET-001");
      Integer itemId = insertItem();
      insertPutawayRule(warehouseId, zoneId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("warehouseId", warehouseId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new DirectedPutawayStep().run(input, output);

      assertThat(output.getValueInteger("locationId")).isEqualTo(locationId);
   }



   /*******************************************************************************
    ** Test that when no putaway rules match, it falls back to a BULK zone
    ** location.
    *******************************************************************************/
   @Test
   void testRun_noMatchingRule_fallsToBulkZone() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer bulkZoneId = insertZone(warehouseId, "Bulk Zone", "BLK01");
      Integer locationId = insertLocation(warehouseId, bulkZoneId, "LOC-BULK-001");
      Integer itemId = insertItem();

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("warehouseId", warehouseId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new DirectedPutawayStep().run(input, output);

      assertThat(output.getValueInteger("locationId")).isEqualTo(locationId);
   }



   /*******************************************************************************
    ** Test that when no zones or locations exist, returns null gracefully.
    *******************************************************************************/
   @Test
   void testRun_noZonesOrLocations_returnsNull() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("warehouseId", warehouseId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new DirectedPutawayStep().run(input, output);

      assertThat(output.getValueInteger("locationId")).isNull();
   }
}
