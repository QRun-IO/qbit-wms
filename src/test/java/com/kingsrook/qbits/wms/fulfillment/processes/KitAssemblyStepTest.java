/*******************************************************************************
 ** Unit tests for KitAssemblyStep.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.TaskStatus;
import com.kingsrook.qbits.wms.core.enums.TaskType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class KitAssemblyStepTest extends BaseTest
{

   /*******************************************************************************
    ** Test that running kit assembly creates a KIT_ASSEMBLE task.
    *******************************************************************************/
   @Test
   void testRun_validKitBom_createsKitAssembleTask() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer kitItemId = insertItem("KIT-001", "Test Kit");
      Integer componentItemId = insertItem("COMP-001", "Component A");
      Integer locationId = insertLocation(warehouseId);
      insertKitBom(kitItemId, componentItemId, 2);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("warehouseId", warehouseId);
      input.addValue("kitItemId", kitItemId);
      input.addValue("quantity", 5);
      input.addValue("destinationLocationId", locationId);

      RunBackendStepOutput output = new RunBackendStepOutput();
      new KitAssemblyStep().run(input, output);

      /////////////////////////////////////////////////////////////////////////
      // Verify KIT_ASSEMBLE task was created                                //
      /////////////////////////////////////////////////////////////////////////
      List<QRecord> tasks = new QueryAction().execute(new QueryInput(WmsTask.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("taskTypeId", QCriteriaOperator.EQUALS, TaskType.KIT_ASSEMBLE.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, kitItemId)))).getRecords();

      assertThat(tasks).hasSize(1);
      assertThat(tasks.get(0).getValueInteger("taskStatusId")).isEqualTo(TaskStatus.PENDING.getPossibleValueId());
      assertThat(output.getValueString("resultMessage")).contains("Kit assembly task created");
   }
}
