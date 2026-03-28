/*******************************************************************************
 ** Unit tests for WmsItemCategory entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsItemCategoryTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsItemCategory.TABLE_NAME).isEqualTo("wmsItemCategory");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsItemCategory entity = new WmsItemCategory();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsItemCategory entity = new WmsItemCategory();
      WmsItemCategory result = entity
         .withId(1)
         .withName("Electronics")
         .withParentCategoryId(null)
         .withDefaultStorageRequirementsId(1)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsItemCategory original = new WmsItemCategory()
         .withId(3)
         .withName("Apparel")
         .withParentCategoryId(1)
         .withDefaultStorageRequirementsId(2)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsItemCategory restored = new WmsItemCategory(record);

      assertThat(restored.getId()).isEqualTo(3);
      assertThat(restored.getName()).isEqualTo("Apparel");
      assertThat(restored.getParentCategoryId()).isEqualTo(1);
      assertThat(restored.getDefaultStorageRequirementsId()).isEqualTo(2);
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsItemCategory entity = new WmsItemCategory(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getParentCategoryId()).isNull();
      assertThat(entity.getDefaultStorageRequirementsId()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testTableExists_inQInstance()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsItemCategory.TABLE_NAME);
      assertThat(table).isNotNull();
   }
}
