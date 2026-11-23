package dev.overgrown.sync.mixin.body_part_damage_modifier;

import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.BodyPartDamageModifierPower;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_1309.class)
public abstract class ModifyDamageByBodyPartMixin {
   @ModifyVariable(method = "method_5643", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private float sync$applyBodyPartModifiers(float amount, class_1282 source) {
      class_1309 self = (class_1309)(Object)this;
      if (self.method_37908().field_9236) {
         return amount;
      }

      // read and clear once per damage event (also for entities without the power, so entries don't linger),
      // and hand the same hit to every power
      class_243 trackedHit = HitLocationTracker.getAndClear(self);

      for (BodyPartDamageModifierPower power : PowerHolderComponent.getPowers(self, BodyPartDamageModifierPower.class)) {
         amount = power.apply(amount, source, trackedHit);
      }

      return amount;
   }
}
