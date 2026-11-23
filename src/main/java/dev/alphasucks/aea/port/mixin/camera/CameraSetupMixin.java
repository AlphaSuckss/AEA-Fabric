package dev.alphasucks.aea.port.mixin.camera;

import dev.alphasucks.aea.port.client.camera.CameraController;
import dev.alphasucks.aea.port.client.camera.CameraController.Result;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_4184;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4184.class)
@Environment(EnvType.CLIENT)
public abstract class CameraSetupMixin {
   @Shadow
   private float field_18717;
   @Shadow
   private float field_18718;
   @Shadow
   private boolean field_18719;
   @Shadow
   @Final
   private Quaternionf field_21518;
   @Shadow
   @Final
   private Vector3f field_18714;
   @Shadow
   @Final
   private Vector3f field_18715;
   @Shadow
   @Final
   private Vector3f field_18716;

   @Shadow
   protected abstract void method_19327(double var1, double var3, double var5);

   @Inject(method = "method_19321", at = @At("TAIL"))
   private void apoli$modifyCamera(class_1922 level, class_1297 entity, boolean thirdPerson, boolean mirrored, float partialTick, CallbackInfo ci) {
      Result result = CameraController.compute((class_4184)(Object)this, entity, partialTick);
      if (result != null) {
         this.field_18717 = result.pitch;
         this.field_18718 = result.yaw;
         this.field_21518
            .rotationYXZ(-result.yaw * (float) (Math.PI / 180.0), result.pitch * (float) (Math.PI / 180.0), result.roll * (float) (Math.PI / 180.0));
         this.field_18714.set(0.0F, 0.0F, 1.0F).rotate(this.field_21518);
         this.field_18715.set(0.0F, 1.0F, 0.0F).rotate(this.field_21518);
         this.field_18716.set(1.0F, 0.0F, 0.0F).rotate(this.field_21518);
         this.method_19327(result.x, result.y, result.z);
         if (result.detached) {
            this.field_18719 = true;
         }
      }
   }
}
