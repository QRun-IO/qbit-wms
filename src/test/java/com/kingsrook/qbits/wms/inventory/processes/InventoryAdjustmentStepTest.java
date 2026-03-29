/*******************************************************************************
 ** Unit tests for {@link InventoryAdjustmentStep}.
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
import com.kingsrook.qbits.wms.core.enums.AdjustmentReasonCode;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class InventoryAdjustmentStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test positive adjustment creates transaction and updates inventory.
    *******************************************************************************/
   @Test
   void testRun_positiveAdjustment_quantityIncreased() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("50"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("locationId", locationId);
      input.addValue("itemId", itemId);
      input.addValue("newQuantity", new BigDecimal("75"));
      input.addValue("reasonCodeId", AdjustmentReasonCode.FOUND_STOCK.getId());
      input.addValue("adjustmentNotes", "Found extra stock");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryAdjustmentStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify inventory was updated                                  //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("75"));

      ///////////////////////////////////////////////////////////////////
      // Verify transaction was created BEFORE inventory update        //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueBigDecimal("quantity")).isEqualByComparingTo(new BigDecimal("25"));

      assertThat(output.getValueString("resultMessage")).contains("adjusted successfully");
   }



   /*******************************************************************************
    ** Test negative adjustment (reducing quantity).
    *******************************************************************************/
   @Test
   void testRun_negativeAdjustment_quantityDecreased() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("locationId", locationId);
      input.addValue("itemId", itemId);
      input.addValue("newQuantity", new BigDecimal("80"));
      input.addValue("reasonCodeId", AdjustmentReasonCode.DAMAGE.getId());
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryAdjustmentStep().run(input, output);

      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueBigDecimal("quantityOnHand")).isEqualByComparingTo(new BigDecimal("80"));

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueBigDecimal("quantity")).isEqualByComparingTo(new BigDecimal("-20"));
   }



   /*******************************************************************************
    ** Test adjustment with no existing inventory still creates the transaction
    ** record.  The insert of a new inventory record may fail when warehouseId
    ** is not resolvable from existing records; the transaction is still logged.
    *******************************************************************************/
   @Test
   void testRun_noExistingInventory_createsTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("locationId", locationId);
      input.addValue("itemId", itemId);
      input.addValue("newQuantity", new BigDecimal("30"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryAdjustmentStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Transaction is always created (perpetual inventory principle) //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      assertThat(txnQuery.getRecords().get(0).getValueBigDecimal("quantity"))
         .isEqualByComparingTo(new BigDecimal("30"));

      assertThat(output.getValueString("resultMessage")).contains("adjusted successfully");
   }



   /*******************************************************************************
    ** Test that missing required fields throws exception.
    *******************************************************************************/
   @Test
   void testRun_missingFields_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("newQuantity", new BigDecimal("10"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new InventoryAdjustmentStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("required");
   }
}
