package dev.alphasucks.aea.port.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_2248.class)
public abstract class BlockDropsEnchantmentScopeMixin {
   @WrapMethod(
      method = "method_9511(Lnet/minecraft/class_2680;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;Lnet/minecraft/class_2586;Lnet/minecraft/class_1297;Lnet/minecraft/class_1799;)V"
   )
   private static void apoli$scopeDropResources(
      class_2680 state,
      class_1937 level,
      class_2338 pos,
      @Nullable class_2586 blockEntity,
      @Nullable class_1297 entity,
      class_1799 tool,
      Operation<Void> original
   ) {
      boolean scoped = ModifyEnchantmentLevelHandler.beginToolScope(entity, tool);

      try {
         original.call(new Object[]{state, level, pos, blockEntity, entity, tool});
      } finally {
         if (scoped) {
            ModifyEnchantmentLevelHandler.endToolScope();
         }
      }
   }

   @WrapMethod(
      method = "method_9609(Lnet/minecraft/class_2680;Lnet/minecraft/class_3218;Lnet/minecraft/class_2338;Lnet/minecraft/class_2586;Lnet/minecraft/class_1297;Lnet/minecraft/class_1799;)Ljava/util/List;"
   )
   private static List<class_1799> apoli$scopeGetDrops(
      class_2680 state,
      class_3218 level,
      class_2338 pos,
      @Nullable class_2586 blockEntity,
      @Nullable class_1297 entity,
      class_1799 tool,
      Operation<List<class_1799>> original
   ) {
      boolean scoped = ModifyEnchantmentLevelHandler.beginToolScope(entity, tool);

      try {
         return (List<class_1799>)original.call(new Object[]{state, level, pos, blockEntity, entity, tool});
      } finally {
         if (scoped) {
            ModifyEnchantmentLevelHandler.endToolScope();
         }
      }
   }
}
