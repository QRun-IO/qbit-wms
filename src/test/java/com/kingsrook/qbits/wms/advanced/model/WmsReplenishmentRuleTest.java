/*******************************************************************************
 ** Tests for WmsReplenishmentRule entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.model;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReplenishmentRuleTest extends BaseTest
{

   @Test
   void testInsertAndRetrieve() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer ruleId = insertReplenishmentRule(warehouseId, itemId, locationId, 10, 50);

      QRecord record = new GetAction().execute(new GetInput(WmsReplenishmentRule.TABLE_NAME).withPrimaryKey(ruleId)).getRecord();
      assertThat(record).isNotNull();
      assertThat(record.getValueInteger("warehouseId")).isEqualTo(warehouseId);
      assertThat(record.getValueInteger("itemId")).isEqualTo(itemId);
      assertThat(record.getValueInteger("pickLocationId")).isEqualTo(locationId);
      assertThat(record.getValueInteger("minQuantity")).isEqualTo(10);
      assertThat(record.getValueInteger("maxQuantity")).isEqualTo(50);
      assertThat(record.getValueBoolean("isActive")).isTrue();
   }
}
