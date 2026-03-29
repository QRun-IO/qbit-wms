/*******************************************************************************
 ** Default (placeholder) implementation of AccountingAdapter.  Returns a
 ** synthetic external ID.  Replace with a real accounting integration for
 ** production use.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class DefaultAccountingAdapter implements AccountingAdapter
{
   private static final QLogger LOG = QLogger.getLogger(DefaultAccountingAdapter.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String syncInvoice(Integer invoiceId, String invoiceNumber, BigDecimal total)
   {
      String externalId = "EXT-" + System.nanoTime();
      LOG.info("Default accounting adapter generated placeholder external ID",
         logPair("invoiceId", invoiceId),
         logPair("invoiceNumber", invoiceNumber),
         logPair("externalId", externalId));
      return (externalId);
   }
}
