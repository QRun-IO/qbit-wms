/*******************************************************************************
 ** Unit tests for WmsReturnReceiptLine entity.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.model;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsReturnReceiptLineTest extends BaseTest
{

   @Test
   void testTableName_constant_isCorrect()
   {
      assertThat(WmsReturnReceiptLine.TABLE_NAME).isEqualTo("wmsReturnReceiptLine");
   }



   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsReturnReceiptLine entity = new WmsReturnReceiptLine()
         .withId(1)
         .withReturnReceiptId(10)
         .withItemId(20)
         .withQuantityReceived(5)
         .withLotNumber("LOT-A")
         .withSerialNumber("SN-001");

      assertThat(entity.getQuantityReceived()).isEqualTo(5);
      assertThat(entity.getLotNumber()).isEqualTo("LOT-A");
   }



   @Test
   void testQRecordRoundTrip_allFields_valuesPreserved()
   {
      WmsReturnReceiptLine original = new WmsReturnReceiptLine()
         .withId(42)
         .withReturnReceiptId(10)
         .withItemId(20)
         .withQuantityReceived(3)
         .withInspectionNotes("Minor scratches")
         .withInspectedBy("Bob");

      QRecord record = original.toQRecord();
      WmsReturnReceiptLine restored = new WmsReturnReceiptLine(record);

      assertThat(restored.getQuantityReceived()).isEqualTo(3);
      assertThat(restored.getInspectionNotes()).isEqualTo("Minor scratches");
   }



   @Test
   void testTableMetaDataCustomizer_hasSections() throws Exception
   {
      var table = QContext.getQInstance().getTable(WmsReturnReceiptLine.TABLE_NAME);
      assertThat(table).isNotNull();
      assertThat(table.getSections()).isNotEmpty();
   }
}
