/*******************************************************************************
 ** MetaData producer for the ExpirationAlertCheck process.  Scheduled process
 ** that finds inventory with expiration dates within a configurable threshold
 ** number of days.
 *******************************************************************************/
package com.kingsrook.qbits.wms.advanced.processes;


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


public class ExpirationAlertCheckProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsExpirationAlertCheck";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Expiration Alert Check")
         .withIcon(new QIcon().withName("event_busy"))

         .withStep(new QFrontendStepMetaData()
            .withName("parameters")
            .withLabel("Parameters")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
            .withFormField(new QFieldMetaData("thresholdDays", QFieldType.INTEGER).withLabel("Threshold Days").withIsRequired(true)))

         .withStep(new QBackendStepMetaData()
            .withName("checkExpirations")
            .withCode(new QCodeReference(ExpirationAlertCheckStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Alert Check Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("alertCount", QFieldType.INTEGER).withLabel("Alerts Found")));
   }
}
