/*******************************************************************************
 ** Enum of pick methods.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum PickMethod implements PossibleValueEnum<Integer>
{
   DISCRETE(1, "Discrete"),
   BATCH(2, "Batch"),
   ZONE(3, "Zone"),
   CLUSTER(4, "Cluster"),
   WAVE(5, "Wave");

   private final Integer id;
   private final String  label;

   public static final String NAME = "PickMethod";



   /*******************************************************************************
    **
    *******************************************************************************/
   PickMethod(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static PickMethod getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(PickMethod value : PickMethod.values())
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
