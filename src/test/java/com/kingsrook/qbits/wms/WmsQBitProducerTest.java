/*******************************************************************************
 ** Unit tests for WmsQBitProducer.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsQBitProducerTest extends BaseTest
{

   /*******************************************************************************
    ** Test that getQBitMetaData returns correct identity.
    *******************************************************************************/
   @Test
   void testGetQBitMetaData_identity_hasCorrectGroupArtifactVersion()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName(BACKEND_NAME);
      WmsQBitProducer producer = new WmsQBitProducer();
      producer.setQBitConfig(config);

      QBitMetaData metaData = producer.getQBitMetaData();
      assertThat(metaData).isNotNull();
      assertThat(metaData.getGroupId()).isEqualTo("com.kingsrook.qbits");
      assertThat(metaData.getArtifactId()).isEqualTo("qbit-wms");
      assertThat(metaData.getVersion()).isEqualTo("0.1.0");
   }



   /*******************************************************************************
    ** Test that the config can be set and retrieved.
    *******************************************************************************/
   @Test
   void testSetQBitConfig_setAndGet_returnsConfig()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName("myBackend");
      WmsQBitProducer producer = new WmsQBitProducer();
      producer.setQBitConfig(config);
      assertThat(producer.getQBitConfig()).isSameAs(config);
   }



   /*******************************************************************************
    ** Test fluent setter for config.
    *******************************************************************************/
   @Test
   void testWithQBitConfig_fluent_returnsSameProducer()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName("myBackend");
      WmsQBitProducer producer = new WmsQBitProducer();
      WmsQBitProducer result = producer.withQBitConfig(config);
      assertThat(result).isSameAs(producer);
      assertThat(producer.getQBitConfig()).isSameAs(config);
   }



   /*******************************************************************************
    ** Test that produce registers all expected tables.
    *******************************************************************************/
   @Test
   void testProduce_registersAllTables_correctTableCount()
   {
      QInstance qInstance = getQInstance();

      //////////////////////////////////////////
      // Verify all 16 core model tables exist //
      //////////////////////////////////////////
      assertThat(qInstance.getTable("wmsWarehouse")).isNotNull();
      assertThat(qInstance.getTable("wmsZone")).isNotNull();
      assertThat(qInstance.getTable("wmsLocation")).isNotNull();
      assertThat(qInstance.getTable("wmsClient")).isNotNull();
      assertThat(qInstance.getTable("wmsVendor")).isNotNull();
      assertThat(qInstance.getTable("wmsItemCategory")).isNotNull();
      assertThat(qInstance.getTable("wmsItem")).isNotNull();
      assertThat(qInstance.getTable("wmsUnitOfMeasure")).isNotNull();
      assertThat(qInstance.getTable("wmsLicensePlate")).isNotNull();
      assertThat(qInstance.getTable("wmsInventory")).isNotNull();
      assertThat(qInstance.getTable("wmsInventoryTransaction")).isNotNull();
      assertThat(qInstance.getTable("wmsInventoryHold")).isNotNull();
      assertThat(qInstance.getTable("wmsTask")).isNotNull();
      assertThat(qInstance.getTable("wmsTaskTypeConfig")).isNotNull();
      assertThat(qInstance.getTable("wmsCycleCount")).isNotNull();
      assertThat(qInstance.getTable("wmsCycleCountLine")).isNotNull();
   }



   /*******************************************************************************
    ** Test that produce registers all expected possible value sources (enums).
    *******************************************************************************/
   @Test
   void testProduce_registersAllPossibleValueSources_enumsPresent()
   {
      QInstance qInstance = getQInstance();

      //////////////////////////////////
      // Verify all 38 enums are PVS  //
      //////////////////////////////////
      assertThat(qInstance.getPossibleValueSource("TaskType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("TaskStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("EquipmentType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("TransactionType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("InventoryStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ZoneType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("LocationType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("HoldType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("StorageRequirements")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("VelocityClass")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("PurchaseOrderStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ReceiptType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ReceiptStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("OrderStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("WaveStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("WaveType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("PickMethod")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ShipmentStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ShippingMode")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("CartonStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ManifestStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ReturnReasonCode")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ReturnAuthorizationStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("InspectionGrade")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("Disposition")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("InvoiceStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("BillingActivityType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("BillingRateCardStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("AdjustmentReasonCode")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("CycleCountType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("CycleCountStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("LpnStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("DockAppointmentType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("DockAppointmentStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("KitWorkOrderType")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("KitWorkOrderStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("QcStatus")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("ConditionCode")).isNotNull();
   }



   /*******************************************************************************
    ** Test that produce also registers entity-level PVSes for tables.
    *******************************************************************************/
   @Test
   void testProduce_registersTablePossibleValueSources_tablesAsPvs()
   {
      QInstance qInstance = getQInstance();

      assertThat(qInstance.getPossibleValueSource("wmsWarehouse")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("wmsZone")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("wmsLocation")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("wmsClient")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("wmsItem")).isNotNull();
      assertThat(qInstance.getPossibleValueSource("wmsLicensePlate")).isNotNull();
   }



   /*******************************************************************************
    ** Test that postProduceActions does not throw (placeholder method).
    *******************************************************************************/
   @Test
   void testPostProduceActions_placeholder_doesNotThrow()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName(BACKEND_NAME);
      WmsQBitProducer producer = new WmsQBitProducer().withQBitConfig(config);
      producer.postProduceActions(null, getQInstance());
   }



   /*******************************************************************************
    ** Helper to get the QInstance from context.
    *******************************************************************************/
   private QInstance getQInstance()
   {
      return com.kingsrook.qqq.backend.core.context.QContext.getQInstance();
   }
}
