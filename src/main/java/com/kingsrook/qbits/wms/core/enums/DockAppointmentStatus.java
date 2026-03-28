/*******************************************************************************
 ** Enum of dock appointment statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum DockAppointmentStatus implements PossibleValueEnum<Integer>
{
   SCHEDULED(1, "Scheduled"),
   CHECKED_IN(2, "Checked In"),
   IN_PROGRESS(3, "In Progress"),
   COMPLETED(4, "Completed"),
   NO_SHOW(5, "No Show"),
   CANCELLED(6, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "DockAppointmentStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   DockAppointmentStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static DockAppointmentStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(DockAppointmentStatus value : DockAppointmentStatus.values())
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
