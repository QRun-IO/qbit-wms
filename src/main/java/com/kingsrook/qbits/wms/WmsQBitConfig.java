/*******************************************************************************
 ** Configuration class for the WMS QBit.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.math.BigDecimal;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


public class WmsQBitConfig implements QBitConfig
{
   private String     backendName;
   private String     tableNamePrefix;
   private String     defaultPickStrategy          = "BATCH";
   private String     defaultPutawayStrategy       = "DIRECTED";
   private String     defaultAllocationStrategy    = "FIFO";
   private Integer    adjustmentApprovalThreshold  = 100;
   private BigDecimal cycleCountVarianceThreshold  = new BigDecimal("5.0");
   private Boolean    blindCountDefault            = true;
   private String     opensearchHost;
   private Integer    opensearchPort;
   private String     opensearchIndexName;



   /*******************************************************************************
    ** Validate the configuration before the QBit is produced.
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      if(!StringUtils.hasContent(backendName))
      {
         errors.add("backendName is required for WmsQBit");
      }
   }



   /*******************************************************************************
    ** Apply prefix to a table name if configured.
    *******************************************************************************/
   public String applyPrefix(String tableName)
   {
      if(StringUtils.hasContent(tableNamePrefix))
      {
         return tableNamePrefix + "_" + tableName;
      }
      return tableName;
   }



   //////////////////////////////////////////////////////////////////////////////
   // Getters and fluent setters                                               //
   //////////////////////////////////////////////////////////////////////////////

   /*******************************************************************************
    ** Getter for backendName
    *******************************************************************************/
   public String getBackendName()
   {
      return (this.backendName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getDefaultBackendNameForTables()
   {
      return (this.backendName);
   }



   /*******************************************************************************
    ** Fluent setter for backendName
    *******************************************************************************/
   public WmsQBitConfig withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableNamePrefix
    *******************************************************************************/
   public String getTableNamePrefix()
   {
      return (this.tableNamePrefix);
   }



   /*******************************************************************************
    ** Fluent setter for tableNamePrefix
    *******************************************************************************/
   public WmsQBitConfig withTableNamePrefix(String tableNamePrefix)
   {
      this.tableNamePrefix = tableNamePrefix;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultPickStrategy
    *******************************************************************************/
   public String getDefaultPickStrategy()
   {
      return (this.defaultPickStrategy);
   }



   /*******************************************************************************
    ** Fluent setter for defaultPickStrategy
    *******************************************************************************/
   public WmsQBitConfig withDefaultPickStrategy(String defaultPickStrategy)
   {
      this.defaultPickStrategy = defaultPickStrategy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultPutawayStrategy
    *******************************************************************************/
   public String getDefaultPutawayStrategy()
   {
      return (this.defaultPutawayStrategy);
   }



   /*******************************************************************************
    ** Fluent setter for defaultPutawayStrategy
    *******************************************************************************/
   public WmsQBitConfig withDefaultPutawayStrategy(String defaultPutawayStrategy)
   {
      this.defaultPutawayStrategy = defaultPutawayStrategy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultAllocationStrategy
    *******************************************************************************/
   public String getDefaultAllocationStrategy()
   {
      return (this.defaultAllocationStrategy);
   }



   /*******************************************************************************
    ** Fluent setter for defaultAllocationStrategy
    *******************************************************************************/
   public WmsQBitConfig withDefaultAllocationStrategy(String defaultAllocationStrategy)
   {
      this.defaultAllocationStrategy = defaultAllocationStrategy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for adjustmentApprovalThreshold
    *******************************************************************************/
   public Integer getAdjustmentApprovalThreshold()
   {
      return (this.adjustmentApprovalThreshold);
   }



   /*******************************************************************************
    ** Fluent setter for adjustmentApprovalThreshold
    *******************************************************************************/
   public WmsQBitConfig withAdjustmentApprovalThreshold(Integer adjustmentApprovalThreshold)
   {
      this.adjustmentApprovalThreshold = adjustmentApprovalThreshold;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cycleCountVarianceThreshold
    *******************************************************************************/
   public BigDecimal getCycleCountVarianceThreshold()
   {
      return (this.cycleCountVarianceThreshold);
   }



   /*******************************************************************************
    ** Fluent setter for cycleCountVarianceThreshold
    *******************************************************************************/
   public WmsQBitConfig withCycleCountVarianceThreshold(BigDecimal cycleCountVarianceThreshold)
   {
      this.cycleCountVarianceThreshold = cycleCountVarianceThreshold;
      return (this);
   }



   /*******************************************************************************
    ** Getter for blindCountDefault
    *******************************************************************************/
   public Boolean getBlindCountDefault()
   {
      return (this.blindCountDefault);
   }



   /*******************************************************************************
    ** Fluent setter for blindCountDefault
    *******************************************************************************/
   public WmsQBitConfig withBlindCountDefault(Boolean blindCountDefault)
   {
      this.blindCountDefault = blindCountDefault;
      return (this);
   }



   /*******************************************************************************
    ** Getter for opensearchHost
    *******************************************************************************/
   public String getOpensearchHost()
   {
      return (this.opensearchHost);
   }



   /*******************************************************************************
    ** Fluent setter for opensearchHost
    *******************************************************************************/
   public WmsQBitConfig withOpensearchHost(String opensearchHost)
   {
      this.opensearchHost = opensearchHost;
      return (this);
   }



   /*******************************************************************************
    ** Getter for opensearchPort
    *******************************************************************************/
   public Integer getOpensearchPort()
   {
      return (this.opensearchPort);
   }



   /*******************************************************************************
    ** Fluent setter for opensearchPort
    *******************************************************************************/
   public WmsQBitConfig withOpensearchPort(Integer opensearchPort)
   {
      this.opensearchPort = opensearchPort;
      return (this);
   }



   /*******************************************************************************
    ** Getter for opensearchIndexName
    *******************************************************************************/
   public String getOpensearchIndexName()
   {
      return (this.opensearchIndexName);
   }



   /*******************************************************************************
    ** Fluent setter for opensearchIndexName
    *******************************************************************************/
   public WmsQBitConfig withOpensearchIndexName(String opensearchIndexName)
   {
      this.opensearchIndexName = opensearchIndexName;
      return (this);
   }
}
