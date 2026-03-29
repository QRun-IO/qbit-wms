/*******************************************************************************
 ** MetaData producer for the ReceiveAgainstPO process.  Allows warehouse
 ** staff to receive goods against an existing purchase order, validate
 ** received items, create receipt records, and generate PUTAWAY and QC
 ** tasks as appropriate.
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


public class ReceiveAgainstPOProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReceiveAgainstPO";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Receive Against PO")
         .withIcon(new QIcon().withName("local_shipping"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select PO                                                    //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectPO")
            .withLabel("Select Purchase Order")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("poNumber", QFieldType.STRING).withLabel("PO Number"))
            .withFormField(new QFieldMetaData("purchaseOrderId", QFieldType.INTEGER).withLabel("Purchase Order ID")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Load and validate PO (backend)                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("loadPO")
            .withCode(new QCodeReference(ReceiveAgainstPOStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Scan items                                                   //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("scanItems")
            .withLabel("Scan Items")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("itemBarcode", QFieldType.STRING).withLabel("Item Barcode").withIsRequired(true))
            .withFormField(new QFieldMetaData("quantity", QFieldType.INTEGER).withLabel("Quantity").withIsRequired(true))
            .withFormField(new QFieldMetaData("lotNumber", QFieldType.STRING).withLabel("Lot Number"))
            .withFormField(new QFieldMetaData("expirationDate", QFieldType.DATE).withLabel("Expiration Date"))
            .withFormField(new QFieldMetaData("conditionId", QFieldType.INTEGER).withLabel("Condition").withPossibleValueSourceName(ConditionCode.NAME)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Process the receipt (backend)                                //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("processReceipt")
            .withCode(new QCodeReference(ReceiveAgainstPOStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 5: Confirmation                                                 //
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
