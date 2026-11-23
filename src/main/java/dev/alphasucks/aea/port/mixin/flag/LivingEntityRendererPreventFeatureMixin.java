package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.client.render.FeatureRenderers;
import dev.alphasucks.aea.port.power.builtin.InvisibilityPower;
import dev.alphasucks.aea.port.power.builtin.PreventFeatureRenderPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_922;
import net.minecraft.class_970;
import net.minecraft.class_989;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererPreventFeatureMixin {
   @WrapWithCondition(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_3887;method_4199(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1297;FFFFFF)V"
      )
   )
   private boolean apoli$preventFeatureRender(
      class_3887<?, ?> layer,
      class_4587 pose,
      class_4597 buffer,
      int packedLight,
      class_1297 rendered,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      class_1297 source = rendered instanceof class_1309 living ? ClientDisguiseManager.powerSource(living) : rendered;
      class_1657 viewer = class_310.method_1551().field_1724;
      if (layer instanceof class_970 && InvisibilityPower.hidesArmor(source) && rendered.method_5756(viewer)) {
         return false;
      } else {
         return layer instanceof class_989 && InvisibilityPower.hidesHeldItems(source) && rendered.method_5756(viewer)
            ? false
            : !PreventFeatureRenderPower.prevents(source, FeatureRenderers.keysFor(layer.getClass()), viewer);
      }
   }
}
