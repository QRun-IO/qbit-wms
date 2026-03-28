/*******************************************************************************
 ** Unit tests for WmsTaskTypeConfig entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsTaskTypeConfigTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsTaskTypeConfig.TABLE_NAME).isEqualTo("wmsTaskTypeConfig");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsTaskTypeConfig entity = new WmsTaskTypeConfig();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getTaskTypeId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsTaskTypeConfig entity = new WmsTaskTypeConfig();
      WmsTaskTypeConfig result = entity
         .withId(1)
         .withTaskTypeId(2)
         .withDefaultPriority(50)
         .withDefaultEquipmentTypeId(1)
         .withAutoAssignEnabled(true)
         .withScanSourceLocation(true)
         .withScanDestinationLocation(false)
         .withScanItem(true)
         .withEscalationMinutes(30)
         .withDescription("Config for pick tasks")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsTaskTypeConfig original = new WmsTaskTypeConfig()
         .withId(5)
         .withTaskTypeId(6)
         .withDefaultPriority(75)
         .withDefaultEquipmentTypeId(2)
         .withAutoAssignEnabled(false)
         .withScanSourceLocation(false)
         .withScanDestinationLocation(true)
         .withScanItem(false)
         .withEscalationMinutes(60)
         .withDescription("Putaway config")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsTaskTypeConfig restored = new WmsTaskTypeConfig(record);

      assertThat(restored.getId()).isEqualTo(5);
      assertThat(restored.getTaskTypeId()).isEqualTo(6);
      assertThat(restored.getDefaultPriority()).isEqualTo(75);
      assertThat(restored.getDefaultEquipmentTypeId()).isEqualTo(2);
      assertThat(restored.getAutoAssignEnabled()).isFalse();
      assertThat(restored.getScanSourceLocation()).isFalse();
      assertThat(restored.getScanDestinationLocation()).isTrue();
      assertThat(restored.getScanItem()).isFalse();
      assertThat(restored.getEscalationMinutes()).isEqualTo(60);
      assertThat(restored.getDescription()).isEqualTo("Putaway config");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsTaskTypeConfig entity = new WmsTaskTypeConfig(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getTaskTypeId()).isNull();
      assertThat(entity.getDefaultPriority()).isNull();
      assertThat(entity.getDefaultEquipmentTypeId()).isNull();
      assertThat(entity.getAutoAssignEnabled()).isNull();
      assertThat(entity.getScanSourceLocation()).isNull();
      assertThat(entity.getScanDestinationLocation()).isNull();
      assertThat(entity.getScanItem()).isNull();
      assertThat(entity.getEscalationMinutes()).isNull();
      assertThat(entity.getDescription()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsTaskTypeConfig.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
