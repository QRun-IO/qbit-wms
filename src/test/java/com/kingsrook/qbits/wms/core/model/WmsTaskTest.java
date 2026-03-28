/*******************************************************************************
 ** Unit tests for WmsTask entity -- the most thorough entity test.
 ** Covers all 37+ fields including fluent setters, getters, QRecord round-trip.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsTaskTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsTask.TABLE_NAME).isEqualTo("wmsTask");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsTask entity = new WmsTask();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getTaskTypeId()).isNull();
      assertThat(entity.getTaskStatusId()).isNull();
      assertThat(entity.getPriority()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getQuantityRequested()).isNull();
      assertThat(entity.getQuantityCompleted()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getSerialNumber()).isNull();
      assertThat(entity.getLpnId()).isNull();
      assertThat(entity.getSourceLocationId()).isNull();
      assertThat(entity.getDestinationLocationId()).isNull();
      assertThat(entity.getZoneId()).isNull();
      assertThat(entity.getAssignedTo()).isNull();
      assertThat(entity.getAssignedDate()).isNull();
      assertThat(entity.getStartedDate()).isNull();
      assertThat(entity.getCompletedDate()).isNull();
      assertThat(entity.getCompletedBy()).isNull();
      assertThat(entity.getWaveId()).isNull();
      assertThat(entity.getTaskGroupId()).isNull();
      assertThat(entity.getSequence()).isNull();
      assertThat(entity.getEquipmentTypeId()).isNull();
      assertThat(entity.getReferenceType()).isNull();
      assertThat(entity.getReferenceId()).isNull();
      assertThat(entity.getOrderId()).isNull();
      assertThat(entity.getOrderLineId()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getReceiptLineId()).isNull();
      assertThat(entity.getCycleCountId()).isNull();
      assertThat(entity.getReturnAuthorizationId()).isNull();
      assertThat(entity.getExpectedQuantity()).isNull();
      assertThat(entity.getCountedQuantity()).isNull();
      assertThat(entity.getVariance()).isNull();
      assertThat(entity.getIsBlindCount()).isNull();
      assertThat(entity.getRecountRequired()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getShortReason()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsTask entity = new WmsTask();
      WmsTask result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withTaskTypeId(TaskType.MOVE.getId())
         .withTaskStatusId(TaskStatus.PENDING.getId())
         .withPriority(50)
         .withItemId(4)
         .withQuantityRequested(new BigDecimal("10.0"))
         .withQuantityCompleted(new BigDecimal("5.0"))
         .withLotNumber("LOT-T")
         .withSerialNumber("SN-T")
         .withLpnId(5)
         .withSourceLocationId(10)
         .withDestinationLocationId(11)
         .withZoneId(6)
         .withAssignedTo("worker1")
         .withAssignedDate(Instant.now())
         .withStartedDate(Instant.now())
         .withCompletedDate(Instant.now())
         .withCompletedBy("worker1")
         .withWaveId(7)
         .withTaskGroupId("GRP-001")
         .withSequence(1)
         .withEquipmentTypeId(1)
         .withReferenceType("ORDER")
         .withReferenceId(100)
         .withOrderId(200)
         .withOrderLineId(201)
         .withReceiptId(300)
         .withReceiptLineId(301)
         .withCycleCountId(400)
         .withReturnAuthorizationId(500)
         .withExpectedQuantity(new BigDecimal("10.0"))
         .withCountedQuantity(new BigDecimal("9.0"))
         .withVariance(new BigDecimal("-1.0"))
         .withIsBlindCount(true)
         .withRecountRequired(false)
         .withNotes("Test notes")
         .withShortReason("Short reason")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsTask original = new WmsTask()
         .withId(99)
         .withWarehouseId(1)
         .withClientId(2)
         .withTaskTypeId(TaskType.PICK.getId())
         .withTaskStatusId(TaskStatus.IN_PROGRESS.getId())
         .withPriority(75)
         .withItemId(3)
         .withQuantityRequested(new BigDecimal("20.0"))
         .withQuantityCompleted(new BigDecimal("15.0"))
         .withLotNumber("LOT-RND")
         .withSerialNumber("SN-RND")
         .withLpnId(4)
         .withSourceLocationId(10)
         .withDestinationLocationId(20)
         .withZoneId(5)
         .withAssignedTo("picker1")
         .withAssignedDate(now)
         .withStartedDate(now)
         .withCompletedDate(now)
         .withCompletedBy("picker1")
         .withWaveId(6)
         .withTaskGroupId("GRP-RT")
         .withSequence(3)
         .withEquipmentTypeId(2)
         .withReferenceType("PO")
         .withReferenceId(50)
         .withOrderId(60)
         .withOrderLineId(61)
         .withReceiptId(70)
         .withReceiptLineId(71)
         .withCycleCountId(80)
         .withReturnAuthorizationId(90)
         .withExpectedQuantity(new BigDecimal("20.0"))
         .withCountedQuantity(new BigDecimal("18.0"))
         .withVariance(new BigDecimal("-2.0"))
         .withIsBlindCount(false)
         .withRecountRequired(true)
         .withNotes("Round trip notes")
         .withShortReason("Short item")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsTask restored = new WmsTask(record);

      assertThat(restored.getId()).isEqualTo(99);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getTaskTypeId()).isEqualTo(TaskType.PICK.getId());
      assertThat(restored.getTaskStatusId()).isEqualTo(TaskStatus.IN_PROGRESS.getId());
      assertThat(restored.getPriority()).isEqualTo(75);
      assertThat(restored.getItemId()).isEqualTo(3);
      assertThat(restored.getQuantityRequested()).isEqualByComparingTo(new BigDecimal("20.0"));
      assertThat(restored.getQuantityCompleted()).isEqualByComparingTo(new BigDecimal("15.0"));
      assertThat(restored.getLotNumber()).isEqualTo("LOT-RND");
      assertThat(restored.getSerialNumber()).isEqualTo("SN-RND");
      assertThat(restored.getLpnId()).isEqualTo(4);
      assertThat(restored.getSourceLocationId()).isEqualTo(10);
      assertThat(restored.getDestinationLocationId()).isEqualTo(20);
      assertThat(restored.getZoneId()).isEqualTo(5);
      assertThat(restored.getAssignedTo()).isEqualTo("picker1");
      assertThat(restored.getAssignedDate()).isEqualTo(now);
      assertThat(restored.getStartedDate()).isEqualTo(now);
      assertThat(restored.getCompletedDate()).isEqualTo(now);
      assertThat(restored.getCompletedBy()).isEqualTo("picker1");
      assertThat(restored.getWaveId()).isEqualTo(6);
      assertThat(restored.getTaskGroupId()).isEqualTo("GRP-RT");
      assertThat(restored.getSequence()).isEqualTo(3);
      assertThat(restored.getEquipmentTypeId()).isEqualTo(2);
      assertThat(restored.getReferenceType()).isEqualTo("PO");
      assertThat(restored.getReferenceId()).isEqualTo(50);
      assertThat(restored.getOrderId()).isEqualTo(60);
      assertThat(restored.getOrderLineId()).isEqualTo(61);
      assertThat(restored.getReceiptId()).isEqualTo(70);
      assertThat(restored.getReceiptLineId()).isEqualTo(71);
      assertThat(restored.getCycleCountId()).isEqualTo(80);
      assertThat(restored.getReturnAuthorizationId()).isEqualTo(90);
      assertThat(restored.getExpectedQuantity()).isEqualByComparingTo(new BigDecimal("20.0"));
      assertThat(restored.getCountedQuantity()).isEqualByComparingTo(new BigDecimal("18.0"));
      assertThat(restored.getVariance()).isEqualByComparingTo(new BigDecimal("-2.0"));
      assertThat(restored.getIsBlindCount()).isFalse();
      assertThat(restored.getRecountRequired()).isTrue();
      assertThat(restored.getNotes()).isEqualTo("Round trip notes");
      assertThat(restored.getShortReason()).isEqualTo("Short item");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsTask entity = new WmsTask(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getTaskTypeId()).isNull();
      assertThat(entity.getTaskStatusId()).isNull();
      assertThat(entity.getPriority()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getQuantityRequested()).isNull();
      assertThat(entity.getQuantityCompleted()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getSerialNumber()).isNull();
      assertThat(entity.getLpnId()).isNull();
      assertThat(entity.getSourceLocationId()).isNull();
      assertThat(entity.getDestinationLocationId()).isNull();
      assertThat(entity.getZoneId()).isNull();
      assertThat(entity.getAssignedTo()).isNull();
      assertThat(entity.getAssignedDate()).isNull();
      assertThat(entity.getStartedDate()).isNull();
      assertThat(entity.getCompletedDate()).isNull();
      assertThat(entity.getCompletedBy()).isNull();
      assertThat(entity.getWaveId()).isNull();
      assertThat(entity.getTaskGroupId()).isNull();
      assertThat(entity.getSequence()).isNull();
      assertThat(entity.getEquipmentTypeId()).isNull();
      assertThat(entity.getReferenceType()).isNull();
      assertThat(entity.getReferenceId()).isNull();
      assertThat(entity.getOrderId()).isNull();
      assertThat(entity.getOrderLineId()).isNull();
      assertThat(entity.getReceiptId()).isNull();
      assertThat(entity.getReceiptLineId()).isNull();
      assertThat(entity.getCycleCountId()).isNull();
      assertThat(entity.getReturnAuthorizationId()).isNull();
      assertThat(entity.getExpectedQuantity()).isNull();
      assertThat(entity.getCountedQuantity()).isNull();
      assertThat(entity.getVariance()).isNull();
      assertThat(entity.getIsBlindCount()).isNull();
      assertThat(entity.getRecountRequired()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getShortReason()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_pendingTask_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.MOVE.getId(), TaskStatus.PENDING.getId());
      assertThat(taskId).isNotNull().isPositive();
   }



   @Test
   void testInsertViaHelper_completedTask_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.PICK.getId(), TaskStatus.COMPLETED.getId());
      assertThat(taskId).isNotNull().isPositive();
   }



   @Test
   void testInsertViaHelper_countTask_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer taskId = insertTask(warehouseId, TaskType.COUNT.getId(), TaskStatus.PENDING.getId());
      assertThat(taskId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaData_hasSections()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsTask.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }



   @Test
   void testQRecordRoundTrip_nullableFields_preserveNull()
   {
      ///////////////////////////////////////////////
      // Set only required fields, leave rest null //
      ///////////////////////////////////////////////
      WmsTask original = new WmsTask()
         .withId(1)
         .withWarehouseId(1)
         .withTaskTypeId(1)
         .withTaskStatusId(1);

      QRecord record = original.toQRecord();
      WmsTask restored = new WmsTask(record);

      assertThat(restored.getClientId()).isNull();
      assertThat(restored.getItemId()).isNull();
      assertThat(restored.getQuantityRequested()).isNull();
      assertThat(restored.getSourceLocationId()).isNull();
      assertThat(restored.getDestinationLocationId()).isNull();
      assertThat(restored.getNotes()).isNull();
   }



   @Test
   void testGettersMatchSetters_everyField_sameValue()
   {
      WmsTask entity = new WmsTask();

      entity.withId(1);
      assertThat(entity.getId()).isEqualTo(1);

      entity.withWarehouseId(2);
      assertThat(entity.getWarehouseId()).isEqualTo(2);

      entity.withClientId(3);
      assertThat(entity.getClientId()).isEqualTo(3);

      entity.withTaskTypeId(4);
      assertThat(entity.getTaskTypeId()).isEqualTo(4);

      entity.withTaskStatusId(5);
      assertThat(entity.getTaskStatusId()).isEqualTo(5);

      entity.withPriority(75);
      assertThat(entity.getPriority()).isEqualTo(75);

      entity.withItemId(6);
      assertThat(entity.getItemId()).isEqualTo(6);

      entity.withQuantityRequested(new BigDecimal("10"));
      assertThat(entity.getQuantityRequested()).isEqualByComparingTo(BigDecimal.TEN);

      entity.withQuantityCompleted(new BigDecimal("8"));
      assertThat(entity.getQuantityCompleted()).isEqualByComparingTo(new BigDecimal("8"));

      entity.withLotNumber("LOT");
      assertThat(entity.getLotNumber()).isEqualTo("LOT");

      entity.withSerialNumber("SN");
      assertThat(entity.getSerialNumber()).isEqualTo("SN");

      entity.withLpnId(7);
      assertThat(entity.getLpnId()).isEqualTo(7);

      entity.withSourceLocationId(8);
      assertThat(entity.getSourceLocationId()).isEqualTo(8);

      entity.withDestinationLocationId(9);
      assertThat(entity.getDestinationLocationId()).isEqualTo(9);

      entity.withZoneId(10);
      assertThat(entity.getZoneId()).isEqualTo(10);

      entity.withAssignedTo("user");
      assertThat(entity.getAssignedTo()).isEqualTo("user");

      Instant now = Instant.now();
      entity.withAssignedDate(now);
      assertThat(entity.getAssignedDate()).isEqualTo(now);

      entity.withStartedDate(now);
      assertThat(entity.getStartedDate()).isEqualTo(now);

      entity.withCompletedDate(now);
      assertThat(entity.getCompletedDate()).isEqualTo(now);

      entity.withCompletedBy("completor");
      assertThat(entity.getCompletedBy()).isEqualTo("completor");

      entity.withWaveId(11);
      assertThat(entity.getWaveId()).isEqualTo(11);

      entity.withTaskGroupId("GRP");
      assertThat(entity.getTaskGroupId()).isEqualTo("GRP");

      entity.withSequence(12);
      assertThat(entity.getSequence()).isEqualTo(12);

      entity.withEquipmentTypeId(13);
      assertThat(entity.getEquipmentTypeId()).isEqualTo(13);

      entity.withReferenceType("REF");
      assertThat(entity.getReferenceType()).isEqualTo("REF");

      entity.withReferenceId(14);
      assertThat(entity.getReferenceId()).isEqualTo(14);

      entity.withOrderId(15);
      assertThat(entity.getOrderId()).isEqualTo(15);

      entity.withOrderLineId(16);
      assertThat(entity.getOrderLineId()).isEqualTo(16);

      entity.withReceiptId(17);
      assertThat(entity.getReceiptId()).isEqualTo(17);

      entity.withReceiptLineId(18);
      assertThat(entity.getReceiptLineId()).isEqualTo(18);

      entity.withCycleCountId(19);
      assertThat(entity.getCycleCountId()).isEqualTo(19);

      entity.withReturnAuthorizationId(20);
      assertThat(entity.getReturnAuthorizationId()).isEqualTo(20);

      entity.withExpectedQuantity(new BigDecimal("100"));
      assertThat(entity.getExpectedQuantity()).isEqualByComparingTo(new BigDecimal("100"));

      entity.withCountedQuantity(new BigDecimal("99"));
      assertThat(entity.getCountedQuantity()).isEqualByComparingTo(new BigDecimal("99"));

      entity.withVariance(new BigDecimal("-1"));
      assertThat(entity.getVariance()).isEqualByComparingTo(new BigDecimal("-1"));

      entity.withIsBlindCount(true);
      assertThat(entity.getIsBlindCount()).isTrue();

      entity.withRecountRequired(false);
      assertThat(entity.getRecountRequired()).isFalse();

      entity.withNotes("notes");
      assertThat(entity.getNotes()).isEqualTo("notes");

      entity.withShortReason("short");
      assertThat(entity.getShortReason()).isEqualTo("short");

      entity.withCreateDate(now);
      assertThat(entity.getCreateDate()).isEqualTo(now);

      entity.withModifyDate(now);
      assertThat(entity.getModifyDate()).isEqualTo(now);
   }
}
