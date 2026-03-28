/*******************************************************************************
 ** Enum of location types within a warehouse.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum LocationType implements PossibleValueEnum<Integer>
{
   BIN(1, "Bin"),
   SHELF(2, "Shelf"),
   FLOOR(3, "Floor"),
   PALLET_POSITION(4, "Pallet Position"),
   STAGING(5, "Staging"),
   DOCK_DOOR(6, "Dock Door");

   private final Integer id;
   private final String  label;

   public static final String NAME = "LocationType";



   /*******************************************************************************
    **
    *******************************************************************************/
   LocationType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static LocationType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(LocationType value : LocationType.values())
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
