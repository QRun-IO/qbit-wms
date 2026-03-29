/*******************************************************************************
 ** Unit tests for DispositionReturnStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.Disposition;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceipt;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class DispositionReturnStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that a RESTOCK disposition creates a RETURN_PUTAWAY task and updates
    ** the RMA status to DISPOSITIONED.
    *******************************************************************************/
   @Test
   void testRun_restockDisposition_createsReturnPutawayTask() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer locationId = insertLocation(warehouseId);
      Integer raId = insertReturnAuthorization(warehouseId, orderId, null);

      /////////////////////////////////////////////////////////////////////////
      // Create return receipt and receipt line                               //
      /////////////////////////////////////////////////////////////////////////
      Integer receiptId = new InsertAction().execute(new InsertInput(WmsReturnReceipt.TABLE_NAME).withRecord(new QRecord()
         .withValue("returnAuthorizationId", raId)
         .withValue("receiptNumber", "RRCPT-TEST")
         .withValue("receivedBy", "TestWorker"))).getRecords().get(0).getValueInteger("id");

      Integer receiptLineId = new InsertAction().execute(new InsertInput(WmsReturnReceiptLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("returnReceiptId", receiptId)
         .withValue("itemId", itemId)
         .withValue("quantityReceived", 5))).getRecords().get(0).getValueInteger("id");

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("returnReceiptLineId", receiptLineId);
      input.addValue("dispositionId", Disposition.RESTOCK.getPossibleValueId());
      input.addValue("dispositionLocationId", locationId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new DispositionReturnStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify RETURN_PUTAWAY task was created                              //
      /////////////////////////////////////////////////////////////////////////
      List<QRecord> tasks = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.RETURN_PUTAWAY.getPossibleValueId())))).getRecords();

      assertThat(tasks).hasSize(1);
      assertThat(tasks.get(0).getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      assertThat(tasks.get(0).getValueInteger("itemId")).isEqualTo(itemId);

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA status updated to DISPOSITIONED                          //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
      assertThat(ra.getValueInteger("statusId")).isEqualTo(ReturnAuthorizationStatus.DISPOSITIONED.getPossibleValueId());
   }
}
