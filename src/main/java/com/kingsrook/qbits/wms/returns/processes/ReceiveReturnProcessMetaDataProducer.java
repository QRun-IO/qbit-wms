/*******************************************************************************
 ** MetaData producer for the ReceiveReturn process.  Receives returned items
 ** against an RMA, creates return receipt records, and inserts a RETURN
 ** inventory transaction at a quarantine location.
 *******************************************************************************/
package com.kingsrook.qbits.wms.returns.processes;


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


public class ReceiveReturnProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsReceiveReturn";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Receive Return")
         .withIcon(new QIcon().withName("assignment_returned"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Enter RMA and receipt details                                //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterReceipt")
            .withLabel("Receipt Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("returnAuthorizationId", QFieldType.INTEGER).withLabel("RMA ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("receivedBy", QFieldType.STRING).withLabel("Received By"))
            .withFormField(new QFieldMetaData("carrierName", QFieldType.STRING).withLabel("Carrier"))
            .withFormField(new QFieldMetaData("trackingNumber", QFieldType.STRING).withLabel("Tracking Number")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Create receipt (backend)                                     //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("createReceipt")
            .withCode(new QCodeReference(ReceiveReturnStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showReceipt")
            .withLabel("Receipt Created")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("returnReceiptId", QFieldType.INTEGER).withLabel("Receipt ID")));
   }
}
