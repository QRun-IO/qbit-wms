/*******************************************************************************
 ** Backend step for StorageSnapshot.  For each active client, counts occupied
 ** pallet positions and bin positions in wmsInventory, then creates STORAGE
 ** billing activities with appropriate activity types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.billing.processes;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
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
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qbits.wms.billing.model.WmsBillingActivity;
import com.kingsrook.qbits.wms.core.enums.BillingActivityType;
import com.kingsrook.qbits.wms.core.enums.LocationType;
import com.kingsrook.qbits.wms.core.model.WmsClient;
import com.kingsrook.qbits.wms.core.model.WmsInventory;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class StorageSnapshotStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(StorageSnapshotStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Query all active clients                                            //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput clientQuery = new QueryAction().execute(new QueryInput(WmsClient.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))));

      int activitiesCreated = 0;

      for(QRecord client : clientQuery.getRecords())
      {
         Integer clientId = client.getValueInteger("id");

         /////////////////////////////////////////////////////////////////////
         // Query inventory records for this client with quantity on hand   //
         /////////////////////////////////////////////////////////////////////
         QueryOutput inventoryQuery = new QueryAction().execute(new QueryInput(WmsInventory.TABLE_NAME)
            .withFilter(new QQueryFilter()
               .withCriteria(new QFilterCriteria("clientId", QCriteriaOperator.EQUALS, clientId))
               .withCriteria(new QFilterCriteria("quantityOnHand", QCriteriaOperator.GREATER_THAN, 0))));

         if(inventoryQuery.getRecords().isEmpty())
         {
            continue;
         }

         /////////////////////////////////////////////////////////////////////
         // Build a map of locationId to locationType for occupied locs     //
         /////////////////////////////////////////////////////////////////////
         Map<Integer, Integer> locationTypes = new LinkedHashMap<>();
         for(QRecord inv : inventoryQuery.getRecords())
         {
            Integer locationId = inv.getValueInteger("locationId");
            if(locationId != null && !locationTypes.containsKey(locationId))
            {
               locationTypes.put(locationId, null);
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Query locations to determine type                               //
         /////////////////////////////////////////////////////////////////////
         if(!locationTypes.isEmpty())
         {
            QueryOutput locationQuery = new QueryAction().execute(new QueryInput(WmsLocation.TABLE_NAME)
               .withFilter(new QQueryFilter()
                  .withCriteria(new QFilterCriteria("id", QCriteriaOperator.IN, locationTypes.keySet().stream().toList()))));

            for(QRecord loc : locationQuery.getRecords())
            {
               locationTypes.put(loc.getValueInteger("id"), loc.getValueInteger("locationTypeId"));
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Count pallet and bin positions                                  //
         /////////////////////////////////////////////////////////////////////
         int palletPositions = 0;
         int binPositions = 0;
         Integer warehouseId = null;

         for(QRecord inv : inventoryQuery.getRecords())
         {
            if(warehouseId == null)
            {
               warehouseId = inv.getValueInteger("warehouseId");
            }

            Integer locationId = inv.getValueInteger("locationId");
            Integer locTypeId = locationTypes.get(locationId);

            if(locTypeId != null && locTypeId.equals(LocationType.PALLET_POSITION.getId()))
            {
               palletPositions++;
            }
            else
            {
               binPositions++;
            }
         }

         /////////////////////////////////////////////////////////////////////
         // Create STORAGE_PER_PALLET_DAY activity                         //
         /////////////////////////////////////////////////////////////////////
         if(palletPositions > 0 && warehouseId != null)
         {
            new InsertAction().execute(new InsertInput(WmsBillingActivity.TABLE_NAME).withRecord(new QRecord()
               .withValue("warehouseId", warehouseId)
               .withValue("clientId", clientId)
               .withValue("activityTypeId", BillingActivityType.STORAGE_PER_PALLET_DAY.getPossibleValueId())
               .withValue("activityDate", Instant.now())
               .withValue("quantity", new BigDecimal(palletPositions))
               .withValue("referenceType", "STORAGE_SNAPSHOT")
               .withValue("isBilled", false)));
            activitiesCreated++;
         }

         /////////////////////////////////////////////////////////////////////
         // Create STORAGE_PER_BIN_DAY activity                            //
         /////////////////////////////////////////////////////////////////////
         if(binPositions > 0 && warehouseId != null)
         {
            new InsertAction().execute(new InsertInput(WmsBillingActivity.TABLE_NAME).withRecord(new QRecord()
               .withValue("warehouseId", warehouseId)
               .withValue("clientId", clientId)
               .withValue("activityTypeId", BillingActivityType.STORAGE_PER_BIN_DAY.getPossibleValueId())
               .withValue("activityDate", Instant.now())
               .withValue("quantity", new BigDecimal(binPositions))
               .withValue("referenceType", "STORAGE_SNAPSHOT")
               .withValue("isBilled", false)));
            activitiesCreated++;
         }

         LOG.info("Storage snapshot for client",
            logPair("clientId", clientId),
            logPair("palletPositions", palletPositions),
            logPair("binPositions", binPositions));
      }

      output.addValue("resultMessage", "Storage snapshot complete. " + activitiesCreated + " billing activities created.");
      output.addValue("activitiesCreated", activitiesCreated);
   }
}
