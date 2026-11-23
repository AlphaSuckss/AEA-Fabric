package dev.alphasucks.aea.port.mixin.block;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PreventBeeAngerPower.Config;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_4466;
import net.minecraft.class_4481;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(class_4481.class)
public class BeehiveBlockMixin {
   // AEA: @WrapOperation instead of @Redirect so other mods touching the same call still apply
   @WrapOperation(method = "method_23893", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4466;method_5980(Lnet/minecraft/class_1309;)V"))
   public void apoli$angerNearbyBees(class_4466 instance, class_1309 livingEntity, Operation<Void> original, @Local(argsOnly = true) class_1937 level, @Local(argsOnly = true) class_2338 blockPos) {
      List<Boolean> anger = new ArrayList();
      PowerLookup.forEach(
         livingEntity,
         Apoli.id("prevent_bee_anger"),
         Config.class,
         config -> {
            BiEntityCtx bi = BiEntityCtx.of(livingEntity, instance, level);
            BlockCtx block = new BlockCtx(blockPos, level.method_8320(blockPos), level);
            if ((config.biEntityCondition().isEmpty() || ((BiEntityCondition)config.biEntityCondition().get()).test(bi))
               && (config.blockCondition().isEmpty() || ((BlockCondition)config.blockCondition().get()).test(block))) {
               anger.add(false);
            }
         }
      );
      if (anger.isEmpty()) {
         original.call(instance, livingEntity);
      }
   }
}
