package dev.alphasucks.aea.compat.carryonledger.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.compat.carryonledger.CarryOnLedgerLogging;
import java.util.function.BiFunction;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Logs blocks put down by Carry On to Ledger as block placements by the player. */
@Pseudo
@Mixin(targets = "tschipp.carryon.common.carry.PlacementHandler", remap = false)
public abstract class CarryOnPlacementMixin {
    @Unique
    private static class_2338 aea$deathPlacePos;

    /**
     * Normal placement. tryPlaceBlock reassigns its pos parameter to the block next to the clicked face when the
     * clicked block can't be replaced; at RETURN the argument slot holds that final position.
     */
    @Inject(method = "tryPlaceBlock(Lnet/minecraft/class_3222;Lnet/minecraft/class_2338;Lnet/minecraft/class_2350;Ljava/util/function/BiFunction;)Z", at = @At("RETURN"), remap = false)
    private static void aea$logPlace(class_3222 player, class_2338 pos, class_2350 facing, BiFunction<?, ?, ?> callback, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            CarryOnLedgerLogging.placed(player.method_37908(), pos, player);
        }
    }

    // AEA: also log the block Carry On drops back into the world when a carrying player dies or logs out
    // (the original only logged right-click placement, so those blocks appeared in the world unlogged).
    @WrapOperation(method = "placeCarried(Lnet/minecraft/class_3222;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8652(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;I)Z", remap = false), remap = false)
    private static boolean aea$rememberDeathPlace(class_1937 world, class_2338 pos, class_2680 state, int flags, Operation<Boolean> original) {
        boolean placed = original.call(world, pos, state, flags);
        if (placed) {
            aea$deathPlacePos = pos.method_10062();
        }
        return placed;
    }

    @Inject(method = "placeCarried(Lnet/minecraft/class_3222;)V", at = @At("TAIL"), remap = false)
    private static void aea$logDeathPlace(class_3222 player, CallbackInfo ci) {
        class_2338 pos = aea$deathPlacePos;
        aea$deathPlacePos = null;
        if (pos != null) {
            CarryOnLedgerLogging.placed(player.method_37908(), pos, player);
        }
    }
}
