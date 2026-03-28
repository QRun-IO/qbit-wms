/*******************************************************************************
 ** Enum of wave types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum WaveType implements PossibleValueEnum<Integer>
{
   CARRIER_CUTOFF(1, "Carrier Cutoff"),
   PRIORITY(2, "Priority"),
   ZONE(3, "Zone"),
   MANUAL(4, "Manual");

   private final Integer id;
   private final String  label;

   public static final String NAME = "WaveType";



   /*******************************************************************************
    **
    *******************************************************************************/
   WaveType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static WaveType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(WaveType value : WaveType.values())
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
