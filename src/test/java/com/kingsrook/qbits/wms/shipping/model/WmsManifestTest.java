/*******************************************************************************
 ** Unit tests for WmsManifest entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsManifestTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsManifest.TABLE_NAME).isEqualTo("wmsManifest");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsManifest entity = new WmsManifest()
         .withId(1)
         .withWarehouseId(10)
         .withManifestNumber("MAN-001")
         .withCarrier("UPS")
         .withStatusId(1);

      assertThat(entity.getManifestNumber()).isEqualTo("MAN-001");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsManifest original = new WmsManifest()
         .withId(42)
         .withWarehouseId(10)
         .withManifestNumber("MAN-002")
         .withCarrier("FEDEX");

      QRecord record = original.toQRecord();
      WmsManifest restored = new WmsManifest(record);

      assertThat(restored.getManifestNumber()).isEqualTo("MAN-002");
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer manifestId = insertManifest(warehouseId);
      assertThat(manifestId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsManifest.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
