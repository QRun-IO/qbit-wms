/*******************************************************************************
 ** Unit tests for StorageSnapshotStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class StorageSnapshotStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that the storage snapshot creates billing activities for clients with
    ** inventory.
    *******************************************************************************/
   @Test
   void testRun_clientWithInventory_createsBillingActivities() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer clientId = insertClient();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Create inventory with a clientId set                                //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", itemId)
         .withValue("locationId", locationId)
         .withValue("quantityOnHand", new BigDecimal("10"))
         .withValue("quantityAvailable", new BigDecimal("10"))
         .withValue("inventoryStatusId", InventoryStatus.AVAILABLE.getId())));

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new StorageSnapshotStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify billing activities were created                              //
      /////////////////////////////////////////////////////////////////////////
      List<QRecord> activities = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId)))).getRecords();

      assertThat(activities).isNotEmpty();
      assertThat(output.getValueInteger("activitiesCreated")).isGreaterThan(0);
   }
}
