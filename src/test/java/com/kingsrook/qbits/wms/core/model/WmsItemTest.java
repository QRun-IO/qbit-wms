/*******************************************************************************
 ** Unit tests for WmsItem entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsItemTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsItem.TABLE_NAME).isEqualTo("wmsItem");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsItem entity = new WmsItem();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getSku()).isNull();
      assertThat(entity.getName()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsItem entity = new WmsItem();
      WmsItem result = entity
         .withId(1)
         .withClientId(2)
         .withSku("SKU-001")
         .withName("Widget")
         .withDescription("A fine widget")
         .withBarcodeUpc("123456789012")
         .withBarcodeSecondary("SEC-001")
         .withItemCategoryId(3)
         .withWeightLbs(new BigDecimal("2.5"))
         .withLengthIn(new BigDecimal("10.0"))
         .withWidthIn(new BigDecimal("5.0"))
         .withHeightIn(new BigDecimal("3.0"))
         .withBaseUom("EACH")
         .withUnitsPerCase(24)
         .withCasesPerPallet(40)
         .withIsLotTracked(true)
         .withIsSerialTracked(false)
         .withIsExpirationTracked(true)
         .withShelfLifeDays(365)
         .withMinRemainingShelfLifeDays(30)
         .withVelocityClassId(1)
         .withStorageRequirementsId(1)
         .withReorderPoint(100)
         .withReorderQuantity(500)
         .withIsActive(true)
         .withImageUrl("https://example.com/item.png")
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsItem original = new WmsItem()
         .withId(42)
         .withClientId(5)
         .withSku("SKU-TEST")
         .withName("Test Item")
         .withDescription("Description of test item")
         .withBarcodeUpc("UPC-TEST")
         .withBarcodeSecondary("SEC-TEST")
         .withItemCategoryId(7)
         .withWeightLbs(new BigDecimal("1.5"))
         .withLengthIn(new BigDecimal("8.0"))
         .withWidthIn(new BigDecimal("4.0"))
         .withHeightIn(new BigDecimal("2.0"))
         .withBaseUom("CASE")
         .withUnitsPerCase(12)
         .withCasesPerPallet(20)
         .withIsLotTracked(true)
         .withIsSerialTracked(true)
         .withIsExpirationTracked(false)
         .withShelfLifeDays(180)
         .withMinRemainingShelfLifeDays(15)
         .withVelocityClassId(2)
         .withStorageRequirementsId(3)
         .withReorderPoint(50)
         .withReorderQuantity(200)
         .withIsActive(true)
         .withImageUrl("https://example.com/test.jpg")
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsItem restored = new WmsItem(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getClientId()).isEqualTo(5);
      assertThat(restored.getSku()).isEqualTo("SKU-TEST");
      assertThat(restored.getName()).isEqualTo("Test Item");
      assertThat(restored.getDescription()).isEqualTo("Description of test item");
      assertThat(restored.getBarcodeUpc()).isEqualTo("UPC-TEST");
      assertThat(restored.getBarcodeSecondary()).isEqualTo("SEC-TEST");
      assertThat(restored.getItemCategoryId()).isEqualTo(7);
      assertThat(restored.getWeightLbs()).isEqualByComparingTo(new BigDecimal("1.5"));
      assertThat(restored.getLengthIn()).isEqualByComparingTo(new BigDecimal("8.0"));
      assertThat(restored.getWidthIn()).isEqualByComparingTo(new BigDecimal("4.0"));
      assertThat(restored.getHeightIn()).isEqualByComparingTo(new BigDecimal("2.0"));
      assertThat(restored.getBaseUom()).isEqualTo("CASE");
      assertThat(restored.getUnitsPerCase()).isEqualTo(12);
      assertThat(restored.getCasesPerPallet()).isEqualTo(20);
      assertThat(restored.getIsLotTracked()).isTrue();
      assertThat(restored.getIsSerialTracked()).isTrue();
      assertThat(restored.getIsExpirationTracked()).isFalse();
      assertThat(restored.getShelfLifeDays()).isEqualTo(180);
      assertThat(restored.getMinRemainingShelfLifeDays()).isEqualTo(15);
      assertThat(restored.getVelocityClassId()).isEqualTo(2);
      assertThat(restored.getStorageRequirementsId()).isEqualTo(3);
      assertThat(restored.getReorderPoint()).isEqualTo(50);
      assertThat(restored.getReorderQuantity()).isEqualTo(200);
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getImageUrl()).isEqualTo("https://example.com/test.jpg");
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsItem entity = new WmsItem(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getClientId()).isNull();
      assertThat(entity.getSku()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getDescription()).isNull();
      assertThat(entity.getBarcodeUpc()).isNull();
      assertThat(entity.getBarcodeSecondary()).isNull();
      assertThat(entity.getItemCategoryId()).isNull();
      assertThat(entity.getWeightLbs()).isNull();
      assertThat(entity.getLengthIn()).isNull();
      assertThat(entity.getWidthIn()).isNull();
      assertThat(entity.getHeightIn()).isNull();
      assertThat(entity.getBaseUom()).isNull();
      assertThat(entity.getUnitsPerCase()).isNull();
      assertThat(entity.getCasesPerPallet()).isNull();
      assertThat(entity.getIsLotTracked()).isNull();
      assertThat(entity.getIsSerialTracked()).isNull();
      assertThat(entity.getIsExpirationTracked()).isNull();
      assertThat(entity.getShelfLifeDays()).isNull();
      assertThat(entity.getMinRemainingShelfLifeDays()).isNull();
      assertThat(entity.getVelocityClassId()).isNull();
      assertThat(entity.getStorageRequirementsId()).isNull();
      assertThat(entity.getReorderPoint()).isNull();
      assertThat(entity.getReorderQuantity()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getImageUrl()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   @Test
   void testInsertViaHelper_defaultItem_returnsNonNullId() throws Exception
   {
      Integer itemId = insertItem();
      assertThat(itemId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaData_hasSections()
   {
      var table = com.kingsrook.qqq.backend.core.context.QContext.getQInstance().getTable(WmsItem.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
