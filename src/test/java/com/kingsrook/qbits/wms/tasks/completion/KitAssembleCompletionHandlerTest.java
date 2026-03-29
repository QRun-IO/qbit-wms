/*******************************************************************************
 ** Unit tests for KitAssembleCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class KitAssembleCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a KIT_ASSEMBLE task deducts component inventory,
    ** creates finished kit inventory, and creates billing activity.
    *******************************************************************************/
   @Test
   void testHandle_normalKitAssembly_createsTransactionsAndBillingActivity() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer clientId = insertClient();
      Integer kitItemId = insertItem("KIT-ITEM", "Finished Kit");
      Integer componentItemId = insertItem("COMP-A", "Component A");
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // Set up BOM: 1 kit needs 2 of component                             //
      /////////////////////////////////////////////////////////////////////////
      insertKitBom(kitItemId, componentItemId, 2);

      /////////////////////////////////////////////////////////////////////////
      // Insert component inventory at the location                         //
      /////////////////////////////////////////////////////////////////////////
      insertInventory(warehouseId, componentItemId, locationId, new BigDecimal("100"));

      /////////////////////////////////////////////////////////////////////////
      // Create completed KIT_ASSEMBLE task                                 //
      /////////////////////////////////////////////////////////////////////////
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("clientId", clientId)
         .withValue("itemId", kitItemId)
         .withValue("destinationLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("5"))
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new KitAssembleCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify inventory transactions were created (PICK for components    //
      // and RECEIVE for finished kit)                                      //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSizeGreaterThanOrEqualTo(2);

      /////////////////////////////////////////////////////////////////////////
      // Verify billing activity was created for the client                 //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput billingQuery = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskId", QCriteriaOperator.EQUALS, 1))));
      assertThat(billingQuery.getRecords()).hasSize(1);
   }



   /*******************************************************************************
    ** Test that null quantity completed skips processing.
    *******************************************************************************/
   @Test
   void testHandle_nullQuantityCompleted_skips() throws Exception
   {
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", 1)
         .withValue("itemId", 1)
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId());

      new KitAssembleCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify no transactions were created                                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isEmpty();
   }



   /*******************************************************************************
    ** Test that zero quantity completed skips processing.
    *******************************************************************************/
   @Test
   void testHandle_zeroQuantityCompleted_skips() throws Exception
   {
      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", 1)
         .withValue("itemId", 1)
         .withValue("quantityCompleted", BigDecimal.ZERO)
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId());

      new KitAssembleCompletionHandler().handle(task);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isEmpty();
   }



   /*******************************************************************************
    ** Test that kit with no BOM still creates finished kit inventory transaction.
    *******************************************************************************/
   @Test
   void testHandle_noBom_createsFinishedKitOnly() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer kitItemId = insertItem("KIT-NO-BOM", "Kit Without BOM");
      Integer locationId = insertLocation(warehouseId);

      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", kitItemId)
         .withValue("destinationLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("3"))
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new KitAssembleCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify only the RECEIVE transaction for finished kit was created   //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
   }



   /*******************************************************************************
    ** Test that without clientId, no billing activity is created.
    *******************************************************************************/
   @Test
   void testHandle_noClientId_noBillingActivityCreated() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer kitItemId = insertItem("KIT-NO-CLIENT", "Kit No Client");
      Integer locationId = insertLocation(warehouseId);

      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", kitItemId)
         .withValue("destinationLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("5"))
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new KitAssembleCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify no billing activity was created (no client)                 //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput billingQuery = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME));
      assertThat(billingQuery.getRecords()).isEmpty();
   }



   /*******************************************************************************
    ** Test with multi-component BOM.
    *******************************************************************************/
   @Test
   void testHandle_multiComponentBom_deductsAllComponents() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer kitItemId = insertItem("KIT-MULTI", "Multi-Component Kit");
      Integer comp1 = insertItem("COMP-1", "Component 1");
      Integer comp2 = insertItem("COMP-2", "Component 2");
      Integer comp3 = insertItem("COMP-3", "Component 3");
      Integer locationId = insertLocation(warehouseId);

      /////////////////////////////////////////////////////////////////////////
      // BOM: 3 components per kit                                          //
      /////////////////////////////////////////////////////////////////////////
      insertKitBom(kitItemId, comp1, 1);
      insertKitBom(kitItemId, comp2, 3);
      insertKitBom(kitItemId, comp3, 5);

      insertInventory(warehouseId, comp1, locationId, new BigDecimal("100"));
      insertInventory(warehouseId, comp2, locationId, new BigDecimal("100"));
      insertInventory(warehouseId, comp3, locationId, new BigDecimal("100"));

      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", kitItemId)
         .withValue("destinationLocationId", locationId)
         .withValue("quantityCompleted", new BigDecimal("2"))
         .withValue("taskTypeId", TaskType.KIT_ASSEMBLE.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new KitAssembleCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify: 3 PICK transactions (one per component) + 1 RECEIVE for   //
      // finished kit = 4 total transactions                                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(4);
   }
}
