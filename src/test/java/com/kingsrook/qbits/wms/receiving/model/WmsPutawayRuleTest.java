/*******************************************************************************
 ** Unit tests for WmsPutawayRule entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsPutawayRuleTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsPutawayRule.TABLE_NAME).isEqualTo("wmsPutawayRule");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsPutawayRule entity = new WmsPutawayRule();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getRuleName()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsPutawayRule entity = new WmsPutawayRule();
      WmsPutawayRule result = entity
         .withId(1)
         .withWarehouseId(10)
         .withClientId(20)
         .withRuleName("Cold Storage Rule")
         .withPriority(1)
         .withZoneTypeMatch(3)
         .withItemCategoryMatch(5)
         .withVelocityClassMatch(2)
         .withStorageRequirementsMatch(1)
         .withTargetZoneId(100)
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test all fields round-trip through QRecord.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsPutawayRule original = new WmsPutawayRule()
         .withId(42)
         .withWarehouseId(10)
         .withClientId(20)
         .withRuleName("Test Rule")
         .withPriority(3)
         .withZoneTypeMatch(1)
         .withItemCategoryMatch(2)
         .withVelocityClassMatch(3)
         .withStorageRequirementsMatch(4)
         .withTargetZoneId(100)
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsPutawayRule restored = new WmsPutawayRule(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getClientId()).isEqualTo(20);
      assertThat(restored.getRuleName()).isEqualTo("Test Rule");
      assertThat(restored.getPriority()).isEqualTo(3);
      assertThat(restored.getTargetZoneId()).isEqualTo(100);
      assertThat(restored.getIsActive()).isTrue();
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsPutawayRule entity = new WmsPutawayRule(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getRuleName()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultRule_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer zoneId = insertZone(warehouseId);
      Integer ruleId = insertPutawayRule(warehouseId, zoneId);
      assertThat(ruleId).isNotNull().isPositive();
   }
}
