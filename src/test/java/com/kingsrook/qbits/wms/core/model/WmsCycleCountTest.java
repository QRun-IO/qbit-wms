/*******************************************************************************
 ** Unit tests for WmsCycleCount entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsCycleCountTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsCycleCount.TABLE_NAME).isEqualTo("wmsCycleCount");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsCycleCount entity = new WmsCycleCount();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getCountTypeId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsCycleCount entity = new WmsCycleCount();
      WmsCycleCount result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withCountTypeId(CycleCountType.FULL.getId())
         .withCycleCountStatusId(CycleCountStatus.PLANNED.getId())
         .withPlannedDate(Instant.now())
         .withStartedDate(Instant.now())
         .withCompletedDate(Instant.now())
         .withAssignedTo("counter1")
         .withNotes("Full cycle count")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsCycleCount original = new WmsCycleCount()
         .withId(10)
         .withWarehouseId(1)
         .withClientId(2)
         .withCountTypeId(CycleCountType.ABC_BASED.getId())
         .withCycleCountStatusId(CycleCountStatus.IN_PROGRESS.getId())
         .withPlannedDate(now)
         .withStartedDate(now)
         .withCompletedDate(now)
         .withAssignedTo("counter-rt")
         .withNotes("Round trip test")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsCycleCount restored = new WmsCycleCount(record);

      assertThat(restored.getId()).isEqualTo(10);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getCountTypeId()).isEqualTo(CycleCountType.ABC_BASED.getId());
      assertThat(restored.getCycleCountStatusId()).isEqualTo(CycleCountStatus.IN_PROGRESS.getId());
      assertThat(restored.getPlannedDate()).isEqualTo(now);
      assertThat(restored.getStartedDate()).isEqualTo(now);
      assertThat(restored.getCompletedDate()).isEqualTo(now);
      assertThat(restored.getAssignedTo()).isEqualTo("counter-rt");
      assertThat(restored.getNotes()).isEqualTo("Round trip test");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsCycleCount entity = new WmsCycleCount(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getCountTypeId()).isNull();
      assertThat(entity.getCycleCountStatusId()).isNull();
      assertThat(entity.getPlannedDate()).isNull();
      assertThat(entity.getStartedDate()).isNull();
      assertThat(entity.getCompletedDate()).isNull();
      assertThat(entity.getAssignedTo()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer ccId = insertCycleCount(warehouseId);
      assertThat(ccId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaData_hasSections()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsCycleCount.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getIcon()).isNotNull();
   }
}
