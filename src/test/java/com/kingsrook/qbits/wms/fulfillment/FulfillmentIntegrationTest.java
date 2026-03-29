/*******************************************************************************
 ** Integration test for the full order-to-packed fulfillment flow.
 ** Creates an order, allocates it, creates a wave, releases the wave to
 ** generate picks, completes the picks, and completes the pack.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.OrderStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import com.kingsrook.qbits.wms.fulfillment.model.WmsWave;
import com.kingsrook.qbits.wms.fulfillment.processes.AllocateOrdersStep;
import com.kingsrook.qbits.wms.fulfillment.processes.ReleaseWaveGeneratePicksStep;
import com.kingsrook.qbits.wms.tasks.completion.PackCompletionHandler;
import com.kingsrook.qbits.wms.tasks.completion.PickCompletionHandler;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class FulfillmentIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** Full order-to-packed flow.
    *******************************************************************************/
   @Test
   void testFullFlow_orderToPacked() throws Exception
   {
      /////////////////////////////////////////////////////////////////////////
      // Setup: warehouse, item, location, inventory                         //
      /////////////////////////////////////////////////////////////////////////
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      insertInventory(warehouseId, itemId, locationId, new BigDecimal("100"));

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Create order                                                //
      /////////////////////////////////////////////////////////////////////////
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Allocate orders                                             //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput allocateInput = new RunBackendStepInput();
      allocateInput.addValue("warehouseId", warehouseId);
      RunBackendStepOutput allocateOutput = new RunBackendStepOutput();
      new AllocateOrdersStep().run(allocateInput, allocateOutput);

      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.ALLOCATED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Create wave and assign order                                //
      /////////////////////////////////////////////////////////////////////////
      Integer waveId = insertWave(warehouseId);
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("waveId", waveId)));

      /////////////////////////////////////////////////////////////////////////
      // Step 4: Release wave to generate picks                              //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput releaseInput = new RunBackendStepInput();
      releaseInput.addValue("waveId", waveId);
      RunBackendStepOutput releaseOutput = new RunBackendStepOutput();
      new ReleaseWaveGeneratePicksStep().run(releaseInput, releaseOutput);

      order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PICK_RELEASED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Step 5: Complete pick tasks                                         //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput pickQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.PICK.getPossibleValueId()))));

      for(QRecord pickTask : pickQuery.getRecords())
      {
         /////////////////////////////////////////////////////////////////////
         // Mark as COMPLETED then handle                                   //
         /////////////////////////////////////////////////////////////////////
         new UpdateAction().execute(new UpdateInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", pickTask.getValueInteger("id"))
            .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
            .withValue("quantityCompleted", pickTask.getValueBigDecimal("quantityRequested"))));

         QRecord completedTask = new GetAction().execute(new GetInput(WmsTask.TABLE_NAME)
            .withPrimaryKey(pickTask.getValueInteger("id"))).getRecord();

         new PickCompletionHandler().handle(completedTask);
      }

      order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PICKED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Step 6: Pack the order -- create a carton, then complete pack       //
      /////////////////////////////////////////////////////////////////////////
      Integer cartonId = insertCarton(orderId);
      insertCartonLine(cartonId, itemId, 10);

      QRecord packTask = new QRecord()
         .withValue("id", 999)
         .withValue("warehouseId", warehouseId)
         .withValue("orderId", orderId)
         .withValue("cartonId", cartonId)
         .withValue("quantityCompleted", BigDecimal.ONE)
         .withValue("taskTypeId", TaskType.PACK.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new PackCompletionHandler().handle(packTask);

      /////////////////////////////////////////////////////////////////////////
      // Verify order is now PACKED                                          //
      /////////////////////////////////////////////////////////////////////////
      order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PACKED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify carton is PACKED                                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      assertThat(carton.getValueInteger("statusId")).isEqualTo(CartonStatus.PACKED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify a LOAD task was created                                      //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput loadQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.LOAD.getPossibleValueId()))));
      assertThat(loadQuery.getRecords()).isNotEmpty();
   }
}
