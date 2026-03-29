/*******************************************************************************
 ** Unit tests for AbcAnalysisStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.VelocityClass;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class AbcAnalysisStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that ABC analysis classifies items by velocity.  With 5 items, the
    ** top 20% (1 item) should be A, next 30% should be B, and the rest C.
    *******************************************************************************/
   @Test
   void testRun_multipleItems_classifiedByVelocity() throws Exception
   {
      Integer warehouseId = insertWarehouse();

      /////////////////////////////////////////////////////////////////////////
      // Create 5 items with varying order volumes                           //
      /////////////////////////////////////////////////////////////////////////
      Integer item1 = insertItem("SKU-A", "High Volume");
      Integer item2 = insertItem("SKU-B", "Med-High Volume");
      Integer item3 = insertItem("SKU-C", "Medium Volume");
      Integer item4 = insertItem("SKU-D", "Low-Med Volume");
      Integer item5 = insertItem("SKU-E", "Low Volume");

      /////////////////////////////////////////////////////////////////////////
      // Create order lines with descending quantities                       //
      /////////////////////////////////////////////////////////////////////////
      Integer orderId = insertOrder(warehouseId);
      insertOrderLine(orderId, item1, 500);
      insertOrderLine(orderId, item2, 300);
      insertOrderLine(orderId, item3, 100);
      insertOrderLine(orderId, item4, 50);
      insertOrderLine(orderId, item5, 10);

      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AbcAnalysisStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify classifications                                              //
      /////////////////////////////////////////////////////////////////////////
      assertThat(output.getValueInteger("itemsClassified")).isEqualTo(5);

      QRecord highest = new GetAction().execute(new GetInput(WmsItem.TABLE_NAME).withPrimaryKey(item1)).getRecord();
      assertThat(highest.getValueInteger("velocityClassId")).isEqualTo(VelocityClass.A.getPossibleValueId());

      QRecord lowest = new GetAction().execute(new GetInput(WmsItem.TABLE_NAME).withPrimaryKey(item5)).getRecord();
      assertThat(lowest.getValueInteger("velocityClassId")).isEqualTo(VelocityClass.C.getPossibleValueId());
   }



   /*******************************************************************************
    ** Test that with no order history, no items are classified.
    *******************************************************************************/
   @Test
   void testRun_noOrderHistory_noClassification() throws Exception
   {
      RunBackendStepInput input = new RunBackendStepInput();
      RunBackendStepOutput output = new RunBackendStepOutput();
      new AbcAnalysisStep().run(input, output);

      assertThat(output.getValueInteger("itemsClassified")).isEqualTo(0);
   }
}
