/*******************************************************************************
 ** Enum of storage requirement types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum StorageRequirements implements PossibleValueEnum<Integer>
{
   AMBIENT(1, "Ambient"),
   COLD(2, "Cold"),
   FROZEN(3, "Frozen"),
   HAZMAT(4, "Hazmat"),
   FRAGILE(5, "Fragile");

   private final Integer id;
   private final String  label;

   public static final String NAME = "StorageRequirements";



   /*******************************************************************************
    **
    *******************************************************************************/
   StorageRequirements(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static StorageRequirements getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(StorageRequirements value : StorageRequirements.values())
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
