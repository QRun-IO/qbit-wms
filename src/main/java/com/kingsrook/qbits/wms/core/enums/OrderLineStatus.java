/*******************************************************************************
 ** Enum of order line statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum OrderLineStatus implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   ALLOCATED(2, "Allocated"),
   PICKING(3, "Picking"),
   PICKED(4, "Picked"),
   PACKED(5, "Packed"),
   SHIPPED(6, "Shipped"),
   BACKORDERED(7, "Backordered"),
   CANCELLED(8, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "OrderLineStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   OrderLineStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static OrderLineStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(OrderLineStatus value : OrderLineStatus.values())
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
