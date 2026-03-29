/*******************************************************************************
 ** Integration tests for shipping processes: generate label, manifest, and
 ** ship confirm.
 *******************************************************************************/
package com.kingsrook.qbits.wms.shipping.processes;


import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qbits.wms.BaseTest;
import com.kingsrook.qbits.wms.core.enums.CartonStatus;
import com.kingsrook.qbits.wms.core.enums.ShipmentStatus;
import com.kingsrook.qbits.wms.fulfillment.model.WmsCarton;
import com.kingsrook.qbits.wms.shipping.model.WmsShipment;
import com.kingsrook.qbits.wms.shipping.model.WmsShipmentOrder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class ShippingIntegrationTest extends BaseTest
{

   /*******************************************************************************
    ** Test the generate-label -> manifest -> ship-confirm pipeline.
    *******************************************************************************/
   @Test
   void testShippingPipeline_labelManifestConfirm_endToEnd() throws Exception
   {
      Integer warehouseId = insertWarehouse();
      Integer itemId = insertItem();
      Integer orderId = insertOrder(warehouseId);
      insertOrderLine(orderId, itemId, 5);

      /////////////////////////////////////////////////////////////////////////
      // Create and pack a carton                                            //
      /////////////////////////////////////////////////////////////////////////
      Integer cartonId = insertCarton(orderId);
      new UpdateAction().execute(new UpdateInput(WmsCarton.TABLE_NAME).withRecord(new QRecord()
         .withValue("id", cartonId)
         .withValue("statusId", CartonStatus.PACKED.getPossibleValueId())));

      /////////////////////////////////////////////////////////////////////////
      // Step 1: Generate shipping label                                     //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput labelInput = new RunBackendStepInput();
      labelInput.addValue("warehouseId", warehouseId);
      labelInput.addValue("cartonId", cartonId);
      labelInput.addValue("carrier", "UPS");
      labelInput.addValue("serviceLevel", "GROUND");
      RunBackendStepOutput labelOutput = new RunBackendStepOutput();

      new GenerateShippingLabelStep().run(labelInput, labelOutput);

      Integer shipmentId = labelOutput.getValueInteger("shipmentId");
      assertThat(shipmentId).isNotNull();
      assertThat(labelOutput.getValueString("trackingNumber")).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify shipment created with LABEL_PRINTED status                   //
      /////////////////////////////////////////////////////////////////////////
      QRecord shipment = new GetAction().execute(new GetInput(WmsShipment.TABLE_NAME).withPrimaryKey(shipmentId)).getRecord();
      assertThat(shipment).isNotNull();
      assertThat(shipment.getValueInteger("statusId")).isEqualTo(ShipmentStatus.LABEL_PRINTED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Verify shipment-order junction created                              //
      /////////////////////////////////////////////////////////////////////////
      QueryOutput soQuery = new QueryAction().execute(new QueryInput(WmsShipmentOrder.TABLE_NAME)
         .withFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("shipmentId", QCriteriaOperator.EQUALS, shipmentId))));
      assertThat(soQuery.getRecords()).isNotEmpty();

      /////////////////////////////////////////////////////////////////////////
      // Verify carton status updated to LABELED                             //
      /////////////////////////////////////////////////////////////////////////
      QRecord carton = new GetAction().execute(new GetInput(WmsCarton.TABLE_NAME).withPrimaryKey(cartonId)).getRecord();
      assertThat(carton.getValueInteger("statusId")).isEqualTo(CartonStatus.LABELED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Step 2: Manifest shipments                                          //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput manifestInput = new RunBackendStepInput();
      manifestInput.addValue("warehouseId", warehouseId);
      manifestInput.addValue("carrier", "UPS");
      RunBackendStepOutput manifestOutput = new RunBackendStepOutput();

      new ManifestShipmentsStep().run(manifestInput, manifestOutput);

      assertThat(manifestOutput.getValueInteger("totalShipments")).isEqualTo(1);

      /////////////////////////////////////////////////////////////////////////
      // Verify shipment is now MANIFESTED                                   //
      /////////////////////////////////////////////////////////////////////////
      QRecord updatedShipment = new GetAction().execute(new GetInput(WmsShipment.TABLE_NAME).withPrimaryKey(shipmentId)).getRecord();
      assertThat(updatedShipment.getValueInteger("statusId")).isEqualTo(ShipmentStatus.MANIFESTED.getPossibleValueId());

      /////////////////////////////////////////////////////////////////////////
      // Step 3: Ship confirm                                                //
      /////////////////////////////////////////////////////////////////////////
      RunBackendStepInput confirmInput = new RunBackendStepInput();
      confirmInput.addValue("shipmentId", shipmentId);
      RunBackendStepOutput confirmOutput = new RunBackendStepOutput();

      new ShipConfirmStep().run(confirmInput, confirmOutput);

      assertThat(confirmOutput.getValueString("resultMessage")).isNotNull();

      /////////////////////////////////////////////////////////////////////////
      // Verify shipment is PICKED_UP                                        //
      /////////////////////////////////////////////////////////////////////////
      QRecord confirmedShipment = new GetAction().execute(new GetInput(WmsShipment.TABLE_NAME).withPrimaryKey(shipmentId)).getRecord();
      assertThat(confirmedShipment.getValueInteger("statusId")).isEqualTo(ShipmentStatus.PICKED_UP.getPossibleValueId());
   }
}
