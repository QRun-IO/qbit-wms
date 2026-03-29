/*******************************************************************************
 ** Enum of Advanced Shipping Notice (ASN) statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum AsnStatus implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   ARRIVED(2, "Arrived"),
   RECEIVED(3, "Received"),
   CANCELLED(4, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "AsnStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   AsnStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static AsnStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(AsnStatus value : AsnStatus.values())
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
