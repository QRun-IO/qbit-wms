/*******************************************************************************
 ** MetaData producer for the WMS QBit.
 *******************************************************************************/
package com.kingsrook.qbits.wms;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


public class WmsQBitProducer implements QBitMetaDataProducer<WmsQBitConfig>
{
   private WmsQBitConfig qBitConfig;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      return new QBitMetaData()
         .withGroupId("com.kingsrook.qbits")
         .withArtifactId("qbit-wms")
         .withVersion("0.1.0")
         .withNamespace(getNamespace())
         .withConfig(getQBitConfig());
   }



   /*******************************************************************************
    ** Post-produce actions: apply security locks and permission rules to all WMS
    ** tables and processes produced by this QBit.
    *******************************************************************************/
   @Override
   public void postProduceActions(MetaDataProducerMultiOutput metaDataProducerMultiOutput, QInstance qInstance)
   {
      /////////////////////////////////////////////////////////////////////////
      // Determine security locks to apply                                   //
      /////////////////////////////////////////////////////////////////////////
      List<RecordSecurityLock> securityLocks = qBitConfig.getRecordSecurityLocks();
      if(CollectionUtils.nullSafeIsEmpty(securityLocks))
      {
         securityLocks = buildDefaultSecurityLocks();
      }

      /////////////////////////////////////////////////////////////////////////
      // Apply security locks and permissions to all WMS tables              //
      /////////////////////////////////////////////////////////////////////////
      for(QTableMetaData table : qInstance.getTables().values())
      {
         if(table.getName().startsWith("wms"))
         {
            //////////////////////////////////////////////////////////////////
            // Apply security locks                                         //
            //////////////////////////////////////////////////////////////////
            for(RecordSecurityLock lock : securityLocks)
            {
               if(table.getFields().containsKey(lock.getFieldName()))
               {
                  table.withRecordSecurityLock(lock);
               }
            }

            //////////////////////////////////////////////////////////////////
            // Apply permission rules                                       //
            //////////////////////////////////////////////////////////////////
            table.withPermissionRules(new QPermissionRules()
               .withLevel(PermissionLevel.READ_INSERT_EDIT_DELETE_PERMISSIONS));
         }
      }

      /////////////////////////////////////////////////////////////////////////
      // Apply permission rules to all WMS processes                         //
      /////////////////////////////////////////////////////////////////////////
      for(QProcessMetaData process : qInstance.getProcesses().values())
      {
         if(process.getName().startsWith("wms"))
         {
            process.withPermissionRules(new QPermissionRules()
               .withLevel(PermissionLevel.HAS_ACCESS_PERMISSION));
         }
      }
   }



   /*******************************************************************************
    ** Build default security locks when none are configured: warehouseId (DENY on
    ** null) and clientId (ALLOW on null, for brand-direct mode where no client).
    *******************************************************************************/
   private List<RecordSecurityLock> buildDefaultSecurityLocks()
   {
      List<RecordSecurityLock> locks = new ArrayList<>();

      locks.add(new RecordSecurityLock()
         .withFieldName("warehouseId")
         .withSecurityKeyType("warehouseAccess")
         .withNullValueBehavior(RecordSecurityLock.NullValueBehavior.DENY));

      locks.add(new RecordSecurityLock()
         .withFieldName("clientId")
         .withSecurityKeyType("clientAccess")
         .withNullValueBehavior(RecordSecurityLock.NullValueBehavior.ALLOW));

      return (locks);
   }



   /*******************************************************************************
    ** Getter for qBitConfig
    *******************************************************************************/
   @Override
   public WmsQBitConfig getQBitConfig()
   {
      return (this.qBitConfig);
   }



   /*******************************************************************************
    ** Setter for qBitConfig
    *******************************************************************************/
   public void setQBitConfig(WmsQBitConfig qBitConfig)
   {
      this.qBitConfig = qBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for qBitConfig
    *******************************************************************************/
   public WmsQBitProducer withQBitConfig(WmsQBitConfig qBitConfig)
   {
      this.qBitConfig = qBitConfig;
      return (this);
   }
}
