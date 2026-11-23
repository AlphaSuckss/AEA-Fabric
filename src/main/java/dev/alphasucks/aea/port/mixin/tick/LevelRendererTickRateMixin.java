package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = class_761.class, priority = 1100)
@Environment(EnvType.CLIENT)
public abstract class LevelRendererTickRateMixin {
   private static final String RENDER_ENTITY = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String RENDER = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

   @ModifyArg(
      method = "method_22977(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 1
   )
   private double apoli$slowedX(
      class_1297 entity, double x, double y, double z, float yRot, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      float shift = apoli$slotShift(entity, partialTick);
      return shift == 0.0F ? x : x + shift * (entity.method_23317() - entity.field_6038);
   }

   @ModifyArg(
      method = "method_22977(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 2
   )
   private double apoli$slowedY(
      class_1297 entity, double x, double y, double z, float yRot, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      float shift = apoli$slotShift(entity, partialTick);
      return shift == 0.0F ? y : y + shift * (entity.method_23318() - entity.field_5971);
   }

   @ModifyArg(
      method = "method_22977(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 3
   )
   private double apoli$slowedZ(
      class_1297 entity, double x, double y, double z, float yRot, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      float shift = apoli$slotShift(entity, partialTick);
      return shift == 0.0F ? z : z + shift * (entity.method_23321() - entity.field_5989);
   }

   @ModifyArg(
      method = "method_22977(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 4
   )
   private float apoli$slowedYaw(
      class_1297 entity, double x, double y, double z, float yRot, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      return apoli$slotShift(entity, partialTick) == 0.0F
         ? yRot
         : class_3532.method_16439(ClientTickRates.slotPartial(entity, partialTick), entity.field_5982, entity.method_36454());
   }

   @ModifyArg(
      method = "method_22977(Lnet/minecraft/class_1297;DDDFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      ),
      index = 5
   )
   private float apoli$slowedPartial(
      class_1297 entity, double x, double y, double z, float yRot, float partialTick, class_4587 poseStack, class_4597 buffers, int light
   ) {
      return ClientTickRates.idle() ? partialTick : ClientTickRates.slotPartial(entity, partialTick);
   }

   @Unique
   private static float apoli$slotShift(class_1297 entity, float partialTick) {
      if (ClientTickRates.idle()) {
         return 0.0F;
      }

      float slot = ClientTickRates.slotPartial(entity, partialTick);
      return slot != partialTick && ClientTickRates.slotDriven(entity) ? slot - partialTick : 0.0F;
   }
}
