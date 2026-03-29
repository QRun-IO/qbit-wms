/*******************************************************************************
 ** MetaData producer for the KitAssembly process.  Selects a kit item, loads
 ** its BOM, and creates KIT_ASSEMBLE tasks for the assembly.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


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


public class KitAssemblyProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsKitAssembly";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Kit Assembly")
         .withIcon(new QIcon().withName("build"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select kit to assemble                                       //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectKit")
            .withLabel("Select Kit")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("kitItemId", QFieldType.INTEGER).withLabel("Kit Item ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("quantity", QFieldType.INTEGER).withLabel("Quantity").withIsRequired(true))
            .withFormField(new QFieldMetaData("destinationLocationId", QFieldType.INTEGER).withLabel("Destination Location")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Create kit assembly tasks (backend)                          //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("createKitTasks")
            .withCode(new QCodeReference(KitAssemblyStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showTasks")
            .withLabel("Tasks Created")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("tasksCreated", QFieldType.INTEGER).withLabel("Tasks Created")));
   }
}
