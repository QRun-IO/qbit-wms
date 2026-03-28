/*******************************************************************************
 ** Enum of receipt types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ReceiptType implements PossibleValueEnum<Integer>
{
   PO_BASED(1, "PO Based"),
   BLIND(2, "Blind"),
   ASN(3, "ASN"),
   RETURN(4, "Return"),
   CROSS_DOCK(5, "Cross Dock");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ReceiptType";



   /*******************************************************************************
    **
    *******************************************************************************/
   ReceiptType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ReceiptType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ReceiptType value : ReceiptType.values())
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
