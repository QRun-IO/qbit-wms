/*******************************************************************************
 ** Unit tests for {@link CompleteTaskStep} inner classes.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsInventoryTransaction;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CompleteTaskStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test ValidateSourceStep transitions to IN_PROGRESS.
    *******************************************************************************/
   @Test
   void testValidateSourceStep_validBarcode_transitionsToInProgress() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-BC-001");

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withSourceLocationId(sourceLoc)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedSourceBarcode", "SRC-BC-001");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CompleteTaskStep.ValidateSourceStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.IN_PROGRESS.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test ValidateSourceStep throws on mismatched barcode.
    *******************************************************************************/
   @Test
   void testValidateSourceStep_wrongBarcode_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-BC-002");

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.ASSIGNED.getId())
         .withPriority(5)
         .withSourceLocationId(sourceLoc)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedSourceBarcode", "WRONG-BARCODE");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CompleteTaskStep.ValidateSourceStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("does not match");
   }



   /*******************************************************************************
    ** Test ValidateDestinationStep with correct barcode succeeds.
    *******************************************************************************/
   @Test
   void testValidateDestinationStep_validBarcode_succeeds() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer destLoc = insertLocation(warehouseId, null, "DST-BC-001");

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withDestinationLocationId(destLoc)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedDestinationBarcode", "DST-BC-001");
      RunBackendStepOutput output = new RunBackendStepOutput();

      // Should not throw
      new CompleteTaskStep.ValidateDestinationStep().run(input, output);
   }



   /*******************************************************************************
    ** Test ExecuteCompletionStep marks task COMPLETED and fires dispatcher.
    *******************************************************************************/
   @Test
   void testExecuteCompletionStep_moveTask_completedAndInventoryUpdated() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-COMP-001");
      Integer destLoc = insertLocation(warehouseId, null, "DST-COMP-001");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("50"));

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withItemId(itemId)
         .withSourceLocationId(sourceLoc)
         .withDestinationLocationId(destLoc)
         .withQuantityRequested(new BigDecimal("20"))))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("quantityCompleted", new BigDecimal("20"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CompleteTaskStep.ExecuteCompletionStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.COMPLETED.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("completed successfully");

      ///////////////////////////////////////////////////////////////////
      // Verify inventory transaction was created                      //
      ///////////////////////////////////////////////////////////////////
      QueryOutput txnQuery = new QueryAction().execute(new QueryInput(WmsInventoryTransaction.TABLE_NAME));
      assertThat(txnQuery.getRecords()).isNotEmpty();
   }



   /*******************************************************************************
    ** Test ExecuteCompletionStep with short quantity sets SHORT status.
    *******************************************************************************/
   @Test
   void testExecuteCompletionStep_shortQuantity_markedShort() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer sourceLoc = insertLocation(warehouseId, null, "SRC-SHORT-001");
      Integer destLoc = insertLocation(warehouseId, null, "DST-SHORT-001");

      insertInventory(warehouseId, itemId, sourceLoc, new BigDecimal("50"));

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withItemId(itemId)
         .withSourceLocationId(sourceLoc)
         .withDestinationLocationId(destLoc)
         .withQuantityRequested(new BigDecimal("20"))))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("quantityCompleted", new BigDecimal("15"));
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CompleteTaskStep.ExecuteCompletionStep().run(input, output);

      QRecord task = new GetAction().executeForRecord(new GetInput(WmsTask.TABLE_NAME).withPrimaryKey(taskId));
      assertThat(task.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.SHORT.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test that null taskId throws QUserFacingException.
    *******************************************************************************/
   @Test
   void testValidateSourceStep_nullTaskId_throwsException()
   {
      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CompleteTaskStep.ValidateSourceStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Task ID is required");
   }



   /*******************************************************************************
    ** Test ValidateItemStep with a matching barcode.
    *******************************************************************************/
   @Test
   void testValidateItemStep_validBarcode_succeeds() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem("ITEM-SKU", "Test Item");

      ///////////////////////////////////////////////////////////////////
      // Set barcodeUpc on the item                                    //
      ///////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput("wmsItem").withRecord(new QRecord()
         .withValue("id", itemId)
         .withValue("barcodeUpc", "UPC-12345")));

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withItemId(itemId)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedItemBarcode", "UPC-12345");
      RunBackendStepOutput output = new RunBackendStepOutput();

      new CompleteTaskStep.ValidateItemStep().run(input, output);
      // Should not throw
   }



   /*******************************************************************************
    ** Test ValidateItemStep with wrong barcode throws exception.
    *******************************************************************************/
   @Test
   void testValidateItemStep_wrongBarcode_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem("ITEM-SKU2", "Test Item 2");

      new UpdateAction().execute(new UpdateInput("wmsItem").withRecord(new QRecord()
         .withValue("id", itemId)
         .withValue("barcodeUpc", "UPC-99999")));

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withItemId(itemId)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      ///////////////////////////////////////////////////////////////////
      // Create a different item with a different barcode              //
      ///////////////////////////////////////////////////////////////////
      Integer otherItemId = insertItem("OTHER-SKU", "Other Item");
      new UpdateAction().execute(new UpdateInput("wmsItem").withRecord(new QRecord()
         .withValue("id", otherItemId)
         .withValue("barcodeUpc", "UPC-OTHER")));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedItemBarcode", "UPC-OTHER");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CompleteTaskStep.ValidateItemStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("does not match");
   }



   /*******************************************************************************
    ** Test ValidateItemStep with unknown barcode throws exception.
    *******************************************************************************/
   @Test
   void testValidateItemStep_unknownBarcode_throwsException() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();

      QRecord taskRecord = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecordEntity(new WmsTask()
         .withWarehouseId(warehouseId)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(5)
         .withItemId(itemId)))
      .getRecords().get(0);

      Integer taskId = taskRecord.getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("taskId", taskId);
      input.addValue("scannedItemBarcode", "DOES-NOT-EXIST");
      RunBackendStepOutput output = new RunBackendStepOutput();

      assertThatThrownBy(() -> new CompleteTaskStep.ValidateItemStep().run(input, output))
         .isInstanceOf(QUserFacingException.class)
         .hasMessageContaining("Item not found");
   }
}
