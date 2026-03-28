/*******************************************************************************
 ** Enum of disposition actions for returned or damaged items.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum Disposition implements PossibleValueEnum<Integer>
{
   RESTOCK(1, "Restock"),
   RESTOCK_SECONDARY(2, "Restock Secondary"),
   REFURBISH(3, "Refurbish"),
   RETURN_TO_VENDOR(4, "Return to Vendor"),
   DONATE(5, "Donate"),
   SCRAP(6, "Scrap");

   private final Integer id;
   private final String  label;

   public static final String NAME = "Disposition";



   /*******************************************************************************
    **
    *******************************************************************************/
   Disposition(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static Disposition getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(Disposition value : Disposition.values())
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
