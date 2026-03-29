/*******************************************************************************
 ** Unit tests for WmsKitBom entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsKitBomTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsKitBom.TABLE_NAME).isEqualTo("wmsKitBom");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsKitBom entity = new WmsKitBom();
      WmsKitBom result = entity
         .withId(1)
         .withKitItemId(10)
         .withComponentItemId(20)
         .withComponentQuantity(3);

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsKitBom original = new WmsKitBom()
         .withId(42)
         .withKitItemId(10)
         .withComponentItemId(20)
         .withComponentQuantity(5);

      QRecord record = original.toQRecord();
      WmsKitBom restored = new WmsKitBom(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getKitItemId()).isEqualTo(10);
      assertThat(restored.getComponentItemId()).isEqualTo(20);
      assertThat(restored.getComponentQuantity()).isEqualTo(5);
   }



   @Test
   void testInsertViaHelper_defaultKitBom_returnsNonNullId() throws Exception
   {
      Integer kitItemId = insertItem("KIT-001", "Test Kit");
      Integer componentItemId = insertItem("COMP-001", "Component");
      Integer bomId = insertKitBom(kitItemId, componentItemId);
      assertThat(bomId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsKitBom.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
