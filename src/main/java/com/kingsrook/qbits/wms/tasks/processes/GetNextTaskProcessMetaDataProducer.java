/*******************************************************************************
 ** MetaData producer for the GetNextTask process.  Presents a mobile workflow
 ** where a worker selects optional zone/equipment filters, receives their next
 ** highest-priority PENDING task, and can begin working it.
 *******************************************************************************/
package com.kingsrook.qbits.wms.tasks.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
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
import com.kingsrook.qbits.wms.core.enums.EquipmentType;
import com.kingsrook.qbits.wms.core.model.WmsTask;
import com.kingsrook.qbits.wms.core.model.WmsZone;


public class GetNextTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsGetNextTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Get Next Task")
         .withIcon(new QIcon().withName("assignment_ind"))
         .withTableName(WmsTask.TABLE_NAME)
         .withStep(new QFrontendStepMetaData()
            .withName("enterCriteria")
            .withLabel("Select Filters")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("workerUserId", QFieldType.STRING).withLabel("Worker ID"))
            .withFormField(new QFieldMetaData("zoneId", QFieldType.INTEGER).withLabel("Zone").withPossibleValueSourceName(WmsZone.TABLE_NAME))
            .withFormField(new QFieldMetaData("equipmentTypeId", QFieldType.INTEGER).withLabel("Equipment Type").withPossibleValueSourceName(EquipmentType.NAME)))
         .withStep(new QBackendStepMetaData()
            .withName("findAndAssign")
            .withCode(new QCodeReference(GetNextTaskStep.class)))
         .withStep(new QFrontendStepMetaData()
            .withName("showTask")
            .withLabel("Your Next Task")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID"))
            .withViewField(new QFieldMetaData("taskType", QFieldType.STRING).withLabel("Task Type"))
            .withViewField(new QFieldMetaData("sourceLocation", QFieldType.STRING).withLabel("Source Location"))
            .withViewField(new QFieldMetaData("itemName", QFieldType.STRING).withLabel("Item"))
            .withViewField(new QFieldMetaData("quantityRequested", QFieldType.DECIMAL).withLabel("Quantity"))
            .withViewField(new QFieldMetaData("destinationLocation", QFieldType.STRING).withLabel("Destination Location"))
            .withViewField(new QFieldMetaData("taskNotes", QFieldType.STRING).withLabel("Notes"))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Status")));
   }
}
