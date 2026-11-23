package dev.alphasucks.aea.port.mixin.block;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.ModifyBouncinessHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyBouncinessPower.Config;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1922;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2248.class)
public class BlockBouncinessMixin {
   @Inject(
      method = "method_9502",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_18799(Lnet/minecraft/class_243;)V"),
      cancellable = true
   )
   public void apoli$updateEntityAfterFallOn(class_1922 blockGetter, class_1297 entity, CallbackInfo ci) {
      if (entity instanceof class_1309 living && ModifyBouncinessHandler.has(living)) {
         class_243 oldDelta = entity.method_18798();
         if (!(oldDelta.field_1351 >= 0.0)) {
            if (!entity.method_37908().method_35237(entity.method_24515(), state -> !state.method_15769())) {
               class_2338 pos = entity.method_23312();
               BlockCtx block = new BlockCtx(pos, blockGetter.method_8320(pos), entity.method_37908());
               if (!entity.method_21750() || !ModifyBouncinessHandler.preventable(living, block)) {
                  double delta = ModifyBouncinessHandler.modify(living, oldDelta.field_1351 * -1.0, block);
                  if (!(delta <= 0.125)) {
                     PowerLookup.forEach(entity, ApoliIds.MODIFY_BOUNCINESS, Config.class, cfg -> {
                        if (cfg.blockCondition().isEmpty() || ((BlockCondition)cfg.blockCondition().get()).test(block)) {
                           cfg.blockAction().ifPresent(action -> action.run(block));
                           cfg.entityAction().ifPresent(action -> action.run(EntityCtx.of(entity, entity.method_37908())));
                        }
                     });
                     entity.method_18800(oldDelta.field_1352, delta, oldDelta.field_1350);
                     ci.cancel();
                  }
               }
            }
         }
      }
   }

   @Inject(
      method = "method_9554",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5747(FFLnet/minecraft/class_1282;)Z"),
      cancellable = true
   )
   public void apoli$fallOn(class_1937 level, class_2680 blockState, class_2338 blockPos, class_1297 entity, float f, CallbackInfo ci) {
      if (entity instanceof class_1309 living && ModifyBouncinessHandler.has(living)) {
         if (ModifyBouncinessHandler.preventsFallDamage(living, new BlockCtx(blockPos, blockState, level))) {
            ci.cancel();
         }
      }
   }
}
