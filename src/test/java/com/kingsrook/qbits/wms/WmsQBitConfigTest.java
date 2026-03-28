/*******************************************************************************
 ** Unit tests for WmsQBitConfig.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


class WmsQBitConfigTest extends BaseTest
{

   /*******************************************************************************
    ** Test validate with a valid config (backendName present).
    *******************************************************************************/
   @Test
   void testValidate_validConfig_noErrors()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName("memory");
      List<String> errors = new ArrayList<>();
      config.validate(new QInstance(), errors);
      assertThat(errors).isEmpty();
   }



   /*******************************************************************************
    ** Test validate with missing backendName.
    *******************************************************************************/
   @Test
   void testValidate_missingBackendName_addsError()
   {
      WmsQBitConfig config = new WmsQBitConfig();
      List<String> errors = new ArrayList<>();
      config.validate(new QInstance(), errors);
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0)).isEqualTo("backendName is required for WmsQBit");
   }



   /*******************************************************************************
    ** Test validate with empty string backendName.
    *******************************************************************************/
   @Test
   void testValidate_emptyBackendName_addsError()
   {
      WmsQBitConfig config = new WmsQBitConfig().withBackendName("");
      List<String> errors = new ArrayList<>();
      config.validate(new QInstance(), errors);
      assertThat(errors).hasSize(1);
      assertThat(errors.get(0)).isEqualTo("backendName is required for WmsQBit");
   }



   /*******************************************************************************
    ** Test applyPrefix when prefix is set.
    *******************************************************************************/
   @Test
   void testApplyPrefix_withPrefix_prependsUnderscore()
   {
      WmsQBitConfig config = new WmsQBitConfig().withTableNamePrefix("acme");
      assertThat(config.applyPrefix("wmsWarehouse")).isEqualTo("acme_wmsWarehouse");
   }



   /*******************************************************************************
    ** Test applyPrefix when prefix is null.
    *******************************************************************************/
   @Test
   void testApplyPrefix_nullPrefix_returnsOriginal()
   {
      WmsQBitConfig config = new WmsQBitConfig();
      assertThat(config.applyPrefix("wmsWarehouse")).isEqualTo("wmsWarehouse");
   }



   /*******************************************************************************
    ** Test applyPrefix when prefix is empty string.
    *******************************************************************************/
   @Test
   void testApplyPrefix_emptyPrefix_returnsOriginal()
   {
      WmsQBitConfig config = new WmsQBitConfig().withTableNamePrefix("");
      assertThat(config.applyPrefix("wmsWarehouse")).isEqualTo("wmsWarehouse");
   }



   /*******************************************************************************
    ** Test all fluent setters return the same instance (chaining).
    *******************************************************************************/
   @Test
   void testFluentSetters_chaining_returnsSameInstance()
   {
      WmsQBitConfig config = new WmsQBitConfig();
      WmsQBitConfig result = config
         .withBackendName("backend")
         .withTableNamePrefix("prefix")
         .withDefaultPickStrategy("WAVE")
         .withDefaultPutawayStrategy("RANDOM")
         .withDefaultAllocationStrategy("LIFO")
         .withAdjustmentApprovalThreshold(200)
         .withCycleCountVarianceThreshold(new BigDecimal("10.0"))
         .withBlindCountDefault(false)
         .withOpensearchHost("localhost")
         .withOpensearchPort(9200)
         .withOpensearchIndexName("wms-index");

      assertThat(result).isSameAs(config);
   }



   /*******************************************************************************
    ** Test all getters return values set by fluent setters.
    *******************************************************************************/
   @Test
   void testGetters_afterFluentSetters_returnCorrectValues()
   {
      WmsQBitConfig config = new WmsQBitConfig()
         .withBackendName("backend")
         .withTableNamePrefix("prefix")
         .withDefaultPickStrategy("WAVE")
         .withDefaultPutawayStrategy("RANDOM")
         .withDefaultAllocationStrategy("LIFO")
         .withAdjustmentApprovalThreshold(200)
         .withCycleCountVarianceThreshold(new BigDecimal("10.0"))
         .withBlindCountDefault(false)
         .withOpensearchHost("localhost")
         .withOpensearchPort(9200)
         .withOpensearchIndexName("wms-index");

      assertThat(config.getBackendName()).isEqualTo("backend");
      assertThat(config.getTableNamePrefix()).isEqualTo("prefix");
      assertThat(config.getDefaultPickStrategy()).isEqualTo("WAVE");
      assertThat(config.getDefaultPutawayStrategy()).isEqualTo("RANDOM");
      assertThat(config.getDefaultAllocationStrategy()).isEqualTo("LIFO");
      assertThat(config.getAdjustmentApprovalThreshold()).isEqualTo(200);
      assertThat(config.getCycleCountVarianceThreshold()).isEqualByComparingTo(new BigDecimal("10.0"));
      assertThat(config.getBlindCountDefault()).isFalse();
      assertThat(config.getOpensearchHost()).isEqualTo("localhost");
      assertThat(config.getOpensearchPort()).isEqualTo(9200);
      assertThat(config.getOpensearchIndexName()).isEqualTo("wms-index");
   }



   /*******************************************************************************
    ** Test default values are correct on a fresh instance.
    *******************************************************************************/
   @Test
   void testDefaults_freshInstance_hasExpectedDefaults()
   {
      WmsQBitConfig config = new WmsQBitConfig();
      assertThat(config.getDefaultPickStrategy()).isEqualTo("BATCH");
      assertThat(config.getDefaultPutawayStrategy()).isEqualTo("DIRECTED");
      assertThat(config.getDefaultAllocationStrategy()).isEqualTo("FIFO");
      assertThat(config.getAdjustmentApprovalThreshold()).isEqualTo(100);
      assertThat(config.getCycleCountVarianceThreshold()).isEqualByComparingTo(new BigDecimal("5.0"));
      assertThat(config.getBlindCountDefault()).isTrue();
      assertThat(config.getBackendName()).isNull();
      assertThat(config.getTableNamePrefix()).isNull();
      assertThat(config.getOpensearchHost()).isNull();
      assertThat(config.getOpensearchPort()).isNull();
      assertThat(config.getOpensearchIndexName()).isNull();
   }
}
