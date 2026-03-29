/*******************************************************************************
 ** MetaData producer for the BlindReceive process.  Allows warehouse staff
 ** to receive unexpected goods without an associated purchase order.  Creates
 ** a receipt, receipt lines, and PUTAWAY tasks with directed putaway.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


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
import com.kingsrook.qbits.wms.core.enums.ConditionCode;
import com.kingsrook.qbits.wms.core.model.WmsWarehouse;


public class BlindReceiveProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsBlindReceive";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Blind Receive")
         .withIcon(new QIcon().withName("inventory"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Scan items                                                   //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanItems")
            .withLabel("Scan Items")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse").withPossibleValueSourceName(WmsWarehouse.TABLE_NAME).withIsRequired(true))
            .withFormField(new QFieldMetaData("itemBarcode", QFieldType.STRING).withLabel("Item Barcode").withIsRequired(true))
            .withFormField(new QFieldMetaData("quantity", QFieldType.INTEGER).withLabel("Quantity").withIsRequired(true))
            .withFormField(new QFieldMetaData("lotNumber", QFieldType.STRING).withLabel("Lot Number"))
            .withFormField(new QFieldMetaData("expirationDate", QFieldType.DATE).withLabel("Expiration Date"))
            .withFormField(new QFieldMetaData("conditionId", QFieldType.INTEGER).withLabel("Condition").withPossibleValueSourceName(ConditionCode.NAME)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Process the blind receipt (backend)                          //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("processBlindReceipt")
            .withCode(new QCodeReference(BlindReceiveStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Receipt Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("receiptId", QFieldType.INTEGER).withLabel("Receipt ID"))
            .withViewField(new QFieldMetaData("receiptLineId", QFieldType.INTEGER).withLabel("Receipt Line ID")));
   }
}
