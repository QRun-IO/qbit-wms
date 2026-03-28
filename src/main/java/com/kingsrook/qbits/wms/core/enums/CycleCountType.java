/*******************************************************************************
 ** Enum of cycle count types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum CycleCountType implements PossibleValueEnum<Integer>
{
   FULL(1, "Full"),
   ABC_BASED(2, "ABC Based"),
   LOCATION_BASED(3, "Location Based"),
   SKU_BASED(4, "SKU Based"),
   RANDOM(5, "Random");

   private final Integer id;
   private final String  label;

   public static final String NAME = "CycleCountType";



   /*******************************************************************************
    **
    *******************************************************************************/
   CycleCountType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static CycleCountType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(CycleCountType value : CycleCountType.values())
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
