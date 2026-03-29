/*******************************************************************************
 ** MetaData producer for the LowStockAlertCheck process.  Scheduled process
 ** that finds items where total on-hand inventory is at or below the item's
 ** reorder point.
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


public class LowStockAlertCheckProcessMetaDataProducer implements MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "wmsLowStockAlertCheck";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withLabel("Low Stock Alert Check")
         .withIcon(new QIcon().withName("notification_important"))

         .withStep(new QBackendStepMetaData()
            .withName("checkLowStock")
            .withCode(new QCodeReference(LowStockAlertCheckStep.class)))

         .withStep(new QFrontendStepMetaData()
            .withName("confirmResult")
            .withLabel("Low Stock Check Complete")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.VIEW_FORM))
            .withViewField(new QFieldMetaData("resultMessage", QFieldType.STRING).withLabel("Result"))
            .withViewField(new QFieldMetaData("alertCount", QFieldType.INTEGER).withLabel("Alerts Found")));
   }
}
