/*******************************************************************************
 ** Unit tests for {@link CountTaskCompletionHandler}.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.completion;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CycleCountStatus;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsCycleCountLine;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class CountTaskCompletionHandlerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that zero variance auto-approves the cycle count line.
    *******************************************************************************/
   @Test
   void testHandle_zeroVariance_autoApproves() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // Create a task with expected == counted (zero variance)        //
      ///////////////////////////////////////////////////////////////////
      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("expectedQuantity", new BigDecimal("50"))
         .withValue("countedQuantity", new BigDecimal("50"))
         .withValue("cycleCountId", cycleCountId)
         .withValue("isBlindCount", false)
      )).getRecords().get(0);

      Integer taskId = task.getValueInteger("id");

      ///////////////////////////////////////////////////////////////////
      // Create the cycle count line linked to this task               //
      ///////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("50"))
         .withValue("status", "PENDING")
         .withValue("taskId", taskId)
      ));

      new CountTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Verify variance is zero on the task                           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME));
      assertThat(taskQuery.getRecords().get(0).getValueBigDecimal("variance"))
         .isEqualByComparingTo(BigDecimal.ZERO);

      ///////////////////////////////////////////////////////////////////
      // Verify cycle count line was auto-approved                     //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      QRecord line = lineQuery.getRecords().get(0);
      assertThat(line.getValueString("status")).isEqualTo("APPROVED");
      assertThat(line.getValueBigDecimal("countedQuantity")).isEqualByComparingTo(new BigDecimal("50"));
   }



   /*******************************************************************************
    ** Test that small non-zero variance marks line as COUNTED (for review).
    *******************************************************************************/
   @Test
   void testHandle_smallVariance_markedForReview() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("expectedQuantity", new BigDecimal("100"))
         .withValue("countedQuantity", new BigDecimal("98"))
         .withValue("cycleCountId", cycleCountId)
         .withValue("isBlindCount", false)
      )).getRecords().get(0);

      Integer taskId = task.getValueInteger("id");

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("100"))
         .withValue("status", "PENDING")
         .withValue("taskId", taskId)
      ));

      new CountTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Line should be COUNTED (awaiting supervisor review)           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsCycleCountLine.TABLE_NAME));
      QRecord line = lineQuery.getRecords().get(0);
      assertThat(line.getValueString("status")).isEqualTo("COUNTED");
      assertThat(line.getValueBigDecimal("variance")).isEqualByComparingTo(new BigDecimal("-2"));
   }



   /*******************************************************************************
    ** Test that a large variance on a blind count triggers automatic recount.
    *******************************************************************************/
   @Test
   void testHandle_largeVarianceBlindCount_triggersRecount() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // 50 expected, 30 counted = 40% variance > 10% threshold       //
      ///////////////////////////////////////////////////////////////////
      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("expectedQuantity", new BigDecimal("50"))
         .withValue("countedQuantity", new BigDecimal("30"))
         .withValue("cycleCountId", cycleCountId)
         .withValue("isBlindCount", true)
      )).getRecords().get(0);

      Integer taskId = task.getValueInteger("id");

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("50"))
         .withValue("status", "PENDING")
         .withValue("taskId", taskId)
      ));

      new CountTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // A new recount task should have been created                   //
      ///////////////////////////////////////////////////////////////////
      QueryOutput taskQuery = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("recountRequired", QCriteriaOperator.EQUALS, true))));

      assertThat(taskQuery.getRecords()).hasSize(1);
      QRecord recountTask = taskQuery.getRecords().get(0);
      assertThat(recountTask.getValueInteger("taskTypeId")).isEqualTo(TaskType.COUNT.getPossibleValueId());
      assertThat(recountTask.getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      assertThat(recountTask.getValueBoolean("isBlindCount")).isFalse();
      assertThat(recountTask.getValueString("notes")).contains("Auto-generated recount");
   }



   /*******************************************************************************
    ** Test that when all cycle count lines are approved, the cycle count is
    ** marked COMPLETED.
    *******************************************************************************/
   @Test
   void testHandle_allLinesApproved_cycleCountCompleted() throws QException
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer locationId = insertLocation(warehouseId);
      Integer cycleCountId = insertCycleCount(warehouseId);

      ///////////////////////////////////////////////////////////////////
      // Create a task with zero variance so line is auto-approved     //
      ///////////////////////////////////////////////////////////////////
      QRecord task = new InsertAction().execute(new InsertInput(WmsTask.TABLE_NAME).withRecord(new QRecord()
         .withValue("warehouseId", warehouseId)
         .withValue("taskTypeId", TaskType.COUNT.getPossibleValueId())
         .withValue("taskStatusId", TaskStatus.COMPLETED.getPossibleValueId())
         .withValue("priority", 5)
         .withValue("itemId", itemId)
         .withValue("sourceLocationId", locationId)
         .withValue("expectedQuantity", new BigDecimal("10"))
         .withValue("countedQuantity", new BigDecimal("10"))
         .withValue("cycleCountId", cycleCountId)
         .withValue("isBlindCount", false)
      )).getRecords().get(0);

      Integer taskId = task.getValueInteger("id");

      new InsertAction().execute(new InsertInput(WmsCycleCountLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cycleCountId", cycleCountId)
         .withValue("locationId", locationId)
         .withValue("itemId", itemId)
         .withValue("expectedQuantity", new BigDecimal("10"))
         .withValue("status", "PENDING")
         .withValue("taskId", taskId)
      ));

      new CountTaskCompletionHandler().handle(task);

      ///////////////////////////////////////////////////////////////////
      // Cycle count should now be COMPLETED                           //
      ///////////////////////////////////////////////////////////////////
      QueryOutput ccQuery = new QueryAction().execute(new QueryInput(WmsCycleCount.TABLE_NAME));
      QRecord cc = ccQuery.getRecords().get(0);
      assertThat(cc.getValueInteger("cycleCountStatusId")).isEqualTo(CycleCountStatus.COMPLETED.getPossibleValueId());
   }
}
