/*******************************************************************************
 ** Tests for WmsBillingRate entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsBillingRateTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer rateCardId = insertBillingRateCard(clientId);
      Integer rateId = insertBillingRate(rateCardId, BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("1.50"));

      QRecord record = new GetAction().execute(new GetInput(WmsBillingRate.TABLE_NAME).withPrimaryKey(rateId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("rateCardId")).isEqualTo(rateCardId);
      assertThat(record.getValueBigDecimal("rate").compareTo(new BigDecimal("1.50"))).isEqualTo(0);
   }
}
