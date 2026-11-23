package dev.alphasucks.aea.port.mixin.scale;

import dev.alphasucks.aea.port.client.render.ScaleRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_332;
import net.minecraft.class_490;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_490.class)
@Environment(EnvType.CLIENT)
public abstract class InventoryScreenScaleMixin {
   private static final String RENDER_ENTITY = "renderEntityInInventory(Lnet/minecraft/client/gui/GuiGraphics;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/world/entity/LivingEntity;)V";

   @Inject(method = "method_48472(Lnet/minecraft/class_332;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/class_1309;)V", at = @At("HEAD"))
   private static void apoli$beginDoll(
      class_332 graphics, int x, int y, int scale, Quaternionf rotation, @Nullable Quaternionf cameraRotation, class_1309 entity, CallbackInfo ci
   ) {
      ScaleRender.beginDoll();
   }

   @Inject(method = "method_48472(Lnet/minecraft/class_332;IIILorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/class_1309;)V", at = @At("RETURN"))
   private static void apoli$endDoll(
      class_332 graphics, int x, int y, int scale, Quaternionf rotation, @Nullable Quaternionf cameraRotation, class_1309 entity, CallbackInfo ci
   ) {
      ScaleRender.endDoll();
   }
}
