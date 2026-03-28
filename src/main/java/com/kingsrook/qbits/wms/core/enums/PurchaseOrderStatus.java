/*******************************************************************************
 ** Enum of purchase order statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum PurchaseOrderStatus implements PossibleValueEnum<Integer>
{
   DRAFT(1, "Draft"),
   OPEN(2, "Open"),
   PARTIALLY_RECEIVED(3, "Partially Received"),
   RECEIVED(4, "Received"),
   CLOSED(5, "Closed"),
   CANCELLED(6, "Cancelled");

   private final Integer id;
   private final String  label;

   public static final String NAME = "PurchaseOrderStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   PurchaseOrderStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static PurchaseOrderStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(PurchaseOrderStatus value : PurchaseOrderStatus.values())
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
