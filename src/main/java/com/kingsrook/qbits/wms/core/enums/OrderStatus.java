/*******************************************************************************
 ** Enum of order statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum OrderStatus implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   ALLOCATED(2, "Allocated"),
   PICK_RELEASED(3, "Pick Released"),
   PICKING(4, "Picking"),
   PICKED(5, "Picked"),
   PACKING(6, "Packing"),
   PACKED(7, "Packed"),
   SHIPPED(8, "Shipped"),
   CANCELLED(9, "Cancelled"),
   ON_HOLD(10, "On Hold"),
   BACKORDERED(11, "Backordered");

   private final Integer id;
   private final String  label;

   public static final String NAME = "OrderStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   OrderStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static OrderStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(OrderStatus value : OrderStatus.values())
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
