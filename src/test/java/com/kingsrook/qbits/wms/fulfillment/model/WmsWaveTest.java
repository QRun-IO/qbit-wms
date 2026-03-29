/*******************************************************************************
 ** Unit tests for WmsWave entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsWaveTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsWave.TABLE_NAME).isEqualTo("wmsWave");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsWave entity = new WmsWave();
      WmsWave result = entity
         .withId(1)
         .withWarehouseId(10)
         .withWaveNumber("WAVE-001")
         .withStatusId(1)
         .withWaveTypeId(4)
         .withTotalOrders(10)
         .withTotalLines(50)
         .withTotalUnits(200);

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsWave original = new WmsWave()
         .withId(42)
         .withWarehouseId(10)
         .withWaveNumber("WAVE-100")
         .withStatusId(2)
         .withTotalOrders(5);

      QRecord record = original.toQRecord();
      WmsWave restored = new WmsWave(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getWarehouseId()).isEqualTo(10);
      assertThat(restored.getWaveNumber()).isEqualTo("WAVE-100");
      assertThat(restored.getStatusId()).isEqualTo(2);
      assertThat(restored.getTotalOrders()).isEqualTo(5);
   }



   @Test
   void testInsertViaHelper_defaultWave_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer waveId = insertWave(warehouseId);
      assertThat(waveId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsWave.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
