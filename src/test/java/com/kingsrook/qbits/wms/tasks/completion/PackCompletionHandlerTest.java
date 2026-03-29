/*******************************************************************************
 ** Unit tests for PackCompletionHandler.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
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
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class PackCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that completing a pack task marks carton as PACKED and when all
    ** cartons are packed, advances order to PACKED and creates LOAD task.
    *******************************************************************************/
   @Test
   void testHandle_singleCarton_advancesOrderToPacked() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      Integer lineId = insertOrderLine(orderId, itemId, 10);
      Integer cartonId = insertCarton(orderId);

      /////////////////////////////////////////////////////////////////////////
      // Set order to PICKED status                                          //
      /////////////////////////////////////////////////////////////////////////
      new UpdateAction().execute(new UpdateInput(WmsOrder.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", orderId)
         .withValue("statusId", OrderStatus.PICKED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Add carton line                                                     //
      /////////////////////////////////////////////////////////////////////////
      insertCartonLine(cartonId, itemId, 10);

      QRecord task = new QRecord()
         .withValue("id", 1)
         .withValue("warehouseId", warehouseId)
         .withValue("orderId", orderId)
         .withValue("cartonId", cartonId)
         .withValue("quantityCompleted", BigDecimal.ONE)
         .withValue("taskTypeId", TaskType.PACK.getPossibleValueId())
         .withValue("completedBy", "TestUser");

      new PackCompletionHandler().handle(task);

      /////////////////////////////////////////////////////////////////////////
      // Verify carton is PACKED                                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      assertThat(carton.getValueInteger("statusId")).isEqualTo(CartonStatus.PACKED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify order advanced to PACKED                                     //
      /////////////////////////////////////////////////////////////////////////
      QRecord order = new GetAction().execute(new GetInput(WmsOrder.TABLE_NAME).withPrimaryKey(orderId)).getRecord();
      assertThat(order.getValueInteger("statusId")).isEqualTo(OrderStatus.PACKED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify a LOAD task was created                                      //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.LOAD.getPossibleValueId()))));
      assertThat(taskQuery.getRecords()).hasSize(1);
   }
}
