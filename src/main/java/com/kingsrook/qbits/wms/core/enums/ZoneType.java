/*******************************************************************************
 ** Enum of zone types within a warehouse.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum ZoneType implements PossibleValueEnum<Integer>
{
   BULK(1, "Bulk"),
   PICK_FACE(2, "Pick Face"),
   RESERVE(3, "Reserve"),
   COLD(4, "Cold"),
   HAZMAT(5, "Hazmat"),
   STAGING(6, "Staging"),
   RECEIVING(7, "Receiving"),
   SHIPPING(8, "Shipping"),
   QUARANTINE(9, "Quarantine"),
   RETURNS(10, "Returns");

   private final Integer id;
   private final String  label;

   public static final String NAME = "ZoneType";



   /*******************************************************************************
    **
    *******************************************************************************/
   ZoneType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static ZoneType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(ZoneType value : ZoneType.values())
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
