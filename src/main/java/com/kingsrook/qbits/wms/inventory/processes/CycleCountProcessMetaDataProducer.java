/*******************************************************************************
 ** MetaData producer for the CreateCycleCount process.  Generates a count plan
 ** with lines for selected locations and creates COUNT tasks that enter the
 ** task queue for worker assignment.
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qbits.wms.core.enums.CycleCountType;
import com.kingsrook.qbits.wms.core.model.WmsCycleCount;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;
import com.kingsrook.qbits.wms.core.model.WmsZone;


public class CycleCountProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsCreateCycleCount";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Create Cycle Count")
         .withIcon(new QIcon().withName("exposure"))
         .withTableName(WmsCycleCount.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select count parameters                                      //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectCountType")
            .withLabel("Count Parameters")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse").withPossibleValueSourceName(WmsWarehouse.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("countTypeId", QFieldType.INTEGER).withLabel("Count Type").withPossibleValueSourceName(CycleCountType.NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("zoneId", QFieldType.INTEGER).withLabel("Target Zone").withPossibleValueSourceName(WmsZone.TABLE_NAME))
            .withFormField(new QFieldMetaData("assignedTo", QFieldType.STRING).withLabel("Assign To")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Generate count list and create tasks                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("generateCountList")
            .withCode(new QCodeReference(CycleCountStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Show count plan summary                                      //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showCountPlan")
            .withLabel("Count Plan Created")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("cycleCountId", QFieldType.INTEGER).withLabel("Cycle Count ID"))
            .withViewField(new QFieldMetaData("lineCount", QFieldType.INTEGER).withLabel("Lines Created"))
            .withViewField(new QFieldMetaData("taskCount", QFieldType.INTEGER).withLabel("Tasks Created")));
   }
}
