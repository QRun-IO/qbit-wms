/*******************************************************************************
 ** Interface for accounting system integrations.  Implement this interface
 ** to sync invoices to an external accounting system (e.g., QuickBooks,
 ** Xero, NetSuite).
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing;


import java.math.BigDecimal;


public interface AccountingAdapter
{

   /*******************************************************************************
    ** Sync an invoice to the external accounting system and return the external
    ** reference ID assigned by that system.
    **
    ** @param invoiceId the WMS invoice primary key
    ** @param invoiceNumber the human-readable invoice number
    ** @param total the invoice total amount
    ** @return the external ID assigned by the accounting system
    *******************************************************************************/
   String syncInvoice(Integer invoiceId, String invoiceNumber, BigDecimal total);
}
