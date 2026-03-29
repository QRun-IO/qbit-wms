/*******************************************************************************
 ** Backend step for ExpirationAlertCheck.  Finds inventory with expiration
 ** dates within a configurable threshold number of days from today.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class ExpirationAlertCheckStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(ExpirationAlertCheckStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer thresholdDays = input.getValueInteger("thresholdDays");

      if(thresholdDays == null || thresholdDays < 0)
      {
         throw new QUserFacingException("Threshold days must be a positive number.");
      }

      LocalDate thresholdDate = LocalDate.now().plusDays(thresholdDays);

      /////////////////////////////////////////////////////////////////////////
      // Query inventory with expiration date within threshold               //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput inventoryQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("expirationDate", QCriteriaOperator.IS_NOT_BLANK))
            .withCriteria(new QFilterCriteria("expirationDate", QCriteriaOperator.LESS_THAN_OR_EQUALS, thresholdDate))
            .withCriteria(new QFilterCriteria("quantityOnHand", QCriteriaOperator.GREATER_THAN, 0))));

      List<QRecord> expiringInventory = inventoryQuery.getRecords();
      int alertCount = expiringInventory.size();

      for(QRecord inv : expiringInventory)
      {
         LOG.warn("Expiration alert",
            logPair("inventoryId", inv.getValueInteger("id")),
            logPair("itemId", inv.getValueInteger("itemId")),
            logPair("locationId", inv.getValueInteger("locationId")),
            logPair("expirationDate", inv.getValue("expirationDate")),
            logPair("quantityOnHand", inv.getValue("quantityOnHand")));
      }

      output.addValue("resultMessage", "Expiration alert check complete. " + alertCount + " inventory records expiring within " + thresholdDays + " days.");
      output.addValue("alertCount", alertCount);
   }
}
