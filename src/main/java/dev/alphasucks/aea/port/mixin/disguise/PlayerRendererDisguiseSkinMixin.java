package dev.alphasucks.aea.port.mixin.disguise;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_630;
import net.minecraft.class_640;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererDisguiseSkinMixin {
   @ModifyReturnValue(method = "method_4216(Lnet/minecraft/class_742;)Lnet/minecraft/class_2960;", at = @At("RETURN"))
   private class_2960 apoli$disguiseSkin(class_2960 original, class_742 player) {
      class_2960 skin = this.apoli$disguiseSkinOf(player);
      return skin != null && CustomModelRenderPower.firstReplace(player) == null ? skin : original;
   }

   @ModifyExpressionValue(method = "method_23205", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_3117()Lnet/minecraft/class_2960;"))
   private class_2960 apoli$disguiseHandSkin(
      class_2960 original, class_4587 pose, class_4597 buffers, int light, class_742 player, class_630 arm, class_630 sleeve
   ) {
      class_2960 skin = this.apoli$disguiseSkinOf(player);
      return skin != null ? skin : original;
   }

   @Unique
   private class_2960 apoli$disguiseSkinOf(class_742 player) {
      class_640 info = ClientDisguiseManager.playerInfo(player.method_5628());
      return info == null ? null : info.method_2968();
   }
}
