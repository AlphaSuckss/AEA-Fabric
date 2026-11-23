package dev.alphasucks.aea.port.mixin.effects;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.effects.CustomEffectRegistry;
import dev.alphasucks.aea.port.effects.EffectConfig;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.class_3300;
import net.minecraft.class_5350;
import net.minecraft.class_7699;
import net.minecraft.class_2170.class_5364;
import net.minecraft.class_5455.class_6890;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_5350.class)
public class ReloadableServerResourcesCustomEffectMixin {
   @Inject(
      method = "method_29466(Lnet/minecraft/class_3300;Lnet/minecraft/class_5455$class_6890;Lnet/minecraft/class_7699;Lnet/minecraft/class_2170$class_5364;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
      at = @At("HEAD")
   )
   private static void apoli$stageCustomEffects(
      class_3300 resourceManager,
      class_6890 registries,
      class_7699 features,
      class_5364 selection,
      int functionLevel,
      Executor backgroundExecutor,
      Executor gameExecutor,
      CallbackInfoReturnable<CompletableFuture<class_5350>> cir
   ) {
      if (EffectConfig.get().enabled()) {
         // AEA: a broken effect file must not stop the world (or /reload) from loading
         try {
            CustomEffectRegistry.stage(resourceManager);
         } catch (RuntimeException e) {
            Apoli.LOGGER.error("[Apoli] Could not load custom effects", e);
         }
      }
   }
}
