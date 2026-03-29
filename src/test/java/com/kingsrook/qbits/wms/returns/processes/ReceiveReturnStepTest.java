/*******************************************************************************
 ** Unit tests for ReceiveReturnStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.ReturnAuthorizationStatus;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnReceiptLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ReceiveReturnStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that receiving a return creates receipt lines and updates RMA status
    ** to RECEIVED.
    *******************************************************************************/
   @Test
   void testRun_receiveReturn_createsReceiptAndUpdatesStatus() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer raId = insertReturnAuthorization(warehouseId, orderId, null);
      insertReturnAuthorizationLine(raId, itemId, 5);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("returnAuthorizationId", raId);
      input.addValue("receivedBy", "Warehouse Worker");
      input.addValue("carrierName", "UPS");
      input.addValue("trackingNumber", "1Z999");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new ReceiveReturnStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA status is RECEIVED                                       //
      /////////////////////////////////////////////////////////////////////////
      QRecord ra = new GetAction().execute(new GetInput(WmsReturnAuthorization.TABLE_NAME).withPrimaryKey(raId)).getRecord();
      assertThat(ra.getValueInteger("statusId")).isEqualTo(ReturnAuthorizationStatus.RECEIVED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify receipt lines were created                                   //
      /////////////////////////////////////////////////////////////////////////
      Integer receiptId = output.getValueInteger("returnReceiptId");
      assertThat(receiptId).isNotNull();

      List<QRecord> receiptLines = new QueryAction().execute(new QueryInput(WmsReturnReceiptLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("returnReceiptId", QCriteriaOperator.EQUALS, receiptId)))).getRecords();

      assertThat(receiptLines).hasSize(1);
      assertThat(receiptLines.get(0).getValueInteger("quantityReceived")).isEqualTo(5);
   }
}
