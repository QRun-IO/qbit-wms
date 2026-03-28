/*******************************************************************************
 ** Enum of inventory adjustment reason codes.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum AdjustmentReasonCode implements PossibleValueEnum<Integer>
{
   DAMAGE(1, "Damage"),
   SHRINKAGE(2, "Shrinkage"),
   FOUND_STOCK(3, "Found Stock"),
   QC_REJECTION(4, "QC Rejection"),
   RECLASSIFICATION(5, "Reclassification"),
   CORRECTION(6, "Correction"),
   EXPIRATION(7, "Expiration"),
   OTHER(8, "Other");

   private final Integer id;
   private final String  label;

   public static final String NAME = "AdjustmentReasonCode";



   /*******************************************************************************
    **
    *******************************************************************************/
   AdjustmentReasonCode(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static AdjustmentReasonCode getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(AdjustmentReasonCode value : AdjustmentReasonCode.values())
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
