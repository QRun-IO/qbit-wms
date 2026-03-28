/*******************************************************************************
 ** Enum of task types in the warehouse.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum TaskType implements PossibleValueEnum<Integer>
{
   PUTAWAY(1, "Putaway"),
   PICK(2, "Pick"),
   PACK(3, "Pack"),
   REPLENISH(4, "Replenish"),
   COUNT(5, "Count"),
   MOVE(6, "Move"),
   RETURN_PUTAWAY(7, "Return Putaway"),
   LOAD(8, "Load"),
   QC_INSPECT(9, "QC Inspect"),
   KIT_ASSEMBLE(10, "Kit Assemble");

   private final Integer id;
   private final String  label;

   public static final String NAME = "TaskType";



   /*******************************************************************************
    **
    *******************************************************************************/
   TaskType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static TaskType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(TaskType value : TaskType.values())
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
