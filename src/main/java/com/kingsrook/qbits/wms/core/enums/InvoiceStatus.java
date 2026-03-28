/*******************************************************************************
 ** Enum of invoice statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum InvoiceStatus implements PossibleValueEnum<Integer>
{
   DRAFT(1, "Draft"),
   SENT(2, "Sent"),
   PAID(3, "Paid"),
   OVERDUE(4, "Overdue"),
   DISPUTED(5, "Disputed"),
   VOID(6, "Void");

   private final Integer id;
   private final String  label;

   public static final String NAME = "InvoiceStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   InvoiceStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static InvoiceStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(InvoiceStatus value : InvoiceStatus.values())
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
