/*******************************************************************************
 ** Enum of inventory statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum InventoryStatus implements PossibleValueEnum<Integer>
{
   AVAILABLE(1, "Available"),
   ALLOCATED(2, "Allocated"),
   ON_HOLD(3, "On Hold"),
   DAMAGED(4, "Damaged"),
   QUARANTINE(5, "Quarantine"),
   EXPIRED(6, "Expired"),
   B_STOCK(7, "B-Stock");

   private final Integer id;
   private final String  label;

   public static final String NAME = "InventoryStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   InventoryStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static InventoryStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(InventoryStatus value : InventoryStatus.values())
      {
         if(Objects.equals(value.id, id))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getPossibleValueId()
   {
      return (getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (getLabel());
   }
}
