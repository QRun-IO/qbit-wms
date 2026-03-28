/*******************************************************************************
 ** Enum of inspection grades for returned or QC-inspected items.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum InspectionGrade implements PossibleValueEnum<Integer>
{
   A_STOCK(1, "A-Stock"),
   B_STOCK(2, "B-Stock"),
   DAMAGED(3, "Damaged"),
   DEFECTIVE(4, "Defective"),
   SCRAP(5, "Scrap");

   private final Integer id;
   private final String  label;

   public static final String NAME = "InspectionGrade";



   /*******************************************************************************
    **
    *******************************************************************************/
   InspectionGrade(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static InspectionGrade getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(InspectionGrade value : InspectionGrade.values())
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
