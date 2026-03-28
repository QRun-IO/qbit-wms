/*******************************************************************************
 ** Enum of kit work order statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum KitWorkOrderStatus implements PossibleValueEnum<Integer>
{
   DRAFT(1, "Draft"),
   RELEASED(2, "Released"),
   IN_PROGRESS(3, "In Progress"),
   COMPLETED(4, "Completed"),
   CANCELLED(5, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "KitWorkOrderStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   KitWorkOrderStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static KitWorkOrderStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(KitWorkOrderStatus value : KitWorkOrderStatus.values())
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
