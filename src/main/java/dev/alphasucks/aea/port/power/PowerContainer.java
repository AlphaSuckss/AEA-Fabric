package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.PowerContainerAttachment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public interface PowerContainer {
   boolean addPower(class_2960 var1, class_2960 var2);

   boolean removePower(class_2960 var1, class_2960 var2);

   boolean removeAllFromSource(class_2960 var1);

   boolean removePowerCompletely(class_2960 var1);

   void clear();

   boolean hasPower(class_2960 var1);

   default boolean isEmpty() {
      return this.allPowers().isEmpty();
   }

   default List<class_2960> powersOfType(class_2960 canonicalTypeId) {
      List<class_2960> out = null;

      for (class_2960 powerId : this.allPowers()) {
         Power power = ApoliPowers.get(powerId);
         if (power != null && canonicalTypeId.equals(PowerTypeRegistry.resolveId(power.typeId()))) {
            if (out == null) {
               out = new ArrayList(2);
            }

            out.add(powerId);
         }
      }

      return out == null ? List.of() : out;
   }

   Set<class_2960> sourcesOf(class_2960 var1);

   Set<class_2960> allPowers();

   Set<class_2960> allSources();

   boolean suppressPower(class_2960 var1, class_2960 var2);

   boolean unsuppressPower(class_2960 var1, class_2960 var2);

   default boolean suppressAll(Collection<class_2960> powers, class_2960 source) {
      boolean any = false;

      for (class_2960 power : powers) {
         if (this.suppressPower(power, source)) {
            any = true;
         }
      }

      return any;
   }

   default boolean unsuppressAll(Collection<class_2960> powers, class_2960 source) {
      boolean any = false;

      for (class_2960 power : powers) {
         if (this.unsuppressPower(power, source)) {
            any = true;
         }
      }

      return any;
   }

   default boolean unsuppressAllFromSource(class_2960 source) {
      return this.unsuppressAll(List.copyOf(this.directlySuppressedPowers()), source);
   }

   boolean isSuppressed(class_2960 var1);

   Set<class_2960> suppressedPowers();

   default Set<class_2960> directlySuppressedPowers() {
      return this.suppressedPowers();
   }

   Set<class_2960> suppressionSourcesOf(class_2960 var1);

   class_1309 owner();

   class_1297 rawOwner();

   void markDirty();

   OptionalInt getAuxInt(class_2960 var1);

   default int getAuxIntOr(class_2960 powerId, int fallback) {
      OptionalInt v = this.getAuxInt(powerId);
      return v.isPresent() ? v.getAsInt() : fallback;
   }

   default int @Nullable [] getAuxInts(class_2960 powerId) {
      return null;
   }

   @Nullable
   static PowerContainer of(class_1297 entity) {
      return PowerContainerAttachment.get(entity);
   }
}
