package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.HeldItemEdits;
import dev.alphasucks.aea.port.client.render.ScaleRender;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_811;
import net.minecraft.class_918;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_918.class)
@Environment(EnvType.CLIENT)
public abstract class ItemRendererScaleMixin {
   @Unique
   private static boolean apoli$scalable(class_1309 entity, class_1799 stack, class_811 context) {
      if (ScaleRender.inDoll()) {
         return false;
      } else {
         return context == class_811.field_4316 && entity.method_6047() != stack && entity.method_6079() != stack ? false : !Scales.untouched(entity);
      }
   }

   @Unique
   private static ScaleType apoli$handScale(class_1309 entity, class_1799 stack, boolean leftHand) {
      if (stack == entity.method_6079()) {
         return ScaleTypes.HELD_ITEM_OFFHAND;
      } else if (stack == entity.method_6047()) {
         return ScaleTypes.HELD_ITEM_MAINHAND;
      } else {
         return leftHand == (entity.method_6068() == class_1306.field_6182) ? ScaleTypes.HELD_ITEM_MAINHAND : ScaleTypes.HELD_ITEM_OFFHAND;
      }
   }

   @Inject(
      method = "method_23177(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1937;III)V",
      at = @At("HEAD")
   )
   private void apoli$heldItemPush(
      @Nullable class_1309 entity,
      class_1799 stack,
      class_811 context,
      boolean leftHand,
      class_4587 poseStack,
      class_4597 buffer,
      @Nullable class_1937 level,
      int light,
      int overlay,
      int seed,
      CallbackInfo ci
   ) {
      if (entity != null && !stack.method_7960()) {
         int edits = HeldItemEdits.resolve(entity, context, leftHand);
         if (edits != 2) {
            boolean scaled = apoli$scalable(entity, stack, context);
            if (scaled || edits != 0) {
               poseStack.method_22903();
               if (edits == 1) {
                  HeldItemEdits.apply(poseStack);
               }

               if (scaled) {
                  float scale = Scales.applied(entity, apoli$handScale(entity, stack, leftHand), class_310.method_1551().method_1488());
                  if (scale != 1.0F) {
                     poseStack.method_22905(scale, scale, scale);
                  }
               }
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = "method_23177(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1937;III)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7960()Z")
   )
   private boolean apoli$hideHeldItem(
      boolean empty, @Local(argsOnly = true) @Nullable class_1309 entity, @Local(argsOnly = true) class_811 context, @Local(argsOnly = true) boolean leftHand
   ) {
      return empty || entity != null && HeldItemEdits.resolve(entity, context, leftHand) == 2;
   }

   @Inject(
      method = "method_23177(Lnet/minecraft/class_1309;Lnet/minecraft/class_1799;Lnet/minecraft/class_811;ZLnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1937;III)V",
      at = @At("RETURN")
   )
   private void apoli$heldItemPop(
      @Nullable class_1309 entity,
      class_1799 stack,
      class_811 context,
      boolean leftHand,
      class_4587 poseStack,
      class_4597 buffer,
      @Nullable class_1937 level,
      int light,
      int overlay,
      int seed,
      CallbackInfo ci
   ) {
      if (entity != null && !stack.method_7960()) {
         int edits = HeldItemEdits.resolve(entity, context, leftHand);
         if (edits != 2) {
            if (apoli$scalable(entity, stack, context) || edits != 0) {
               poseStack.method_22909();
            }
         }
      }
   }
}
