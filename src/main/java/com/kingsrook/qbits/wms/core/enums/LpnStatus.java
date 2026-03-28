/*******************************************************************************
 ** Enum of license plate number (LPN) statuses.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum LpnStatus implements PossibleValueEnum<Integer>
{
   ACTIVE(1, "Active"),
   IN_TRANSIT(2, "In Transit"),
   CONSUMED(3, "Consumed"),
   SCRAPPED(4, "Scrapped");

   private final Integer id;
   private final String  label;

   public static final String NAME = "LpnStatus";



   /*******************************************************************************
    **
    *******************************************************************************/
   LpnStatus(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static LpnStatus getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(LpnStatus value : LpnStatus.values())
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
