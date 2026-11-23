package dev.alphasucks.aea.port.mixin.effects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.effects.CustomEffectRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.class_2960;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keeps runtime custom effects out of Fabric's registry sync (they are synced by {@code aea:sync_custom_effects}) and out of the saved id map. */
@Mixin(value = RegistrySyncManager.class, remap = false)
public class FabricRegistrySyncCustomEffectMixin {
   @ModifyReturnValue(method = "createAndPopulateRegistryMap", at = @At("RETURN"), remap = false)
   @Nullable
   private static Map<class_2960, Object2IntMap<class_2960>> apoli$filterCustomEffects(@Nullable Map<class_2960, Object2IntMap<class_2960>> original) {
      if (original == null) {
         return null;
      }

      Set<class_2960> custom = CustomEffectRegistry.ids();
      if (custom.isEmpty()) {
         return original;
      }

      Object2IntMap<class_2960> effects = (Object2IntMap<class_2960>)original.get(class_7924.field_41208.method_29177());
      if (effects == null) {
         return original;
      }

      for (class_2960 id : custom) {
         effects.removeInt(id);
      }

      return original;
   }
}
