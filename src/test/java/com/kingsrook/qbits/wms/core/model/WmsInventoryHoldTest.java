/*******************************************************************************
 ** Unit tests for WmsInventoryHold entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsInventoryHoldTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsInventoryHold.TABLE_NAME).isEqualTo("wmsInventoryHold");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsInventoryHold entity = new WmsInventoryHold();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getItemId()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsInventoryHold entity = new WmsInventoryHold();
      WmsInventoryHold result = entity
         .withId(1)
         .withWarehouseId(2)
         .withClientId(3)
         .withItemId(4)
         .withLotNumber("LOT-H")
         .withLocationId(5)
         .withHoldTypeId(1)
         .withReason("QC failure")
         .withPlacedBy("admin")
         .withPlacedDate(Instant.now())
         .withReleasedBy("supervisor")
         .withReleasedDate(Instant.now())
         .withStatus("ACTIVE")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsInventoryHold original = new WmsInventoryHold()
         .withId(10)
         .withWarehouseId(1)
         .withClientId(2)
         .withItemId(3)
         .withLotNumber("LOT-HOLD")
         .withLocationId(4)
         .withHoldTypeId(2)
         .withReason("Damaged goods")
         .withPlacedBy("jsmith")
         .withPlacedDate(now)
         .withReleasedBy("jdoe")
         .withReleasedDate(now)
         .withStatus("RELEASED")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsInventoryHold restored = new WmsInventoryHold(record);

      assertThat(restored.getId()).isEqualTo(10);
      assertThat(restored.getWarehouseId()).isEqualTo(1);
      assertThat(restored.getClientId()).isEqualTo(2);
      assertThat(restored.getItemId()).isEqualTo(3);
      assertThat(restored.getLotNumber()).isEqualTo("LOT-HOLD");
      assertThat(restored.getLocationId()).isEqualTo(4);
      assertThat(restored.getHoldTypeId()).isEqualTo(2);
      assertThat(restored.getReason()).isEqualTo("Damaged goods");
      assertThat(restored.getPlacedBy()).isEqualTo("jsmith");
      assertThat(restored.getPlacedDate()).isEqualTo(now);
      assertThat(restored.getReleasedBy()).isEqualTo("jdoe");
      assertThat(restored.getReleasedDate()).isEqualTo(now);
      assertThat(restored.getStatus()).isEqualTo("RELEASED");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsInventoryHold entity = new WmsInventoryHold(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getWarehouseId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getItemId()).isNull();
      assertThat(entity.getLotNumber()).isNull();
      assertThat(entity.getLocationId()).isNull();
      assertThat(entity.getHoldTypeId()).isNull();
      assertThat(entity.getReason()).isNull();
      assertThat(entity.getPlacedBy()).isNull();
      assertThat(entity.getPlacedDate()).isNull();
      assertThat(entity.getReleasedBy()).isNull();
      assertThat(entity.getReleasedDate()).isNull();
      assertThat(entity.getStatus()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsInventoryHold.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
