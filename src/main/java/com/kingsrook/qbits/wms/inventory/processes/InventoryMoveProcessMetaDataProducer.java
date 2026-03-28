/*******************************************************************************
 ** MetaData producer for the InventoryMove process.  Creates a MOVE task and
 ** immediately completes it for manual inventory moves outside the task queue
 ** (e.g., supervisor-initiated immediate relocations).
 *******************************************************************************/
package com.kingsrook.qbits.wms.inventory.processes;


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
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLocation;


public class InventoryMoveProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsInventoryMove";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Move Inventory")
         .withIcon(new QIcon().withName("swap_horiz"))
         .withTableName(WmsInventory.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Scan source location                                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanSource")
            .withLabel("Source Location")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("sourceLocationId", QFieldType.INTEGER).withLabel("Source Location").withPossibleValueSourceName(WmsLocation.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("itemId", QFieldType.INTEGER).withLabel("Item").withPossibleValueSourceName(WmsItem.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("quantityToMove", QFieldType.DECIMAL).withLabel("Quantity to Move").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Scan destination                                             //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanDestination")
            .withLabel("Destination Location")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("destinationLocationId", QFieldType.INTEGER).withLabel("Destination Location").withPossibleValueSourceName(WmsLocation.TABLE_NAME).withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Execute the move                                             //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeMove")
            .withCode(new QCodeReference(InventoryMoveStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Move Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("movedTaskId", QFieldType.INTEGER).withLabel("Task ID")));
   }
}
