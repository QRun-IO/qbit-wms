/*******************************************************************************
 ** Tests for WmsInvoiceLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInvoiceLineTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer invoiceId = insertInvoice(clientId);

      Integer lineId = new InsertAction().execute(new InsertInput(WmsInvoiceLine.TABLE_NAME)
            .withRecordEntity(new WmsInvoiceLine()
               .withInvoiceId(invoiceId)
               .withActivityTypeId(BillingActivityType.PICK_PER_UNIT.getPossibleValueId())
               .withDescription("Pick Per Unit")
               .withQuantity(new BigDecimal("100"))
               .withUnitRate(new BigDecimal("1.50"))
               .withLineTotal(new BigDecimal("150"))))
         .getRecords().get(0).getValueInteger("id");

      QRecord record = new GetAction().execute(new GetInput(WmsInvoiceLine.TABLE_NAME).withPrimaryKey(lineId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("invoiceId")).isEqualTo(invoiceId);
      assertThat(record.getValueBigDecimal("lineTotal").compareTo(new BigDecimal("150"))).isEqualTo(0);
   }
}
