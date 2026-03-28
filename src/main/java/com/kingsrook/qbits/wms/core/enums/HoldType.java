/*******************************************************************************
 ** Enum of inventory hold types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum HoldType implements PossibleValueEnum<Integer>
{
   QC(1, "QC"),
   DAMAGE(2, "Damage"),
   RECALL(3, "Recall"),
   REGULATORY(4, "Regulatory"),
   CUSTOMER_DISPUTE(5, "Customer Dispute"),
   EXPIRATION(6, "Expiration"),
   CUSTOM(7, "Custom");

   private final Integer id;
   private final String  label;

   public static final String NAME = "HoldType";



   /*******************************************************************************
    **
    *******************************************************************************/
   HoldType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static HoldType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(HoldType value : HoldType.values())
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
