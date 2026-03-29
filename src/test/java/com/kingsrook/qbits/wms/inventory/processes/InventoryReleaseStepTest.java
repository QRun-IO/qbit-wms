/*******************************************************************************
 ** Unit tests for {@link InventoryReleaseStep}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
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


class InventoryReleaseStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test releasing a hold restores AVAILABLE status.
    *******************************************************************************/
   @Test
   void testRun_activeHold_releasedAndAvailable() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // Create ON_HOLD inventory                                      //
      ///////////////////////////////////////////////////////////////////
      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("50"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("inventoryStatusId", InventoryStatus.ON_HOLD.getPossibleValueId())
         .withValue("quantityOnHold", new BigDecimal("50"))
         .withValue("quantityAvailable", BigDecimal.ZERO)));

      ///////////////////////////////////////////////////////////////////
      // Create the hold record                                        //
      ///////////////////////////////////////////////////////////////////
      QRecord holdRecord = new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("locationId", locationId)
         .withValue("holdTypeId", HoldType.QC.getId())
         .withValue("reason", "QC hold")
         .withValue("placedBy", "admin")
         .withValue("placedDate", Instant.now())
         .withValue("status", "ACTIVE")))
      .getRecords().get(0);

      Integer holdId = holdRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("holdId", holdId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryReleaseStep().run(input, output);

      ///////////////////////////////////////////////////////////////////
      // Verify hold is RELEASED                                       //
      ///////////////////////////////////////////////////////////////////
      QueryOutput holdQuery = new QueryAction().execute(new QueryInput(WmsInventoryHold.TABLE_NAME));
      assertThat(holdQuery.getRecords().get(0).getValueString("status")).isEqualTo("RELEASED");

      ///////////////////////////////////////////////////////////////////
      // Verify inventory is AVAILABLE again                           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput invQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME));
      QRecord inv = invQuery.getRecords().get(0);
      assertThat(inv.getValueInteger("inventoryStatusId")).isEqualTo(InventoryStatus.AVAILABLE.getPossibleValueId());
      assertThat(inv.getValueBigDecimal("quantityOnHold")).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(inv.getValueBigDecimal("quantityAvailable")).isEqualByComparingTo(new BigDecimal("50"));

      assertThat(output.getValueString("resultMessage")).contains("released successfully");
   }



   /*******************************************************************************
    ** Test that a RELEASE transaction is created.
    *******************************************************************************/
   @Test
   void testRun_activeHold_createsReleaseTransaction() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);

      Integer invId = insertInventory(warehouseId, itemId, locationId, new BigDecimal("30"));
      new UpdateAction().execute(new UpdateInput(WmsInventory.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invId)
         .withValue("inventoryStatusId", InventoryStatus.ON_HOLD.getPossibleValueId())
         .withValue("quantityOnHold", new BigDecimal("30"))
         .withValue("quantityAvailable", BigDecimal.ZERO)));

      QRecord holdRecord = new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("locationId", locationId)
         .withValue("holdTypeId", HoldType.QC.getId())
         .withValue("reason", "QC")
         .withValue("placedDate", Instant.now())
         .withValue("status", "ACTIVE")))
      .getRecords().get(0);

      Integer holdId = holdRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("holdId", holdId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      new InventoryReleaseStep().run(input, output);

      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).hasSize(1);
      QRecord txn = txnQuery.getRecords().get(0);
      assertThat(txn.getValueInteger("transactionTypeId")).isEqualTo(TransactionType.RELEASE.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test that releasing a non-ACTIVE hold throws exception.
    *******************************************************************************/
   @Test
   void testRun_releasedHold_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();

      QRecord holdRecord = new InsertAction().execute(new InsertInput(WmsInventoryHold.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("itemId", itemId)
         .withValue("holdTypeId", HoldType.QC.getId())
         .withValue("status", "RELEASED")))
      .getRecords().get(0);

      Integer holdId = holdRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("holdId", holdId);
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new InventoryReleaseStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Only ACTIVE holds");
   }
}
