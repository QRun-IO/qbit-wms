/*******************************************************************************
 ** Unit tests for WmsReturnAuthorizationLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReturnAuthorizationLineTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReturnAuthorizationLine.TABLE_NAME).isEqualTo("wmsReturnAuthorizationLine");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReturnAuthorizationLine entity = new WmsReturnAuthorizationLine()
         .withId(1)
         .withReturnAuthorizationId(10)
         .withItemId(20)
         .withQuantityAuthorized(5)
         .withQuantityReceived(0);

      assertThat(entity.getQuantityAuthorized()).isEqualTo(5);
      assertThat(entity.getQuantityReceived()).isEqualTo(0);
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsReturnAuthorizationLine original = new WmsReturnAuthorizationLine()
         .withId(42)
         .withReturnAuthorizationId(10)
         .withItemId(20)
         .withQuantityAuthorized(10)
         .withQuantityReceived(3);

      QRecord record = original.toQRecord();
      WmsReturnAuthorizationLine restored = new WmsReturnAuthorizationLine(record);

      assertThat(restored.getQuantityAuthorized()).isEqualTo(10);
      assertThat(restored.getQuantityReceived()).isEqualTo(3);
   }



   @Test
   void testInsertViaHelper_returnsNonNullId() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer raId = insertReturnAuthorization(warehouseId);
      Integer itemId = insertItem();
      Integer lineId = insertReturnAuthorizationLine(raId, itemId);
      assertThat(lineId).isNotNull().isPositive();
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsReturnAuthorizationLine.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
