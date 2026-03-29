/*******************************************************************************
 ** MetaData producer for the CreateRMA process.  Looks up the original order,
 ** lets the user select items and quantities to return, and creates a return
 ** authorization with lines.
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


public class CreateRMAProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsCreateRMA";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Create RMA")
         .withIcon(new QIcon().withName("assignment_return"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Enter order and return details                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("enterDetails")
            .withLabel("Return Details")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("orderId", QFieldType.INTEGER).withLabel("Original Order ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("customerName", QFieldType.STRING).withLabel("Customer Name"))
            .withFormField(new QFieldMetaData("reasonCodeId", QFieldType.INTEGER).withLabel("Reason Code"))
            .withFormField(new QFieldMetaData("notes", QFieldType.STRING).withLabel("Notes")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Create RMA (backend)                                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("createRMA")
            .withCode(new QCodeReference(CreateRMAStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("showRMA")
            .withLabel("RMA Created")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("returnAuthorizationId", QFieldType.INTEGER).withLabel("RMA ID"))
            .withViewField(new QFieldMetaData("rmaNumber", QFieldType.STRING).withLabel("RMA Number")));
   }
}
