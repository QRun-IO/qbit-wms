/*******************************************************************************
 ** Enum of shipping modes.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ShippingMode implements PossibleValueEnum<Integer>
{
   PARCEL(1, "Parcel"),
   LTL(2, "LTL"),
   FTL(3, "FTL");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ShippingMode";



   /*******************************************************************************
    **
    *******************************************************************************/
   ShippingMode(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ShippingMode getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ShippingMode value : ShippingMode.values())
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
