package dev.alphasucks.aea.port.mixin.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.power.builtin.BlockPlaceHandler;
import dev.alphasucks.aea.port.power.builtin.PreventItemUseHandler;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1657;
import net.minecraft.class_1747;
import net.minecraft.class_1750;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1747.class)
public abstract class BlockItemPlaceMixin {
   private static final String PLACE = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;";
   private static final String USE_ON = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;";
   private static final String ITEM_USE = "Lnet/minecraft/world/item/BlockItem;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;";

   @Inject(method = "method_7712(Lnet/minecraft/class_1750;)Lnet/minecraft/class_1269;", at = @At("HEAD"), cancellable = true)
   private void apoli$preventBlockPlace(class_1750 context, CallbackInfoReturnable<class_1269> cir) {
      class_1657 player = context.method_8036();
      if (player != null) {
         if (BlockPlaceHandler.isPrevented(
            player, context.method_8045(), context.method_20287(), context.method_8041(), context.method_8037(), apoli$onPos(context), context.method_8038()
         )) {
            cir.setReturnValue(class_1269.field_5814);
         }
      }
   }

   @Inject(method = "method_7712(Lnet/minecraft/class_1750;)Lnet/minecraft/class_1269;", at = @At("RETURN"))
   private void apoli$actionOnBlockPlace(class_1750 context, CallbackInfoReturnable<class_1269> cir) {
      if (((class_1269)cir.getReturnValue()).method_23665()) {
         class_1657 player = context.method_8036();
         if (player != null) {
            class_1937 level = context.method_8045();
            class_1268 hand = context.method_20287();
            class_2338 toPos = context.method_8037();
            class_2350 face = context.method_8038();
            class_1799 placed = context.method_8041();
            if (placed.method_7960()) {
               placed = new class_1799((class_1747)(Object)this);
            }

            BlockPlaceHandler.fireAfterPlace(player, level, hand, placed, toPos, apoli$onPos(context), face);
         }
      }
   }

   @WrapOperation(
      method = "method_7884(Lnet/minecraft/class_1838;)Lnet/minecraft/class_1269;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1747;method_7836(Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_1268;)Lnet/minecraft/class_1271;"
      )
   )
   private class_1271<class_1799> apoli$preventEatingBlockItem(
      class_1747 item, class_1937 level, class_1657 player, class_1268 hand, Operation<class_1271<class_1799>> original
   ) {
      if (player != null) {
         class_1799 held = player.method_5998(hand);
         if (PreventItemUseHandler.isBlocked(player, held, level)) {
            return class_1271.method_22431(held);
         }
      }

      return (class_1271<class_1799>)original.call(new Object[]{item, level, player, hand});
   }

   private static class_2338 apoli$onPos(class_1750 context) {
      return ((UseOnContextAccessor)context).apoli$getHitResult().method_17777();
   }
}
