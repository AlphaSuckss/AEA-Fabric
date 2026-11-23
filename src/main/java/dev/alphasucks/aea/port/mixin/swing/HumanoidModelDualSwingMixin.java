package dev.alphasucks.aea.port.mixin.swing;

import dev.alphasucks.aea.port.access.DualSwingHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_572.class)
@Environment(EnvType.CLIENT)
public abstract class HumanoidModelDualSwingMixin<T extends class_1309> {
   @Shadow
   public class_630 field_3398;
   @Shadow
   public class_630 field_3391;
   @Shadow
   public class_630 field_3401;
   @Shadow
   public class_630 field_27433;

   @Inject(method = "method_29353", at = @At("TAIL"))
   private void apoli$swingOtherArm(T entity, float ageInTicks, CallbackInfo ci) {
      float attackTime = ((class_583)(Object)this).field_3447;
      if (!(attackTime <= 0.0F) && DualSwingHolder.of(entity)) {
         class_1306 attacking = entity.field_6266 == class_1268.field_5808 ? entity.method_6068() : entity.method_6068().method_5928();
         class_630 other = attacking == class_1306.field_6183 ? this.field_27433 : this.field_3401;
         float eased = 1.0F - attackTime;
         eased *= eased;
         eased *= eased;
         eased = 1.0F - eased;
         float lift = class_3532.method_15374(eased * (float) Math.PI);
         float pitch = class_3532.method_15374(attackTime * (float) Math.PI) * -(this.field_3398.field_3654 - 0.7F) * 0.75F;
         other.field_3654 -= lift * 1.2F + pitch;
         other.field_3675 = other.field_3675 + this.field_3391.field_3675 * 2.0F;
         other.field_3674 = other.field_3674 + class_3532.method_15374(attackTime * (float) Math.PI) * -0.4F;
      }
   }
}
