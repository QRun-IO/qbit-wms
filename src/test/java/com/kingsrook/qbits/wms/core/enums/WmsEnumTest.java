/*******************************************************************************
 ** Comprehensive test covering all 38 WMS enums.
 ** Exercises getPossibleValueId, getPossibleValueLabel, values count,
 ** getById found, and getById not-found for every enum type.
 *******************************************************************************/
package com.kingsrook.qbits.wms.core.enums;


import java.lang.reflect.Method;
import java.util.stream.Stream;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;


class WmsEnumTest
{

   /*******************************************************************************
    ** Provide all enum classes with their expected value counts for parameterized tests.
    *******************************************************************************/
   static Stream<Arguments> enumProvider()
   {
      return Stream.of(
         Arguments.of(TaskType.class, 10),
         Arguments.of(TaskStatus.class, 8),
         Arguments.of(EquipmentType.class, 5),
         Arguments.of(TransactionType.class, 15),
         Arguments.of(InventoryStatus.class, 7),
         Arguments.of(ZoneType.class, 10),
         Arguments.of(LocationType.class, 6),
         Arguments.of(HoldType.class, 7),
         Arguments.of(StorageRequirements.class, 5),
         Arguments.of(VelocityClass.class, 3),
         Arguments.of(PurchaseOrderStatus.class, 6),
         Arguments.of(ReceiptType.class, 5),
         Arguments.of(ReceiptStatus.class, 4),
         Arguments.of(OrderStatus.class, 11),
         Arguments.of(WaveStatus.class, 5),
         Arguments.of(WaveType.class, 4),
         Arguments.of(PickMethod.class, 5),
         Arguments.of(ShipmentStatus.class, 7),
         Arguments.of(ShippingMode.class, 3),
         Arguments.of(CartonStatus.class, 5),
         Arguments.of(ManifestStatus.class, 3),
         Arguments.of(ReturnReasonCode.class, 6),
         Arguments.of(ReturnAuthorizationStatus.class, 7),
         Arguments.of(InspectionGrade.class, 5),
         Arguments.of(Disposition.class, 6),
         Arguments.of(InvoiceStatus.class, 6),
         Arguments.of(BillingActivityType.class, 13),
         Arguments.of(BillingRateCardStatus.class, 3),
         Arguments.of(AdjustmentReasonCode.class, 8),
         Arguments.of(CycleCountType.class, 5),
         Arguments.of(CycleCountStatus.class, 5),
         Arguments.of(LpnStatus.class, 4),
         Arguments.of(DockAppointmentType.class, 2),
         Arguments.of(DockAppointmentStatus.class, 6),
         Arguments.of(KitWorkOrderType.class, 2),
         Arguments.of(KitWorkOrderStatus.class, 5),
         Arguments.of(QcStatus.class, 4),
         Arguments.of(ConditionCode.class, 3)
      );
   }



   /*******************************************************************************
    ** Test that every enum has the expected number of values.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   void testValuesCount_allEnums_matchExpected(Class<? extends Enum<?>> enumClass, int expectedCount)
   {
      Enum<?>[] constants = enumClass.getEnumConstants();
      assertThat(constants).hasSize(expectedCount);
   }



   /*******************************************************************************
    ** Test that getPossibleValueId returns a non-null Integer for every constant.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   @SuppressWarnings("unchecked")
   void testGetPossibleValueId_allConstants_nonNull(Class<? extends Enum<?>> enumClass, int expectedCount)
   {
      for(Enum<?> constant : enumClass.getEnumConstants())
      {
         PossibleValueEnum<Integer> pve = (PossibleValueEnum<Integer>) constant;
         assertThat(pve.getPossibleValueId())
            .as("getPossibleValueId() for %s.%s", enumClass.getSimpleName(), constant.name())
            .isNotNull();
      }
   }



   /*******************************************************************************
    ** Test that getPossibleValueLabel returns a non-blank String for every constant.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   @SuppressWarnings("unchecked")
   void testGetPossibleValueLabel_allConstants_nonBlank(Class<? extends Enum<?>> enumClass, int expectedCount)
   {
      for(Enum<?> constant : enumClass.getEnumConstants())
      {
         PossibleValueEnum<Integer> pve = (PossibleValueEnum<Integer>) constant;
         assertThat(pve.getPossibleValueLabel())
            .as("getPossibleValueLabel() for %s.%s", enumClass.getSimpleName(), constant.name())
            .isNotBlank();
      }
   }



   /*******************************************************************************
    ** Test that getById returns the correct constant for each id.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   @SuppressWarnings("unchecked")
   void testGetById_validId_returnsCorrectConstant(Class<? extends Enum<?>> enumClass, int expectedCount) throws Exception
   {
      Method getByIdMethod = enumClass.getMethod("getById", Integer.class);

      for(Enum<?> constant : enumClass.getEnumConstants())
      {
         PossibleValueEnum<Integer> pve = (PossibleValueEnum<Integer>) constant;
         Integer id = pve.getPossibleValueId();
         Object result = getByIdMethod.invoke(null, id);
         assertThat(result)
            .as("getById(%d) for %s", id, enumClass.getSimpleName())
            .isEqualTo(constant);
      }
   }



   /*******************************************************************************
    ** Test that getById returns null for an id that does not exist.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   void testGetById_invalidId_returnsNull(Class<? extends Enum<?>> enumClass, int expectedCount) throws Exception
   {
      Method getByIdMethod = enumClass.getMethod("getById", Integer.class);
      Object result = getByIdMethod.invoke(null, Integer.valueOf(-999));
      assertThat(result)
         .as("getById(-999) for %s", enumClass.getSimpleName())
         .isNull();
   }



   /*******************************************************************************
    ** Test that getById returns null when given null.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   void testGetById_nullId_returnsNull(Class<? extends Enum<?>> enumClass, int expectedCount) throws Exception
   {
      Method getByIdMethod = enumClass.getMethod("getById", Integer.class);
      Object result = getByIdMethod.invoke(null, (Integer) null);
      assertThat(result)
         .as("getById(null) for %s", enumClass.getSimpleName())
         .isNull();
   }



   /*******************************************************************************
    ** Test that all enum ids are unique within each enum.
    *******************************************************************************/
   @ParameterizedTest
   @MethodSource("enumProvider")
   @SuppressWarnings("unchecked")
   void testUniqueIds_allEnums_noDuplicates(Class<? extends Enum<?>> enumClass, int expectedCount)
   {
      Enum<?>[] constants = enumClass.getEnumConstants();
      long uniqueIdCount = java.util.Arrays.stream(constants)
         .map(c -> ((PossibleValueEnum<Integer>) c).getPossibleValueId())
         .distinct()
         .count();
      assertThat(uniqueIdCount)
         .as("unique IDs for %s", enumClass.getSimpleName())
         .isEqualTo(constants.length);
   }



   /*******************************************************************************
    ** Spot-check specific enum values for correctness.
    *******************************************************************************/
   @Test
   void testSpecificValues_spotCheck_correctIdAndLabel()
   {
      assertThat(TaskType.PUTAWAY.getPossibleValueId()).isEqualTo(1);
      assertThat(TaskType.PUTAWAY.getPossibleValueLabel()).isEqualTo("Putaway");
      assertThat(TaskType.KIT_ASSEMBLE.getPossibleValueId()).isEqualTo(10);

      assertThat(TaskStatus.PENDING.getId()).isEqualTo(1);
      assertThat(TaskStatus.COMPLETED.getLabel()).isEqualTo("Completed");

      assertThat(InventoryStatus.AVAILABLE.getId()).isEqualTo(1);
      assertThat(InventoryStatus.B_STOCK.getLabel()).isEqualTo("B-Stock");

      assertThat(TransactionType.RECEIVE.getId()).isEqualTo(1);
      assertThat(TransactionType.KIT_DISASSEMBLE.getId()).isEqualTo(15);

      assertThat(ZoneType.BULK.getId()).isEqualTo(1);
      assertThat(ZoneType.RETURNS.getId()).isEqualTo(10);

      assertThat(LocationType.BIN.getId()).isEqualTo(1);
      assertThat(LocationType.DOCK_DOOR.getId()).isEqualTo(6);

      assertThat(EquipmentType.FORKLIFT.getId()).isEqualTo(1);
      assertThat(EquipmentType.NONE.getId()).isEqualTo(5);

      assertThat(LpnStatus.ACTIVE.getId()).isEqualTo(1);
      assertThat(CycleCountType.FULL.getId()).isEqualTo(1);
      assertThat(CycleCountStatus.PLANNED.getId()).isEqualTo(1);

      assertThat(ConditionCode.NEW.getId()).isEqualTo(1);
      assertThat(ConditionCode.DEFECTIVE.getId()).isEqualTo(3);
   }



   /*******************************************************************************
    ** Test that the NAME constant is set correctly for a sampling of enums.
    *******************************************************************************/
   @Test
   void testNameConstant_spotCheck_matchesClassName()
   {
      assertThat(TaskType.NAME).isEqualTo("TaskType");
      assertThat(TaskStatus.NAME).isEqualTo("TaskStatus");
      assertThat(EquipmentType.NAME).isEqualTo("EquipmentType");
      assertThat(TransactionType.NAME).isEqualTo("TransactionType");
      assertThat(InventoryStatus.NAME).isEqualTo("InventoryStatus");
      assertThat(ZoneType.NAME).isEqualTo("ZoneType");
      assertThat(LocationType.NAME).isEqualTo("LocationType");
      assertThat(HoldType.NAME).isEqualTo("HoldType");
      assertThat(StorageRequirements.NAME).isEqualTo("StorageRequirements");
      assertThat(VelocityClass.NAME).isEqualTo("VelocityClass");
      assertThat(LpnStatus.NAME).isEqualTo("LpnStatus");
      assertThat(CycleCountType.NAME).isEqualTo("CycleCountType");
      assertThat(CycleCountStatus.NAME).isEqualTo("CycleCountStatus");
      assertThat(ConditionCode.NAME).isEqualTo("ConditionCode");
      assertThat(QcStatus.NAME).isEqualTo("QcStatus");
      assertThat(PickMethod.NAME).isEqualTo("PickMethod");
   }
}
