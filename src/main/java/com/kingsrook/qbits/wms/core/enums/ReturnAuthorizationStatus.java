/*******************************************************************************
 ** Enum of return authorization statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ReturnAuthorizationStatus implements PossibleValueEnum<Integer>
{
   AUTHORIZED(1, "Authorized"),
   AWAITING_RECEIPT(2, "Awaiting Receipt"),
   RECEIVED(3, "Received"),
   INSPECTING(4, "Inspecting"),
   DISPOSITIONED(5, "Dispositioned"),
   CLOSED(6, "Closed"),
   CANCELLED(7, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ReturnAuthorizationStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   ReturnAuthorizationStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ReturnAuthorizationStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ReturnAuthorizationStatus value : ReturnAuthorizationStatus.values())
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
