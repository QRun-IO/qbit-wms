/*******************************************************************************
 ** Enum of task statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum TaskStatus implements PossibleValueEnum<Integer>
{
   PENDING(1, "Pending"),
   ASSIGNED(2, "Assigned"),
   IN_PROGRESS(3, "In Progress"),
   PAUSED(4, "Paused"),
   COMPLETED(5, "Completed"),
   SHORT(6, "Short"),
   CANCELLED(7, "Cancelled"),
   ON_HOLD(8, "On Hold");

   private final Integer id;
   private final String  label;

   public static final String NAME = "TaskStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   TaskStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static TaskStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(TaskStatus value : TaskStatus.values())
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
