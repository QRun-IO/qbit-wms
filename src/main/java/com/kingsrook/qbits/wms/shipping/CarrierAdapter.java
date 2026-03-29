/*******************************************************************************
 ** Interface for carrier integrations.  Provides label generation and rate
 ** lookup methods.  Implement this interface to integrate with a real carrier
 ** API (e.g., EasyPost, ShipEngine, UPS, FedEx).
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping;


import java.math.BigDecimal;


public interface CarrierAdapter
{

   /*******************************************************************************
    ** Generate a shipping label and return the tracking number.
    **
    ** @param carrier the carrier code (e.g., "UPS", "FEDEX")
    ** @param serviceLevel the service level (e.g., "GROUND", "NEXT_DAY_AIR")
    ** @param shipFromAddress origin address
    ** @param shipToAddress destination address
    ** @param weightLbs package weight in pounds
    ** @return the tracking number assigned by the carrier
    *******************************************************************************/
   String generateLabel(String carrier, String serviceLevel, String shipFromAddress, String shipToAddress, BigDecimal weightLbs);



   /*******************************************************************************
    ** Retrieve a shipping rate for the given carrier and service level.
    **
    ** @param carrier the carrier code
    ** @param serviceLevel the service level
    ** @param weightLbs package weight in pounds
    ** @return the rate in the base currency
    *******************************************************************************/
   BigDecimal getRate(String carrier, String serviceLevel, BigDecimal weightLbs);
}
