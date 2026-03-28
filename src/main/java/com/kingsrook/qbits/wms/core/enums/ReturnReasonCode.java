/*******************************************************************************
 ** Enum of return reason codes.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ReturnReasonCode implements PossibleValueEnum<Integer>
{
   DEFECTIVE(1, "Defective"),
   WRONG_ITEM(2, "Wrong Item"),
   DAMAGED_IN_TRANSIT(3, "Damaged In Transit"),
   UNWANTED(4, "Unwanted"),
   WARRANTY(5, "Warranty"),
   OTHER(6, "Other");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ReturnReasonCode";



   /*******************************************************************************
    **
    *******************************************************************************/
   ReturnReasonCode(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ReturnReasonCode getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ReturnReasonCode value : ReturnReasonCode.values())
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
