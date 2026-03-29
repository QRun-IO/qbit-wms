/*******************************************************************************
 ** MetaData producer for the AllocateOrders process.  Provides a frontend step
 ** to select PENDING orders filtered by priority, ship-by date, and status,
 ** then a backend step that allocates inventory for each order line.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


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


public class AllocateOrdersProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsAllocateOrders";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Allocate Orders")
         .withIcon(new QIcon().withName("assignment_turned_in"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Select orders to allocate                                    //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("selectOrders")
            .withLabel("Select Orders")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("minPriority", QFieldType.INTEGER).withLabel("Minimum Priority"))
            .withFormField(new QFieldMetaData("shipByDate", QFieldType.DATE).withLabel("Ship By Date (on or before)"))
            .withFormField(new QFieldMetaData("clientId", QFieldType.INTEGER).withLabel("Client ID")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Allocate inventory against order lines (backend)             //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("allocate")
            .withCode(new QCodeReference(AllocateOrdersStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Allocation Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("ordersAllocated", QFieldType.INTEGER).withLabel("Orders Allocated")));
   }
}
