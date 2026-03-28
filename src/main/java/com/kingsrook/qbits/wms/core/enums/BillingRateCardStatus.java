/*******************************************************************************
 ** Enum of billing rate card statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum BillingRateCardStatus implements PossibleValueEnum<Integer>
{
   DRAFT(1, "Draft"),
   ACTIVE(2, "Active"),
   EXPIRED(3, "Expired");

   private final Integer id;
   private final String  label;

   public static final String NAME = "BillingRateCardStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   BillingRateCardStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static BillingRateCardStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(BillingRateCardStatus value : BillingRateCardStatus.values())
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
