/*******************************************************************************
 ** Unit tests for {@link InventoryHoldStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.HoldType;
import com.kingsrook.qbits.wms.core.enums.InventoryStatus;
import com.kingsrook.qbits.wms.core.enums.TransactionType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class InventoryHoldStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that placing hold creates a hold record.
    *******************************************************************************/
   @Test
   void testRun_validHold_createsHoldRecord() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("locationId", locationId);
      input.addValue("holdTypeId", HoldType.QC.getId());
      input.addValue("holdReason", "Quality check required");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryHoldStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify hold record was created                                //
      ///////////////////////////////////////////////////////////////////
      QueryOutput holdQuery = new QueryAction().execute(new QueryInput(WmsInventoryHold.TABLE_NAME));
      assertThat(holdQuery.getRecords()).hasSize(1);
      QRecord hold = holdQuery.getRecords().get(0);
      assertThat(hold.getValueString("status")).isEqualTo("ACTIVE");
      assertThat(hold.getValueString("reason")).isEqualTo("Quality check required");
   }



   /*******************************************************************************
    ** Test that inventory status is updated to ON_HOLD.
    *******************************************************************************/
   @Test
   void testRun_validHold_inventoryStatusOnHold() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("50"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("locationId", locationId);
      input.addValue("holdTypeId", HoldType.DAMAGE.getId());
      input.addValue("holdReason", "Damaged goods");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryHoldStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify inventory status changed to ON_HOLD                    //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueInteger("inventoryStatusId")).isEqualTo(InventoryStatus.ON_HOLD.getPossibleValueId());
      assertThat(inv.getValueBigDecimal("quantityOnHold")).isEqualByComparingTo(new BigDecimal("50"));
      assertThat(inv.getValueBigDecimal("quantityAvailable")).isEqualByComparingTo(BigDecimal.ZERO);
   }



   /*******************************************************************************
    ** Test that a HOLD transaction is created.
    *******************************************************************************/
   @Test
   void testRun_validHold_createsHoldTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("25"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("locationId", locationId);
      input.addValue("holdTypeId", HoldType.RECALL.getId());
      input.addValue("holdReason", "Product recall");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryHoldStep().run(input, output);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.HOLD.getPossibleValueId());
      assertThat(txn.getValueBigDecimal("quantity")).isEqualByComparingTo(new BigDecimal("25"));
   }



   /*******************************************************************************
    ** Test that no matching inventory throws exception.
    *******************************************************************************/
   @Test
   void testRun_noMatchingInventory_throwsException() throws QException
   {
      Integer itemId = insertItem();

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("itemId", itemId);
      input.addValue("holdTypeId", HoldType.QC.getId());
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new InventoryHoldStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("No inventory found");
   }
}
