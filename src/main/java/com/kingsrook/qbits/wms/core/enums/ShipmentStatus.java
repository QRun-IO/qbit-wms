/*******************************************************************************
 ** Enum of shipment statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ShipmentStatus implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   LABEL_PRINTED(2, "Label Printed"),
   MANIFESTED(3, "Manifested"),
   PICKED_UP(4, "Picked Up"),
   IN_TRANSIT(5, "In Transit"),
   DELIVERED(6, "Delivered"),
   EXCEPTION(7, "Exception");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ShipmentStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   ShipmentStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ShipmentStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ShipmentStatus value : ShipmentStatus.values())
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
