/*******************************************************************************
 ** MetaData producer for the CompleteTask process.  The central mobile workflow
 ** that walks a worker through scan verification (source location, item,
 ** quantity, destination) and then fires the TaskCompletionDispatcher for
 ** type-specific side effects.
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
import com.kingsrook.qbits.wms.core.model.WmsTask;


public class CompleteTaskProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsCompleteTask";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Complete Task")
         .withIcon(new QIcon().withName("task_alt"))
         .withTableName(WmsTask.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Scan source location barcode                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanSource")
            .withLabel("Scan Source Location")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("taskId", QFieldType.INTEGER).withLabel("Task ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("scannedSourceBarcode", QFieldType.STRING).withLabel("Source Location Barcode")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Validate source location                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("validateSource")
            .withCode(new QCodeReference(CompleteTaskStep.ValidateSourceStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Scan item barcode                                            //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanItem")
            .withLabel("Scan Item")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("scannedItemBarcode", QFieldType.STRING).withLabel("Item Barcode")))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Validate item                                                //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("validateItem")
            .withCode(new QCodeReference(CompleteTaskStep.ValidateItemStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 5: Enter quantity                                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterQuantity")
            .withLabel("Enter Quantity")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("quantityCompleted", QFieldType.DECIMAL).withLabel("Quantity")))

         //////////////////////////////////////////////////////////////////////////
         // Step 6: Scan destination location (for MOVE, PUTAWAY, etc.)          //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanDestination")
            .withLabel("Scan Destination Location")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("scannedDestinationBarcode", QFieldType.STRING).withLabel("Destination Location Barcode")))

         //////////////////////////////////////////////////////////////////////////
         // Step 7: Validate destination                                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("validateDestination")
            .withCode(new QCodeReference(CompleteTaskStep.ValidateDestinationStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 8: Execute completion and dispatch to handler                   //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeCompletion")
            .withCode(new QCodeReference(CompleteTaskStep.ExecuteCompletionStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 9: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showResult")
            .withLabel("Completion Summary")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("completedTaskId", QFieldType.INTEGER).withLabel("Task ID"))
            .withViewField(new QFieldMetaData("completedStatus", QFieldType.STRING).withLabel("Status"))
            .withViewField(new QFieldMetaData("completedQuantity", QFieldType.DECIMAL).withLabel("Quantity Completed")));
   }
}
