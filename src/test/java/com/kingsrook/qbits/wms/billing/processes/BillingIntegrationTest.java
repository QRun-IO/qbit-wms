/*******************************************************************************
 ** Integration test for billing workflow: create rate card -> perform billable
 ** operations -> generate invoice -> verify line items and totals.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import java.math.BigDecimal;
import java.time.LocalDate;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.billing.model.WmsInvoice;
import com.kingsrook.qbits.wms.billing.model.WmsInvoiceLine;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class BillingIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** End-to-end billing test: create rate card, billing activities, generate
    ** invoice, verify totals, then sync to accounting.
    *******************************************************************************/
   @Test
   void testEndToEnd_createRateCard_generateInvoice_syncToAccounting() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer clientId = insertClient();

      /////////////////////////////////////////////////////////////////////////
      // Create rate card with rates                                         //
      /////////////////////////////////////////////////////////////////////////
      Integer rateCardId = insertBillingRateCard(clientId, "Client Rate Card");
      insertBillingRate(rateCardId, BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("0.50"));
      insertBillingRate(rateCardId, BillingActivityType.STORAGE_PER_BIN_DAY.getPossibleValueId(), new BigDecimal("2.00"));

      /////////////////////////////////////////////////////////////////////////
      // Create billing activities                                           //
      /////////////////////////////////////////////////////////////////////////
      insertBillingActivity(warehouseId, clientId,
         BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("100"));
      insertBillingActivity(warehouseId, clientId,
         BillingActivityType.STORAGE_PER_BIN_DAY.getPossibleValueId(), new BigDecimal("10"));

      /////////////////////////////////////////////////////////////////////////
      // Generate invoice                                                    //
      /////////////////////////////////////////////////////////////////////////
      RunProcessInput processInput = new RunProcessInput();
      processInput.setProcessName(GenerateInvoiceProcessMetaDataProducer.NAME);
      processInput.addValue("clientId", clientId);
      processInput.addValue("billingPeriodStart", LocalDate.now().minusDays(1));
      processInput.addValue("billingPeriodEnd", LocalDate.now().plusDays(1));
      processInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      RunProcessOutput processOutput = new RunProcessAction().execute(processInput);

      Integer invoiceId = ValueUtils.getValueAsInteger(processOutput.getValues().get("invoiceId"));
      assertThat(invoiceId).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify invoice                                                      //
      /////////////////////////////////////////////////////////////////////////
      QRecord invoice = new GetAction().execute(new GetInput(WmsInvoice.TABLE_NAME).withPrimaryKey(invoiceId)).getRecord();
      assertThat(invoice).isNotNull();
      assertThat(invoice.getValueInteger("statusId")).isEqualTo(InvoiceStatus.DRAFT.getPossibleValueId());

      // Pick: 100 * 0.50 = 50.00, Storage: 10 * 2.00 = 20.00, Total = 70.00
      BigDecimal expectedTotal = new BigDecimal("70.00");
      assertThat(invoice.getValueBigDecimal("total").compareTo(expectedTotal)).isEqualTo(0);

      /////////////////////////////////////////////////////////////////////////
      // Verify invoice lines                                                //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsInvoiceLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("invoiceId", QCriteriaOperator.EQUALS, invoiceId))));
      assertThat(lineQuery.getRecords()).hasSize(2);

      /////////////////////////////////////////////////////////////////////////
      // Verify activities marked as billed                                  //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput activityQuery = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId))));
      for(QRecord activity : activityQuery.getRecords())
      {
         assertThat(activity.getValueBoolean("isBilled")).isTrue();
         assertThat(activity.getValueInteger("invoiceId")).isEqualTo(invoiceId);
      }

      /////////////////////////////////////////////////////////////////////////
      // Sync invoice to accounting                                          //
      /////////////////////////////////////////////////////////////////////////
      RunProcessInput syncInput = new RunProcessInput();
      syncInput.setProcessName(SyncInvoiceToAccountingProcessMetaDataProducer.NAME);
      syncInput.addValue("invoiceId", invoiceId);
      syncInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      new RunProcessAction().execute(syncInput);

      /////////////////////////////////////////////////////////////////////////
      // Verify invoice status is SENT                                       //
      /////////////////////////////////////////////////////////////////////////
      QRecord sentInvoice = new GetAction().execute(new GetInput(WmsInvoice.TABLE_NAME).withPrimaryKey(invoiceId)).getRecord();
      assertThat(sentInvoice.getValueInteger("statusId")).isEqualTo(InvoiceStatus.SENT.getPossibleValueId());
      assertThat(sentInvoice.getValue("sentDate")).isNotNull();
   }
}
