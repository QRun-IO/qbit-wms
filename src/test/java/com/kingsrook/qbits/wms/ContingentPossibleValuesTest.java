/*******************************************************************************
 ** Metadata and memory-backend regression tests for contingent possible values.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qbits.wms.inventory.processes.CycleCountProcessMetaDataProducer;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueSearchFilterUseCase;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Verify declared dependencies and their behavior through real possible-value searches.
 *******************************************************************************/
public class ContingentPossibleValuesTest extends BaseTest
{
   /*******************************************************************************
    ** Verify every dependent field and its referenced table, then search two scopes.
    *******************************************************************************/
   @ParameterizedTest
   @CsvSource({
         "wmsLocation,zoneId,warehouseId,warehouseId,,",
         "wmsOrderLine,uomId,itemId,itemId,,",
         "wmsAsnLine,uomId,itemId,itemId,,",
         "wmsPurchaseOrderLine,uomId,itemId,itemId,,",
         "wmsReceiptLine,uomId,itemId,itemId,,",
         "wmsKitBom,componentUomId,itemId,componentItemId,,",
         "wmsKitBom,kitItemId,clientId,clientId,,",
         "wmsKitBom,componentItemId,clientId,clientId,,",
         "wmsReceipt,purchaseOrderId,warehouseId,warehouseId,clientId,clientId",
         "wmsOrder,waveId,warehouseId,warehouseId,,",
         "wmsPurchaseOrder,dockDoorId,warehouseId,warehouseId,,",
         "wmsDockAppointment,dockDoorId,warehouseId,warehouseId,,",
         "wmsShipment,dockDoorId,warehouseId,warehouseId,,",
         "wmsShipment,manifestId,warehouseId,warehouseId,,",
         "wmsInventory,locationId,warehouseId,warehouseId,,",
         "wmsInventory,lpnId,warehouseId,warehouseId,,",
         "wmsInventory,itemId,clientId,clientId,,",
         "wmsInventoryHold,locationId,warehouseId,warehouseId,,",
         "wmsInventoryHold,itemId,clientId,clientId,,",
         "wmsLicensePlate,locationId,warehouseId,warehouseId,,",
         "wmsReplenishmentRule,pickLocationId,warehouseId,warehouseId,,",
         "wmsReplenishmentRule,sourceZoneId,warehouseId,warehouseId,,",
         "wmsPutawayRule,targetZoneId,warehouseId,warehouseId,,",
         "wmsTask,lpnId,warehouseId,warehouseId,,",
         "wmsTask,sourceLocationId,warehouseId,warehouseId,,",
         "wmsTask,destinationLocationId,warehouseId,warehouseId,,",
         "wmsTask,zoneId,warehouseId,warehouseId,,",
         "wmsKitWorkOrder,kitItemId,clientId,clientId,,",
         "wmsReturnAuthorization,originalOrderId,warehouseId,warehouseId,clientId,clientId",
         "wmsBillingActivity,rateId,activityTypeId,activityTypeId,,"
   })
   void tableFilters(String tableName, String fieldName, String column, String input, String secondColumn, String secondInput) throws Exception
   {
      QTableMetaData table = QContext.getQInstance().getTable(tableName);
      QFieldMetaData field = table.getField(fieldName);
      assertTrue(field.getIsEditable());
      assertTrue(table.getField(input).getIsEditable());
      assertCriterion(field, 0, column, input);
      assertEquals(secondColumn == null ? 1 : 2, field.getPossibleValueSourceFilter().getCriteria().size());
      if(secondColumn != null)
      {
         assertTrue(table.getField(secondInput).getIsEditable());
         assertCriterion(field, 1, secondColumn, secondInput);
      }
      assertSearchScopes(field, column, input, secondColumn, secondInput);
   }



   /*******************************************************************************
    ** The process field must carry its own filter, independent of table fields.
    *******************************************************************************/
   @Test
   void cycleCountForm() throws Exception
   {
      QFrontendStepMetaData step = (QFrontendStepMetaData) QContext.getQInstance()
         .getProcess(CycleCountProcessMetaDataProducer.NAME).getStep("selectCountType");
      QFieldMetaData field = step.getFormField("zoneId");
      assertTrue(step.getFormField("warehouseId").getIsEditable());
      assertTrue(field.getIsEditable());
      assertCriterion(field, 0, "warehouseId", "warehouseId");
      assertEquals(1, field.getPossibleValueSourceFilter().getCriteria().size());
      assertSearchScopes(field, "warehouseId", "warehouseId", null, null);
   }



   /*******************************************************************************
    ** Validate the expression and the actual column on the possible-value table.
    *******************************************************************************/
   private void assertCriterion(QFieldMetaData field, int index, String column, String input)
   {
      assertNotNull(field.getPossibleValueSourceFilter());
      QFilterCriteria criterion = field.getPossibleValueSourceFilter().getCriteria().get(index);
      assertEquals(column, criterion.getFieldName());
      assertEquals(QCriteriaOperator.EQUALS, criterion.getOperator());
      assertEquals(List.of("${input." + input + "}"), criterion.getValues());
      String target = QContext.getQInstance().getPossibleValueSource(field.getPossibleValueSourceName()).getTableName();
      assertNotNull(QContext.getQInstance().getTable(target).getField(column));
   }



   /*******************************************************************************
    ** Seed both scopes; paired filters also get rows matching only one criterion.
    *******************************************************************************/
   private void assertSearchScopes(QFieldMetaData field, String column, String input, String secondColumn, String secondInput) throws Exception
   {
      String target = QContext.getQInstance().getPossibleValueSource(field.getPossibleValueSourceName()).getTableName();
      // Isolate contingent filtering from record-security authorization in this test.
      QContext.getQInstance().getTable(target).setRecordSecurityLocks(List.of());
      seed(target, 101, column, 1, secondColumn, 1);
      seed(target, 102, column, 2, secondColumn, 2);
      Map<String, Serializable> values = Map.of(input, 1);
      Set<Serializable> all = Set.of(101, 102);
      if(secondColumn != null)
      {
         seed(target, 103, column, 1, secondColumn, 2);
         seed(target, 104, column, 2, secondColumn, 1);
         values = Map.of(input, 1, secondInput, 1);
         all = Set.of(101, 102, 103, 104);
         assertEquals(Set.of(), search(field, Map.of(input, 1), PossibleValueSearchFilterUseCase.FORM));
         assertEquals(Set.of(), search(field, Map.of(secondInput, 1), PossibleValueSearchFilterUseCase.FORM));
      }
      assertEquals(Set.of(101), search(field, values, PossibleValueSearchFilterUseCase.FORM));
      assertEquals(Set.of(), search(field, Map.of(), PossibleValueSearchFilterUseCase.FORM));
      assertEquals(all, search(field, Map.of(), PossibleValueSearchFilterUseCase.FILTER));
      assertCriterion(field, 0, column, input);
   }



   /*******************************************************************************
    ** Seed the memory backend directly so unrelated required fields do not interfere.
    *******************************************************************************/
   private void seed(String table, int id, String column, int scope, String secondColumn, int secondScope) throws Exception
   {
      QRecord record = new QRecord().withTableName(table).withValue("id", id).withValue(column, scope);
      if(secondColumn != null)
      {
         record.setValue(secondColumn, secondScope);
      }
      MemoryRecordStore.getInstance().insert(new InsertInput(table).withRecord(record), true);
   }



   /*******************************************************************************
    ** Interpret a clone exactly as the UI does before invoking the real PVS action.
    *******************************************************************************/
   private Set<Serializable> search(QFieldMetaData field, Map<String, Serializable> values, PossibleValueSearchFilterUseCase useCase) throws Exception
   {
      QQueryFilter filter = field.getPossibleValueSourceFilter().clone();
      filter.interpretValues(MapBuilder.of("input", values), useCase);
      return new SearchPossibleValueSourceAction().execute(new SearchPossibleValueSourceInput()
            .withPossibleValueSourceName(field.getPossibleValueSourceName())
            .withDefaultQueryFilter(filter))
         .getResults().stream().map(value -> (Serializable) value.getId()).collect(Collectors.toSet());
   }
}
