/*******************************************************************************
 ** Tests for WmsInvoice entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.InvoiceStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInvoiceTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer invoiceId = insertInvoice(clientId);

      QRecord record = new GetAction().execute(new GetInput(WmsInvoice.TABLE_NAME).withPrimaryKey(invoiceId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueInteger("statusId")).isEqualTo(InvoiceStatus.DRAFT.getPossibleValueId());
   }
}
