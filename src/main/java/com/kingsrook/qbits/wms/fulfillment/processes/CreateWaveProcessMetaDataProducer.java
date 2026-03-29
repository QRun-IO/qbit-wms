/*******************************************************************************
 ** MetaData producer for the CreateWave process.  Defines a multi-step flow:
 ** a frontend to set wave criteria, a backend to find matching orders, a
 ** review frontend, and a backend to create the wave and assign orders.
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
import com.kingsrook.qbits.wms.core.enums.ShippingMode;
import com.kingsrook.qbits.wms.core.enums.WaveType;


public class CreateWaveProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsCreateWave";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Create Wave")
         .withIcon(new QIcon().withName("waves"))

         //////////////////////////////////////////////////////////////////////////
         // Step 1: Define wave criteria                                         //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("defineCriteria")
            .withLabel("Define Wave Criteria")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withLabel("Warehouse ID").withIsRequired(true))
            .withFormField(new QFieldMetaData("waveTypeId", QFieldType.INTEGER).withLabel("Wave Type").withPossibleValueSourceName(WaveType.NAME))
            .withFormField(new QFieldMetaData("carrierId", QFieldType.INTEGER).withLabel("Carrier ID"))
            .withFormField(new QFieldMetaData("shippingModeId", QFieldType.INTEGER).withLabel("Service Level").withPossibleValueSourceName(ShippingMode.NAME))
            .withFormField(new QFieldMetaData("shipByDate", QFieldType.DATE).withLabel("Ship By Date (on or before)")))

         //////////////////////////////////////////////////////////////////////////
         // Step 2: Find matching orders (backend)                               //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("findOrders")
            .withCode(new QCodeReference(CreateWaveFindStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 3: Review matching orders                                       //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("reviewOrders")
            .withLabel("Review Matching Orders")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("matchingOrderCount", QFieldType.INTEGER).withLabel("Matching Orders")))

         //////////////////////////////////////////////////////////////////////////
         // Step 4: Execute wave creation (backend)                              //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QBackendStepMetaData()
            .withName("executeWave")
            .withCode(new QCodeReference(CreateWaveExecuteStep.class)))

         //////////////////////////////////////////////////////////////////////////
         // Step 5: Confirmation                                                 //
         //////////////////////////////////////////////////////////////////////////
         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Wave Created")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("waveId", QFieldType.INTEGER).withLabel("Wave ID"))
            .withViewField(new QFieldMetaData("ordersInWave", QFieldType.INTEGER).withLabel("Orders in Wave")));
   }
}
