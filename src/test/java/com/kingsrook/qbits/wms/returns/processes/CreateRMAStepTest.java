/*******************************************************************************
 ** Unit tests for CreateRMAStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.ReturnReasonCode;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorization;
import com.kingsrook.qbits.wms.returns.model.WmsReturnAuthorizationLine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class CreateRMAStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that creating an RMA from a shipped order creates an RMA with lines
    ** matching the order's shipped quantities.
    *******************************************************************************/
   @Test
   void testRun_createRmaFromOrder_createsRmaWithLines() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Mark the order line as shipped                                      //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrderLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", lineId)
         .withValue("quantityShipped", 10)));

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      input.addValue("orderId", orderId);
      input.addValue("customerName", "Test Customer");
      input.addValue("reasonCodeId", ReturnReasonCode.DEFECTIVE.getPossibleValueId());

      RunBackendStepOutput output = new RunBackendStepOutput();
      new CreateRMAStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify RMA was created                                              //
      /////////////////////////////////////////////////////////////////////////
      Integer raId = output.getValueInteger("returnAuthorizationId");
      assertThat(raId).isNotNull();

      List<QRecord> raLines = new QueryAction().execute(new QueryInput(WmsReturnAuthorizationLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("returnAuthorizationId", QCriteriaOperator.EQUALS, raId)))).getRecords();

      assertThat(raLines).hasSize(1);
      assertThat(raLines.get(0).getValueInteger("quantityAuthorized")).isEqualTo(10);
      assertThat(output.getValueString("resultMessage")).contains("RMA created");
   }
}
