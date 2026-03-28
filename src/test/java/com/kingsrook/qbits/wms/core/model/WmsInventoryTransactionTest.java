/*******************************************************************************
 ** Unit tests for WmsInventoryTransaction entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInventoryTransactionTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsInventoryTransaction.TABLE_NAME).isEqualTo("wmsInventoryTransaction");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsInventoryTransaction entity = new WmsInventoryTransaction();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsInventoryTransaction entity = new WmsInventoryTransaction();
      WmsInventoryTransaction result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withItemId(4)
         .withTransactionTypeId(7)
         .withFromLocationId(10)
         .withToLocationId(11)
         .withQuantity(new BigDecimal("25.0"))
         .withLotNumber("LOT-X")
         .withSerialNumber("SN-X")
         .withLpnId(5)
         .withReferenceType("ORDER")
         .withReferenceId(100)
         .withTaskId(200)
         .withReasonCode("CORRECTION")
         .withPerformedBy("jsmith")
         .withPerformedDate(Instant.now())
         .withNotes("Test transaction")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsInventoryTransaction original = new WmsInventoryTransaction()
         .withId(55)
         .withWarehouseId(1)
         .withClientId(2)
         .withItemId(3)
         .withTransactionTypeId(7)
         .withFromLocationId(10)
         .withToLocationId(20)
         .withQuantity(new BigDecimal("15.0"))
         .withLotNumber("LOT-T")
         .withSerialNumber("SN-T")
         .withLpnId(4)
         .withReferenceType("TASK")
         .withReferenceId(50)
         .withTaskId(60)
         .withReasonCode("MOVE")
         .withPerformedBy("jdoe")
         .withPerformedDate(now)
         .withNotes("Moved inventory")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsInventoryTransaction restored = new WmsInventoryTransaction(record);

      assertThat(restored.getId()).isEqualTo(55);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getItemId()).isEqualTo(3);
      assertThat(restored.getTransactionTypeId()).isEqualTo(7);
      assertThat(restored.getFromLocationId()).isEqualTo(10);
      assertThat(restored.getToLocationId()).isEqualTo(20);
      assertThat(restored.getQuantity()).isEqualByComparingTo(new BigDecimal("15.0"));
      assertThat(restored.getLotNumber()).isEqualTo("LOT-T");
      assertThat(restored.getSerialNumber()).isEqualTo("SN-T");
      assertThat(restored.getLpnId()).isEqualTo(4);
      assertThat(restored.getReferenceType()).isEqualTo("TASK");
      assertThat(restored.getReferenceId()).isEqualTo(50);
      assertThat(restored.getTaskId()).isEqualTo(60);
      assertThat(restored.getReasonCode()).isEqualTo("MOVE");
      assertThat(restored.getPerformedBy()).isEqualTo("jdoe");
      assertThat(restored.getPerformedDate()).isEqualTo(now);
      assertThat(restored.getNotes()).isEqualTo("Moved inventory");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsInventoryTransaction entity = new WmsInventoryTransaction(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getTransactionTypeId()).isNull();
      assertThat(entity.getFromLocationId()).isNull();
      assertThat(entity.getToLocationId()).isNull();
      assertThat(entity.getQuantity()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getSerialNumber()).isNull();
      assertThat(entity.getLpnId()).isNull();
      assertThat(entity.getReferenceType()).isNull();
      assertThat(entity.getReferenceId()).isNull();
      assertThat(entity.getTaskId()).isNull();
      assertThat(entity.getReasonCode()).isNull();
      assertThat(entity.getPerformedBy()).isNull();
      assertThat(entity.getPerformedDate()).isNull();
      assertThat(entity.getNotes()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsInventoryTransaction.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
