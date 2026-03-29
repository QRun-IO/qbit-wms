/*******************************************************************************
 ** Unit tests for WmsAllocationRule entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsAllocationRuleTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsAllocationRule.TABLE_NAME).isEqualTo("wmsAllocationRule");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsAllocationRule entity = new WmsAllocationRule();
      WmsAllocationRule result = entity
         .withId(1)
         .withWarehouseId(10)
         .withStrategyId(1)
         .withPriority(5)
         .withIsActive(true);

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsAllocationRule original = new WmsAllocationRule()
         .withId(42)
         .withWarehouseId(10)
         .withStrategyId(1)
         .withPriority(3)
         .withIsActive(true);

      QRecord record = original.toQRecord();
      WmsAllocationRule restored = new WmsAllocationRule(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getStrategyId()).isEqualTo(1);
      assertThat(restored.getPriority()).isEqualTo(3);
      assertThat(restored.getIsActive()).isTrue();
   }



   @Test
   void testInsertViaHelper_defaultAllocationRule_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer ruleId = insertAllocationRule(warehouseId);
      assertThat(ruleId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsAllocationRule.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
