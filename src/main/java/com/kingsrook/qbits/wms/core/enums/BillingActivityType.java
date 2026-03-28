/*******************************************************************************
 ** Enum of billing activity types for 3PL billing.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum BillingActivityType implements PossibleValueEnum<Integer>
{
   RECEIVING_PER_UNIT(1, "Receiving Per Unit"),
   RECEIVING_PER_PALLET(2, "Receiving Per Pallet"),
   STORAGE_PER_PALLET_DAY(3, "Storage Per Pallet Day"),
   STORAGE_PER_BIN_DAY(4, "Storage Per Bin Day"),
   PICK_PER_ORDER(5, "Pick Per Order"),
   PICK_PER_LINE(6, "Pick Per Line"),
   PICK_PER_UNIT(7, "Pick Per Unit"),
   PACK_PER_ORDER(8, "Pack Per Order"),
   SHIP_PER_ORDER(9, "Ship Per Order"),
   KITTING_PER_UNIT(10, "Kitting Per Unit"),
   RETURN_PER_UNIT(11, "Return Per Unit"),
   SPECIAL_HANDLING(12, "Special Handling"),
   MINIMUM_MONTHLY(13, "Minimum Monthly");

   private final Integer id;
   private final String  label;

   public static final String NAME = "BillingActivityType";



   /*******************************************************************************
    **
    *******************************************************************************/
   BillingActivityType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static BillingActivityType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(BillingActivityType value : BillingActivityType.values())
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
