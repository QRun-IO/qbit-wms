/*******************************************************************************
 ** Backend step for GenerateInvoice.  Queries unbilled billing activities for
 ** the selected client within the billing period, groups by activity type,
 ** applies rates from the active rate card, creates an invoice with invoice
 ** lines, and marks activities as billed.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.billing.model.WmsBillingRate;
import com.kingsrook.qbits.wms.billing.model.WmsBillingRateCard;
import com.kingsrook.qbits.wms.billing.model.WmsInvoice;
import com.kingsrook.qbits.wms.billing.model.WmsInvoiceLine;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.BillingRateCardStatus;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class GenerateInvoiceStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(GenerateInvoiceStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer clientId = input.getValueInteger("clientId");
      LocalDate periodStart = input.getValueLocalDate("billingPeriodStart");
      LocalDate periodEnd = input.getValueLocalDate("billingPeriodEnd");

      if(clientId == null)
      {
         throw new QUserFacingException("Client is required.");
      }
      if(periodStart == null || periodEnd == null)
      {
         throw new QUserFacingException("Billing period start and end dates are required.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Query unbilled billing activities for this client in the period     //
      /////////////////////////////////////////////////////////////////////////
      Instant periodStartInstant = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();
      Instant periodEndInstant = periodEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

      QueryOutput activityQuery = new QueryAction().execute(new QueryInput(WmsBillingActivity.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId))
            .withCriteria(new QFilterCriteria("isBilled", QCriteriaOperator.EQUALS, false))
            .withCriteria(new QFilterCriteria("activityDate", QCriteriaOperator.GREATER_THAN_OR_EQUALS, periodStartInstant))
            .withCriteria(new QFilterCriteria("activityDate", QCriteriaOperator.LESS_THAN, periodEndInstant))));

      List<QRecord> activities = activityQuery.getRecords();

      if(activities.isEmpty())
      {
         output.addValue("resultMessage", "No unbilled activities found for the selected client and period.");
         output.addValue("invoiceId", null);
         output.addValue("invoiceTotal", "0.00");
         return;
      }

      /////////////////////////////////////////////////////////////////////////
      // Find the active rate card for this client                           //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput rateCardQuery = new QueryAction().execute(new QueryInput(WmsBillingRateCard.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId))
            .withCriteria(new QFilterCriteria("statusId", QCriteriaOperator.EQUALS, BillingRateCardStatus.ACTIVE.getPossibleValueId()))));

      Map<Integer, QRecord> ratesByActivityType = new LinkedHashMap<>();
      if(!rateCardQuery.getRecords().isEmpty())
      {
         Integer rateCardId = rateCardQuery.getRecords().get(0).getValueInteger("id");

         QueryOutput ratesQuery = new QueryAction().execute(new QueryInput(WmsBillingRate.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("rateCardId", QCriteriaOperator.EQUALS, rateCardId))));

         for(QRecord rate : ratesQuery.getRecords())
         {
            Integer activityTypeId = rate.getValueInteger("activityTypeId");
            if(activityTypeId != null)
            {
               ratesByActivityType.put(activityTypeId, rate);
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Group activities by activity type                                   //
      /////////////////////////////////////////////////////////////////////////
      Map<Integer, List<QRecord>> activitiesByType = new LinkedHashMap<>();
      for(QRecord activity : activities)
      {
         Integer activityTypeId = activity.getValueInteger("activityTypeId");
         activitiesByType.computeIfAbsent(activityTypeId, k -> new ArrayList<>()).add(activity);
      }

      /////////////////////////////////////////////////////////////////////////
      // Create invoice                                                      //
      /////////////////////////////////////////////////////////////////////////
      String invoiceNumber = "INV-" + clientId + "-" + periodStart.toString().replace("-", "");

      QRecord invoiceRecord = new InsertAction().execute(new InsertInput(WmsInvoice.TABLE_NAME).withRecord(new QRecord()
         .withValue("clientId", clientId)
         .withValue("invoiceNumber", invoiceNumber)
         .withValue("billingPeriodStart", periodStart)
         .withValue("billingPeriodEnd", periodEnd)
         .withValue("statusId", InvoiceStatus.DRAFT.getPossibleValueId())
         .withValue("generatedDate", Instant.now())
         .withValue("subtotal", BigDecimal.ZERO)
         .withValue("tax", BigDecimal.ZERO)
         .withValue("total", BigDecimal.ZERO))).getRecords().get(0);

      Integer invoiceId = invoiceRecord.getValueInteger("id");
      BigDecimal invoiceSubtotal = BigDecimal.ZERO;

      /////////////////////////////////////////////////////////////////////////
      // Create invoice lines for each activity type                         //
      /////////////////////////////////////////////////////////////////////////
      for(Map.Entry<Integer, List<QRecord>> entry : activitiesByType.entrySet())
      {
         Integer activityTypeId = entry.getKey();
         List<QRecord> typeActivities = entry.getValue();

         BigDecimal totalQuantity = BigDecimal.ZERO;
         for(QRecord activity : typeActivities)
         {
            BigDecimal qty = ValueUtils.getValueAsBigDecimal(activity.getValue("quantity"));
            if(qty != null)
            {
               totalQuantity = totalQuantity.add(qty);
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Look up rate for this activity type                             //
         /////////////////////////////////////////////////////////////////////
         BigDecimal unitRate = BigDecimal.ZERO;
         Integer rateId = null;
         QRecord rateRecord = ratesByActivityType.get(activityTypeId);
         if(rateRecord != null)
         {
            unitRate = ValueUtils.getValueAsBigDecimal(rateRecord.getValue("rate"));
            rateId = rateRecord.getValueInteger("id");
            if(unitRate == null)
            {
               unitRate = BigDecimal.ZERO;
            }
         }

         BigDecimal lineTotal = totalQuantity.multiply(unitRate);

         /////////////////////////////////////////////////////////////////////
         // Apply minimum charge if applicable                              //
         /////////////////////////////////////////////////////////////////////
         if(rateRecord != null)
         {
            BigDecimal minimumCharge = ValueUtils.getValueAsBigDecimal(rateRecord.getValue("minimumCharge"));
            if(minimumCharge != null && lineTotal.compareTo(minimumCharge) < 0)
            {
               lineTotal = minimumCharge;
            }
         }

         BillingActivityType activityType = BillingActivityType.getById(activityTypeId);
         String description = activityType != null ? activityType.getLabel() : "Activity Type " + activityTypeId;

         new InsertAction().execute(new InsertInput(WmsInvoiceLine.TABLE_NAME).withRecord(new QRecord()
            .withValue("invoiceId", invoiceId)
            .withValue("activityTypeId", activityTypeId)
            .withValue("description", description)
            .withValue("quantity", totalQuantity)
            .withValue("unitRate", unitRate)
            .withValue("lineTotal", lineTotal)));

         invoiceSubtotal = invoiceSubtotal.add(lineTotal);

         /////////////////////////////////////////////////////////////////////
         // Mark activities as billed and link to invoice                   //
         /////////////////////////////////////////////////////////////////////
         for(QRecord activity : typeActivities)
         {
            new UpdateAction().execute(new UpdateInput(WmsBillingActivity.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", activity.getValueInteger("id"))
               .withValue("isBilled", true)
               .withValue("invoiceId", invoiceId)
               .withValue("rateId", rateId)
               .withValue("unitRate", unitRate)
               .withValue("totalCharge", ValueUtils.getValueAsBigDecimal(activity.getValue("quantity")) != null
                  ? ValueUtils.getValueAsBigDecimal(activity.getValue("quantity")).multiply(unitRate)
                  : BigDecimal.ZERO)));
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Update invoice totals                                               //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsInvoice.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", invoiceId)
         .withValue("subtotal", invoiceSubtotal)
         .withValue("total", invoiceSubtotal)));

      LOG.info("Invoice generated", logPair("invoiceId", invoiceId), logPair("total", invoiceSubtotal), logPair("activityCount", activities.size()));

      output.addValue("resultMessage", "Invoice generated successfully with " + activitiesByType.size() + " line items covering " + activities.size() + " activities.");
      output.addValue("invoiceId", invoiceId);
      output.addValue("invoiceTotal", invoiceSubtotal.toPlainString());
   }
}
