/*******************************************************************************
 ** MetaData producer for the InventoryHold process.  Places a hold on
 ** inventory by creating a wms_inventory_hold record, creating a HOLD
 ** transaction, and updating wms_inventory to ON_HOLD status.
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
import com.kingsrook.qbits.wms.core.enums.HoldType;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLocation;


public class InventoryHoldProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsInventoryHold";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Place Inventory Hold")
         .withIcon(new QIcon().withName("lock"))
         .withTableName(WmsInventory.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select inventory to hold                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectInventory")
            .withLabel("Select Inventory")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("itemId", QFieldType.INTEGER).withLabel("Item").withPossibleValueSourceName(WmsItem.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("lotNumber", QFieldType.STRING).withLabel("Lot Number"))
            .withFormField(new QFieldMetaData("locationId", QFieldType.INTEGER).withLabel("Location").withPossibleValueSourceName(WmsLocation.TABLE_NAME)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Enter hold details                                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterHoldDetails")
            .withLabel("Hold Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("holdTypeId", QFieldType.INTEGER).withLabel("Hold Type").withPossibleValueSourceName(HoldType.NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("holdReason", QFieldType.STRING).withLabel("Reason").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Execute the hold                                             //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeHold")
            .withCode(new QCodeReference(InventoryHoldStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Hold Placed")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("affectedRecords", QFieldType.INTEGER).withLabel("Affected Inventory Records")));
   }
}
