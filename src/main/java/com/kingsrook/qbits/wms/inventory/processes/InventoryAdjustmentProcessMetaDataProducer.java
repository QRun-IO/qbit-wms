/*******************************************************************************
 ** MetaData producer for the InventoryAdjustment process.  Allows supervisors
 ** to manually adjust inventory quantities at a specific location with a
 ** reason code and notes.
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
import com.kingsrook.qbits.wms.core.enums.AdjustmentReasonCode;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLocation;


public class InventoryAdjustmentProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsInventoryAdjust";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Inventory Adjustment")
         .withIcon(new QIcon().withName("tune"))
         .withTableName(WmsInventory.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select inventory location and item                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectInventory")
            .withLabel("Select Inventory")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("locationId", QFieldType.INTEGER).withLabel("Location").withPossibleValueSourceName(WmsLocation.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("itemId", QFieldType.INTEGER).withLabel("Item").withPossibleValueSourceName(WmsItem.TABLE_NAME).withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Enter adjustment details                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterAdjustment")
            .withLabel("Adjustment Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("currentQuantity", QFieldType.DECIMAL).withLabel("Current Quantity").withIsEditable(false))
            .withFormField(new QFieldMetaData("newQuantity", QFieldType.DECIMAL).withLabel("New Quantity").withIsRequired(true))
            .withFormField(new QFieldMetaData("reasonCodeId", QFieldType.INTEGER).withLabel("Reason Code").withPossibleValueSourceName(AdjustmentReasonCode.NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("adjustmentNotes", QFieldType.STRING).withLabel("Notes")))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Execute the adjustment                                       //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeAdjustment")
            .withCode(new QCodeReference(InventoryAdjustmentStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Adjustment Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("adjustedItem", QFieldType.STRING).withLabel("Item"))
            .withViewField(new QFieldMetaData("adjustedLocation", QFieldType.STRING).withLabel("Location"))
            .withViewField(new QFieldMetaData("previousQuantity", QFieldType.DECIMAL).withLabel("Previous Quantity"))
            .withViewField(new QFieldMetaData("adjustedQuantity", QFieldType.DECIMAL).withLabel("New Quantity"))
            .withViewField(new QFieldMetaData("delta", QFieldType.DECIMAL).withLabel("Delta")));
   }
}
