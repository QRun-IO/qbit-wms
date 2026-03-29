/*******************************************************************************
 ** MetaData producer for the ReceiveASN process.  Allows warehouse staff
 ** to receive goods against an advance ship notice.  Loads ASN data and
 ** lines for verification, creates receipts with actual vs expected
 ** quantities, updates ASN status, and generates PUTAWAY tasks.
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


public class ReceiveASNProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReceiveASN";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Receive ASN")
         .withIcon(new QIcon().withName("receipt_long"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select ASN                                                   //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectASN")
            .withLabel("Select ASN")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("asnNumber", QFieldType.STRING).withLabel("ASN Number").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Load and validate ASN (backend)                              //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("loadASN")
            .withCode(new QCodeReference(ReceiveASNStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Verify contents (allow quantity edits)                       //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("verifyContents")
            .withLabel("Verify Contents")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("asnId", QFieldType.INTEGER).withLabel("ASN ID").withIsEditable(false))
            .withFormField(new QFieldMetaData("asnNumber", QFieldType.STRING).withLabel("ASN Number").withIsEditable(false))
            .withFormField(new QFieldMetaData("purchaseOrderId", QFieldType.INTEGER).withLabel("Purchase Order ID").withIsEditable(false)))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Process the ASN receipt (backend)                            //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("processASNReceipt")
            .withCode(new QCodeReference(ReceiveASNStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 5: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("ASN Receipt Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("receiptId", QFieldType.INTEGER).withLabel("Receipt ID"))
            .withViewField(new QFieldMetaData("linesProcessed", QFieldType.INTEGER).withLabel("Lines Processed")));
   }
}
