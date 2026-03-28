/*******************************************************************************
 ** Enum of inventory transaction types.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;


@QMetaDataProducingPossibleValueEnum()
public enum TransactionType implements PossibleValueEnum<Integer>
{
   RECEIVE(1, "Receive"),
   PUTAWAY(2, "Putaway"),
   PICK(3, "Pick"),
   PACK(4, "Pack"),
   SHIP(5, "Ship"),
   ADJUST(6, "Adjust"),
   MOVE(7, "Move"),
   COUNT(8, "Count"),
   HOLD(9, "Hold"),
   RELEASE(10, "Release"),
   RETURN(11, "Return"),
   SCRAP(12, "Scrap"),
   REPLENISH(13, "Replenish"),
   KIT_BUILD(14, "Kit Build"),
   KIT_DISASSEMBLE(15, "Kit Disassemble");

   private final Integer id;
   private final String  label;

   public static final String NAME = "TransactionType";



   /*******************************************************************************
    **
    *******************************************************************************/
   TransactionType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /*******************************************************************************
    ** Get instance by id
    *******************************************************************************/
   public static TransactionType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(TransactionType value : TransactionType.values())
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
