/*******************************************************************************
 ** MetaData producer for the InventoryRelease process.  Releases an active
 ** hold on inventory, restoring the inventory status to AVAILABLE and creating
 ** a RELEASE transaction.
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
import com.kingsrook.qbits.wms.core.model.WmsInventoryHold;


public class InventoryReleaseProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsInventoryRelease";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Release Inventory Hold")
         .withIcon(new QIcon().withName("lock_open"))
         .withTableName(WmsInventoryHold.TABLE_NAME)

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select active hold                                           //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectHold")
            .withLabel("Select Hold to Release")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("holdId", QFieldType.INTEGER).withLabel("Hold ID").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Confirm release                                              //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmRelease")
            .withLabel("Confirm Release")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("holdDetails", QFieldType.STRING).withLabel("Hold Details")))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Execute the release                                          //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeRelease")
            .withCode(new QCodeReference(InventoryReleaseStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Show result                                                  //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Hold Released")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("affectedRecords", QFieldType.INTEGER).withLabel("Affected Inventory Records")));
   }
}
