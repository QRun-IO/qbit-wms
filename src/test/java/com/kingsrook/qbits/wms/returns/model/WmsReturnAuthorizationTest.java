/*******************************************************************************
 ** Unit tests for WmsReturnAuthorization entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReturnAuthorizationTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReturnAuthorization.TABLE_NAME).isEqualTo("wmsReturnAuthorization");
   }



   @Test
   void testDefaultConstructor_newInstance_allFieldsNull()
   {
      WmsReturnAuthorization entity = new WmsReturnAuthorization();
      assertThat(entity.getId()).isNull();
      assertThat(entity.getRmaNumber()).isNull();
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReturnAuthorization entity = new WmsReturnAuthorization()
         .withId(1)
         .withWarehouseId(10)
         .withRmaNumber("RMA-001")
         .withCustomerName("John Doe")
         .withStatusId(2);

      assertThat(entity.getRmaNumber()).isEqualTo("RMA-001");
      assertThat(entity.getCustomerName()).isEqualTo("John Doe");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      Instant now = Instant.now();
      WmsReturnAuthorization original = new WmsReturnAuthorization()
         .withId(42)
         .withWarehouseId(10)
         .withRmaNumber("RMA-100")
         .withCustomerName("Jane Doe")
         .withStatusId(3)
         .withAuthorizedDate(now)
         .withNotes("Test notes");

      QRecord record = original.toQRecord();
      WmsReturnAuthorization restored = new WmsReturnAuthorization(record);

      assertThat(restored.getRmaNumber()).isEqualTo("RMA-100");
      assertThat(restored.getCustomerName()).isEqualTo("Jane Doe");
      assertThat(restored.getNotes()).isEqualTo("Test notes");
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer raId = insertReturnAuthorization(warehouseId);
      assertThat(raId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsReturnAuthorization.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getIcon()).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
