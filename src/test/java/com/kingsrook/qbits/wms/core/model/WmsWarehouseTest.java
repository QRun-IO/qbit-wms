/*******************************************************************************
 ** Unit tests for WmsWarehouse entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsWarehouseTest extends BaseTest
{

   /*******************************************************************************
    ** Test TABLE_NAME constant.
    *******************************************************************************/
   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsWarehouse.TABLE_NAME).isEqualTo("wmsWarehouse");
   }



   /*******************************************************************************
    ** Test default constructor creates empty entity.
    *******************************************************************************/
   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsWarehouse entity = new WmsWarehouse();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
   }



   /*******************************************************************************
    ** Test fluent setters chain correctly and return same instance.
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsWarehouse entity = new WmsWarehouse();
      WmsWarehouse result = entity
         .withId(1)
         .withName("Main Warehouse")
         .withCode("WH01")
         .withAddressLine1("123 Main St")
         .withAddressLine2("Suite 100")
         .withCity("Springfield")
         .withStateProvince("IL")
         .withPostalCode("62701")
         .withCountry("US")
         .withTimezone("America/Chicago")
         .withIsActive(true)
         .withCreateDate(Instant.now())
         .withModifyDate(Instant.now());

      assertThat(result).isSameAs(entity);
   }



   /*******************************************************************************
    ** Test all fields round-trip through QRecord.
    *******************************************************************************/
   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();

      WmsWarehouse original = new WmsWarehouse()
         .withId(42)
         .withName("Test Warehouse")
         .withCode("TW01")
         .withAddressLine1("456 Elm St")
         .withAddressLine2("Bldg B")
         .withCity("Shelbyville")
         .withStateProvince("IN")
         .withPostalCode("46176")
         .withCountry("US")
         .withTimezone("America/New_York")
         .withIsActive(true)
         .withCreateDate(now)
         .withModifyDate(now);

      QRecord record = original.toQRecord();
      WmsWarehouse restored = new WmsWarehouse(record);

      assertThat(restored.getId()).isEqualTo(42);
      assertThat(restored.getName()).isEqualTo("Test Warehouse");
      assertThat(restored.getCode()).isEqualTo("TW01");
      assertThat(restored.getAddressLine1()).isEqualTo("456 Elm St");
      assertThat(restored.getAddressLine2()).isEqualTo("Bldg B");
      assertThat(restored.getCity()).isEqualTo("Shelbyville");
      assertThat(restored.getStateProvince()).isEqualTo("IN");
      assertThat(restored.getPostalCode()).isEqualTo("46176");
      assertThat(restored.getCountry()).isEqualTo("US");
      assertThat(restored.getTimezone()).isEqualTo("America/New_York");
      assertThat(restored.getIsActive()).isTrue();
      assertThat(restored.getCreateDate()).isEqualTo(now);
      assertThat(restored.getModifyDate()).isEqualTo(now);
   }



   /*******************************************************************************
    ** Test constructing from empty QRecord.
    *******************************************************************************/
   @Test
   void testQRecordConstructor_emptyRecord_allFieldsNull()
   {
      WmsWarehouse entity = new WmsWarehouse(new QRecord());
      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isNull();
      assertThat(entity.getCode()).isNull();
      assertThat(entity.getAddressLine1()).isNull();
      assertThat(entity.getAddressLine2()).isNull();
      assertThat(entity.getCity()).isNull();
      assertThat(entity.getStateProvince()).isNull();
      assertThat(entity.getPostalCode()).isNull();
      assertThat(entity.getCountry()).isNull();
      assertThat(entity.getTimezone()).isNull();
      assertThat(entity.getIsActive()).isNull();
      assertThat(entity.getCreateDate()).isNull();
      assertThat(entity.getModifyDate()).isNull();
   }



   /*******************************************************************************
    ** Test insert via BaseTest helper returns valid id.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_defaultWarehouse_returnsNonNullId() throws Exception
   {
      Integer id = insertWarehouse();
      assertThat(id).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test insert with custom name and code.
    *******************************************************************************/
   @Test
   void testInsertViaHelper_customNameAndCode_returnsNonNullId() throws Exception
   {
      Integer id = insertWarehouse("Custom WH", "CW01");
      assertThat(id).isNotNull().isPositive();
   }



   /*******************************************************************************
    ** Test TableMetaDataCustomizer produces valid metadata.
    *******************************************************************************/
   @Test
   void testTableMetaDataCustomizer_customizeMetaData_hasSections() throws Exception
   {
      var qInstance = com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
      var table = qInstance.getTable(WmsWarehouse.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
      assertThat(table.getRecordLabelFormat()).isNotNull();
   }
}
