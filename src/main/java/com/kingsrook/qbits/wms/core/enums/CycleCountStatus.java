/*******************************************************************************
 ** Enum of cycle count statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum CycleCountStatus implements PossibleValueEnum<Integer>
{
   PLANNED(1, "Planned"),
   IN_PROGRESS(2, "In Progress"),
   PENDING_REVIEW(3, "Pending Review"),
   COMPLETED(4, "Completed"),
   CANCELLED(5, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "CycleCountStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   CycleCountStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static CycleCountStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(CycleCountStatus value : CycleCountStatus.values())
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
