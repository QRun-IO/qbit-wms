/*******************************************************************************
 ** Enum of carton statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum CartonStatus implements PossibleValueEnum<Integer>
{
   OPEN(1, "Open"),
   PACKED(2, "Packed"),
   LABELED(3, "Labeled"),
   SHIPPED(4, "Shipped"),
   VOID(5, "Void");

   private final Integer id;
   private final String  label;

   public static final String NAME = "CartonStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   CartonStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static CartonStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(CartonStatus value : CartonStatus.values())
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
