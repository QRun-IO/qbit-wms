/*******************************************************************************
 ** Enum of equipment types used for warehouse tasks.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum EquipmentType implements PossibleValueEnum<Integer>
{
   FORKLIFT(1, "Forklift"),
   RF_GUN(2, "RF Gun"),
   PALLET_JACK(3, "Pallet Jack"),
   PACK_STATION(4, "Pack Station"),
   NONE(5, "None");

   private final Integer id;
   private final String  label;

   public static final String NAME = "EquipmentType";



   /*******************************************************************************
    **
    *******************************************************************************/
   EquipmentType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static EquipmentType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(EquipmentType value : EquipmentType.values())
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
