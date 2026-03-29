/*******************************************************************************
 ** Unit tests for {@link InventoryMoveStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class InventoryMoveStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that move deducts from source and adds to destination.
    *******************************************************************************/
   @Test
   void testRun_validMove_inventoryTransferred() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-001");
      Integer destLoc = insertLocation(warehouseId, null, "DST-001");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("100"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("sourceLocationId", sourceLoc);
      input.addValue("destinationLocationId", destLoc);
      input.addValue("itemId", itemId);
      input.addValue("quantityToMove", new BigDecimal("30"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryMoveStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify source deducted                                        //
      ///////////////////////////////////////////////////////////////////
      QueryOutput sourceQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, sourceLoc))));
      assertThat(sourceQuery.getRecords().get(0).getValueBigDecimal("quantityOnHand"))
         .isEqualByComparingTo(new BigDecimal("70"));

      ///////////////////////////////////////////////////////////////////
      // Verify destination added                                      //
      ///////////////////////////////////////////////////////////////////
      QueryOutput destQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("locationId", QCriteriaOperator.EQUALS, destLoc))));
      assertThat(destQuery.getRecords()).hasSize(1);
      assertThat(destQuery.getRecords().get(0).getValueBigDecimal("quantityOnHand"))
         .isEqualByComparingTo(new BigDecimal("30"));

      assertThat(output.getValueString("resultMessage")).contains("moved successfully");
   }



   /*******************************************************************************
    ** Test that a MOVE transaction is created.
    *******************************************************************************/
   @Test
   void testRun_validMove_createsMoveTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-002");
      Integer destLoc = insertLocation(warehouseId, null, "DST-002");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("50"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("sourceLocationId", sourceLoc);
      input.addValue("destinationLocationId", destLoc);
      input.addValue("itemId", itemId);
      input.addValue("quantityToMove", new BigDecimal("20"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryMoveStep().run(input, output);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.MOVE.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test that insufficient inventory throws exception.
    *******************************************************************************/
   @Test
   void testRun_insufficientInventory_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-003");
      Integer destLoc = insertLocation(warehouseId, null, "DST-003");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("10"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("sourceLocationId", sourceLoc);
      input.addValue("destinationLocationId", destLoc);
      input.addValue("itemId", itemId);
      input.addValue("quantityToMove", new BigDecimal("50"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new InventoryMoveStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Insufficient");
   }



   /*******************************************************************************
    ** Test that same source and destination throws exception.
    *******************************************************************************/
   @Test
   void testRun_sameSourceAndDest_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer loc = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, loc, new BigDecimal("100"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("sourceLocationId", loc);
      input.addValue("destinationLocationId", loc);
      input.addValue("itemId", itemId);
      input.addValue("quantityToMove", new BigDecimal("10"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new InventoryMoveStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("must be different");
   }
}
