/*******************************************************************************
 ** Tests for WmsBillingRateCard entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.model;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsBillingRateCardTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer clientId = insertClient();
      Integer rateCardId = insertBillingRateCard(clientId, "Test Rate Card");

      QRecord record = new GetAction().execute(new GetInput(WmsBillingRateCard.TABLE_NAME).withPrimaryKey(rateCardId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueString("name")).isEqualTo("Test Rate Card");
   }
}
