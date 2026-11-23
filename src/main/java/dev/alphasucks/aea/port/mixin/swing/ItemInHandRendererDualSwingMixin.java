package dev.alphasucks.aea.port.mixin.swing;

import dev.alphasucks.aea.port.access.DualSwingHolder;
import dev.alphasucks.aea.port.client.render.ArmEdits;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_759.class)
@Environment(EnvType.CLIENT)
public abstract class ItemInHandRendererDualSwingMixin {
   @ModifyArg(
      method = "method_22976",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 4
   )
   private float apoli$swingProgress(
      class_742 player,
      float partialTick,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 pose,
      class_4597 buffers,
      int light
   ) {
      if (ArmEdits.held(ArmEdits.of(player, true), ArmEdits.armFor(player, hand))) {
         return 0.0F;
      } else {
         return !(swingProgress > 0.0F) && DualSwingHolder.of(player) ? player.method_6055(partialTick) : swingProgress;
      }
   }

   @ModifyArg(
      method = "method_22976",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_3228(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 6
   )
   private float apoli$equippedProgress(
      class_742 player,
      float partialTick,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 pose,
      class_4597 buffers,
      int light
   ) {
      return ArmEdits.held(ArmEdits.of(player, true), ArmEdits.armFor(player, hand)) ? 0.0F : equippedProgress;
   }
}
