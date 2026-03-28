/*******************************************************************************
 ** Enum of receipt statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ReceiptStatus implements PossibleValueEnum<Integer>
{
   IN_PROGRESS(1, "In Progress"),
   QC_HOLD(2, "QC Hold"),
   COMPLETED(3, "Completed"),
   CANCELLED(4, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ReceiptStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   ReceiptStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ReceiptStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ReceiptStatus value : ReceiptStatus.values())
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
