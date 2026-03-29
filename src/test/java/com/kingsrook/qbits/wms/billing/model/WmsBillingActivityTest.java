/*******************************************************************************
 ** Tests for WmsBillingActivity entity.
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


class WmsBillingActivityTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer clientId = insertClient();
      Integer activityId = insertBillingActivity(warehouseId, clientId,
         BillingActivityType.PICK_PER_UNIT.getPossibleValueId(), new BigDecimal("25"));

      QRecord record = new GetAction().execute(new GetInput(WmsBillingActivity.TABLE_NAME).withPrimaryKey(activityId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("clientId")).isEqualTo(clientId);
      assertThat(record.getValueBigDecimal("quantity").compareTo(new BigDecimal("25"))).isEqualTo(0);
      assertThat(record.getValueBoolean("isBilled")).isFalse();
   }
}
