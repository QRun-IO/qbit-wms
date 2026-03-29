/*******************************************************************************
 ** Default (placeholder) implementation of CarrierAdapter.  Returns synthetic
 ** tracking numbers using System.nanoTime().  Replace with a real carrier
 ** integration for production use.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class DefaultCarrierAdapter implements CarrierAdapter
{
   private static final QLogger LOG = QLogger.getLogger(DefaultCarrierAdapter.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String generateLabel(String carrier, String serviceLevel, String shipFromAddress, String shipToAddress, BigDecimal weightLbs)
   {
      String trackingNumber = "TRK-" + System.nanoTime();
      LOG.info("Default carrier adapter generated placeholder label",
         logPair("carrier", carrier),
         logPair("serviceLevel", serviceLevel),
         logPair("trackingNumber", trackingNumber));
      return (trackingNumber);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getRate(String carrier, String serviceLevel, BigDecimal weightLbs)
   {
      return (BigDecimal.ZERO);
   }
}
