/*******************************************************************************
 ** MetaData producer for the GenerateInvoice process.  Provides a frontend step
 ** to select client and billing period, then a backend step that queries unbilled
 ** billing activities, applies rates from the active rate card, and creates an
 ** invoice with line items.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


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


public class GenerateInvoiceProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsGenerateInvoice";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Generate Invoice")
         .withIcon(new QIcon().withName("receipt_long"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select client and billing period                             //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectParameters")
            .withLabel("Invoice Parameters")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("clientId", QFieldType.INTEGER).withLabel("Client ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("billingPeriodStart", QFieldType.DATE).withLabel("Billing Period Start").withIsRequired(true))
            .withFormField(new QFieldMetaData("billingPeriodEnd", QFieldType.DATE).withLabel("Billing Period End").withIsRequired(true)))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Generate invoice (backend)                                   //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("generateInvoice")
            .withCode(new QCodeReference(GenerateInvoiceStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Invoice Generated")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("invoiceId", QFieldType.INTEGER).withLabel("Invoice ID"))
            .withViewField(new QFieldMetaData("invoiceTotal", QFieldType.STRING).withLabel("Invoice Total")));
   }
}
