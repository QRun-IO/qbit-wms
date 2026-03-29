/*******************************************************************************
 ** Backend step for PackOrder scan phase.  Validates the scanned item barcode
 ** against expected order lines and records the scan into a carton line.
 *******************************************************************************/
package com.kingsrook.qbits.wms.fulfillment.processes;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCartonLine;
import com.kingsrook.qbits.wms.fulfillment.model.WmsOrderLine;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class PackOrderScanStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(PackOrderScanStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer cartonId = input.getValueInteger("cartonId");
      Integer orderId = input.getValueInteger("orderId");
      String itemBarcode = input.getValueString("itemBarcode");
      Integer quantity = input.getValueInteger("quantity");

      if(itemBarcode == null || itemBarcode.isBlank())
      {
         throw new QUserFacingException("Item barcode is required.");
      }
      if(quantity == null || quantity <= 0)
      {
         throw new QUserFacingException("Quantity must be greater than zero.");
      }

      /////////////////////////////////////////////////////////////////////////
      // Resolve item by barcode                                             //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput itemQuery = new QueryAction().execute(new QueryInput(WmsItem.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("barcodeUpc", QCriteriaOperator.EQUALS, itemBarcode))
            .withBooleanOperator(QQueryFilter.BooleanOperator.OR)
            .withCriteria(new QFilterCriteria("sku", QCriteriaOperator.EQUALS, itemBarcode))));

      if(itemQuery.getRecords().isEmpty())
      {
         throw new QUserFacingException("Item not found for barcode: " + itemBarcode);
      }

      Integer itemId = itemQuery.getRecords().get(0).getValueInteger("id");

      /////////////////////////////////////////////////////////////////////////
      // Find matching order line                                            //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput lineQuery = new QueryAction().execute(new QueryInput(WmsOrderLine.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("orderId", QCriteriaOperator.EQUALS, orderId))
            .withCriteria(new QFilterCriteria("itemId", QCriteriaOperator.EQUALS, itemId))));

      Integer orderLineId = null;
      if(!lineQuery.getRecords().isEmpty())
      {
         orderLineId = lineQuery.getRecords().get(0).getValueInteger("id");
      }

      /////////////////////////////////////////////////////////////////////////
      // Create carton line                                                  //
      /////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(WmsCartonLine.TABLE_NAME).withRecord(new QRecord()
         .withValue("cartonId", cartonId)
         .withValue("orderLineId", orderLineId)
         .withValue("itemId", itemId)
         .withValue("quantity", quantity)));

      LOG.info("Carton line added", logPair("cartonId", cartonId), logPair("itemId", itemId), logPair("quantity", quantity));

      output.addValue("resultMessage", "Item scanned into carton.");
   }
}
