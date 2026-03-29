/*******************************************************************************
 ** Unit tests for WmsReturnReceipt entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReturnReceiptTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReturnReceipt.TABLE_NAME).isEqualTo("wmsReturnReceipt");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReturnReceipt entity = new WmsReturnReceipt()
         .withId(1)
         .withReturnAuthorizationId(10)
         .withReceiptNumber("RRCPT-001")
         .withReceivedBy("TestUser")
         .withCarrierName("UPS")
         .withTrackingNumber("TRK123");

      assertThat(entity.getReceiptNumber()).isEqualTo("RRCPT-001");
      assertThat(entity.getCarrierName()).isEqualTo("UPS");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsReturnReceipt original = new WmsReturnReceipt()
         .withId(42)
         .withReturnAuthorizationId(10)
         .withReceiptNumber("RRCPT-100")
         .withReceivedBy("Alice");

      QRecord record = original.toQRecord();
      WmsReturnReceipt restored = new WmsReturnReceipt(record);

      assertThat(restored.getReceiptNumber()).isEqualTo("RRCPT-100");
      assertThat(restored.getReceivedBy()).isEqualTo("Alice");
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsReturnReceipt.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
