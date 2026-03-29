/*******************************************************************************
 ** Backend step for DirectedPutaway.  A utility process that determines the
 ** best putaway location for a received item by evaluating wms_putaway_rule
 ** records in priority order.  Falls back to the first available BULK zone
 ** location if no rules match.
 *******************************************************************************/
package com.kingsrook.qbits.wms.receiving.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.core.enums.ZoneType;
import com.kingsrook.qbits.wms.core.model.WmsItem;
import com.kingsrook.qbits.wms.core.model.WmsLocation;
import com.kingsrook.qbits.wms.core.model.WmsZone;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


public class DirectedPutawayStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(DirectedPutawayStep.class);

   public static final String PUTAWAY_RULE_TABLE_NAME = "wmsPutawayRule";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      Integer itemId = input.getValueInteger("itemId");
      Integer warehouseId = input.getValueInteger("warehouseId");
      Integer quantity = input.getValueInteger("quantity");

      LOG.info("Evaluating directed putaway",
         logPair("itemId", itemId),
         logPair("warehouseId", warehouseId),
         logPair("quantity", quantity));

      /////////////////////////////////////////////////////////////////////////
      // Load the item to check storage requirements, velocity, category     //
      /////////////////////////////////////////////////////////////////////////
      GetOutput itemGet = new GetAction().execute(new GetInput(WmsItem.TABLE_NAME).withPrimaryKey(itemId));
      QRecord item = itemGet.getRecord();

      String storageRequirements = item != null ? item.getValueString("storageRequirements") : null;
      String velocityClass = item != null ? item.getValueString("velocityClass") : null;
      Integer itemCategoryId = item != null ? item.getValueInteger("itemCategoryId") : null;

      /////////////////////////////////////////////////////////////////////////
      // Query putaway rules ordered by priority, where active               //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput rulesQuery = new QueryAction().execute(new QueryInput(PUTAWAY_RULE_TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))
            .withOrderBy(new QFilterOrderBy("priority", true))));

      List<QRecord> rules = rulesQuery.getRecords();

      /////////////////////////////////////////////////////////////////////////
      // Evaluate each rule to find the first match                          //
      /////////////////////////////////////////////////////////////////////////
      for(QRecord rule : rules)
      {
         if(ruleMatchesItem(rule, storageRequirements, velocityClass, itemCategoryId))
         {
            Integer targetZoneId = rule.getValueInteger("targetZoneId");
            Integer locationId = findAvailableLocationInZone(targetZoneId, warehouseId);

            if(locationId != null)
            {
               LOG.info("Directed putaway matched rule",
                  logPair("ruleId", rule.getValueInteger("id")),
                  logPair("ruleName", rule.getValueString("ruleName")),
                  logPair("targetZoneId", targetZoneId),
                  logPair("locationId", locationId));

               output.addValue("locationId", locationId);
               return;
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Fallback: find any available location in a BULK zone                //
      /////////////////////////////////////////////////////////////////////////
      LOG.info("No putaway rule matched, falling back to BULK zone",
         logPair("itemId", itemId),
         logPair("warehouseId", warehouseId));

      Integer fallbackLocationId = findFallbackBulkLocation(warehouseId);
      output.addValue("locationId", fallbackLocationId);
   }



   /*******************************************************************************
    ** Check whether a putaway rule matches the given item attributes.  Null
    ** match fields on the rule are treated as wildcards (match anything).
    *******************************************************************************/
   private Boolean ruleMatchesItem(QRecord rule, String storageRequirements, String velocityClass, Integer itemCategoryId)
   {
      String ruleStorage = rule.getValueString("storageRequirementsMatch");
      if(ruleStorage != null && !ruleStorage.equals(storageRequirements))
      {
         return (false);
      }

      String ruleVelocity = rule.getValueString("velocityClassMatch");
      if(ruleVelocity != null && !ruleVelocity.equals(velocityClass))
      {
         return (false);
      }

      Integer ruleCategoryId = rule.getValueInteger("itemCategoryMatch");
      if(ruleCategoryId != null && !ruleCategoryId.equals(itemCategoryId))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    ** Find an available, active location within a specific zone.
    *******************************************************************************/
   private Integer findAvailableLocationInZone(Integer zoneId, Integer warehouseId) throws QException
   {
      QueryOutput locQuery = new QueryAction().execute(new QueryInput(WmsLocation.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("zoneId", QCriteriaOperator.EQUALS, zoneId))
            .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))
            .withOrderBy(new QFilterOrderBy("pickSequence", true))));

      List<QRecord> locations = locQuery.getRecords();
      if(!locations.isEmpty())
      {
         return (locations.get(0).getValueInteger("id"));
      }

      return (null);
   }



   /*******************************************************************************
    ** Fallback: find the first available location in any BULK zone for the
    ** warehouse.
    *******************************************************************************/
   private Integer findFallbackBulkLocation(Integer warehouseId) throws QException
   {
      /////////////////////////////////////////////////////////////////////////
      // Find BULK zones in the warehouse                                    //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput zoneQuery = new QueryAction().execute(new QueryInput(WmsZone.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("warehouseId", QCriteriaOperator.EQUALS, warehouseId))
            .withCriteria(new QFilterCriteria("zoneTypeId", QCriteriaOperator.EQUALS, ZoneType.BULK.getPossibleValueId()))
            .withCriteria(new QFilterCriteria("isActive", QCriteriaOperator.EQUALS, true))));

      for(QRecord zone : zoneQuery.getRecords())
      {
         Integer locationId = findAvailableLocationInZone(zone.getValueInteger("id"), warehouseId);
         if(locationId != null)
         {
            return (locationId);
         }
      }

      LOG.warn("No available putaway location found in any BULK zone",
         logPair("warehouseId", warehouseId));

      return (null);
   }
}
