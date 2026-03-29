/*******************************************************************************
 ** Unit tests for WmsReplenishmentRule entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReplenishmentRuleTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReplenishmentRule.TABLE_NAME).isEqualTo("wmsReplenishmentRule");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsReplenishmentRule entity = new WmsReplenishmentRule();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getPickLocationId()).isNull();
      assertThat(entity.getMinQuantity()).isNull();
      assertThat(entity.getMaxQuantity()).isNull();
      assertThat(entity.getReplenishmentUom()).isNull();
      assertThat(entity.getSourceZoneId()).isNull();
      assertThat(entity.getPriority()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReplenishmentRule entity = new WmsReplenishmentRule();
      Instant now = Instant.now();
      WmsReplenishmentRule result = entity
         .withId(1)
         .withWarehouseId(2)
         .withItemId(3)
         .withPickLocationId(4)
         .withMinQuantity(10)
         .withMaxQuantity(50)
         .withReplenishmentUom("EA")
         .withSourceZoneId(5)
         .withPriority(3)
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test all fields round-trip through QRecord.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsReplenishmentRule original = new WmsReplenishmentRule()
         .withId(42)
         .withWarehouseId(1)
         .withItemId(2)
         .withPickLocationId(3)
         .withMinQuantity(5)
         .withMaxQuantity(25)
         .withReplenishmentUom("CS")
         .withSourceZoneId(4)
         .withPriority(7)
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsReplenishmentRule restored = new WmsReplenishmentRule(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getItemId()).isEqualTo(2);
      assertThat(restored.getPickLocationId()).isEqualTo(3);
      assertThat(restored.getMinQuantity()).isEqualTo(5);
      assertThat(restored.getMaxQuantity()).isEqualTo(25);
      assertThat(restored.getReplenishmentUom()).isEqualTo("CS");
      assertThat(restored.getSourceZoneId()).isEqualTo(4);
      assertThat(restored.getPriority()).isEqualTo(7);
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsReplenishmentRule entity = new WmsReplenishmentRule(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getPickLocationId()).isNull();
      assertThat(entity.getMinQuantity()).isNull();
      assertThat(entity.getMaxQuantity()).isNull();
      assertThat(entity.getReplenishmentUom()).isNull();
      assertThat(entity.getSourceZoneId()).isNull();
      assertThat(entity.getPriority()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test setter methods.
    *******************************************************************************/
   @Test
   void testSetters_allFields_valuesSet()
   {
      WmsReplenishmentRule entity = new WmsReplenishmentRule();
      Instant now = Instant.now();

      entity.setId(1);
      entity.setWarehouseId(2);
      entity.setItemId(3);
      entity.setPickLocationId(4);
      entity.setMinQuantity(10);
      entity.setMaxQuantity(50);
      entity.setReplenishmentUom("EA");
      entity.setSourceZoneId(5);
      entity.setPriority(3);
      entity.setIsActive(true);
      entity.setCreateDate(now);
      entity.setModifyDate(now);

      assertThat(entity.getId()).isEqualTo(1);
      assertThat(entity.getWarehouseId()).isEqualTo(2);
      assertThat(entity.getItemId()).isEqualTo(3);
      assertThat(entity.getPickLocationId()).isEqualTo(4);
      assertThat(entity.getMinQuantity()).isEqualTo(10);
      assertThat(entity.getMaxQuantity()).isEqualTo(50);
      assertThat(entity.getReplenishmentUom()).isEqualTo("EA");
      assertThat(entity.getSourceZoneId()).isEqualTo(5);
      assertThat(entity.getPriority()).isEqualTo(3);
      assertThat(entity.getIsActive()).isTrue();
      assertThat(entity.getCreateDate()).isEqualTo(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test insert and retrieve via BaseTest helper.
    *******************************************************************************/
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



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var qInstance = com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
      var table = qInstance.getTable(WmsReplenishmentRule.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
