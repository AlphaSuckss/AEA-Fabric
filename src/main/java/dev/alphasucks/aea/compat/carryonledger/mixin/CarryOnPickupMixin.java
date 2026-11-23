package dev.alphasucks.aea.compat.carryonledger.mixin;

import dev.alphasucks.aea.compat.carryonledger.CarryOnLedgerLogging;
import java.util.function.BiFunction;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Logs Carry On block pickups to Ledger as block breaks by the player (state and contents captured before removal). */
@Pseudo
@Mixin(targets = "tschipp.carryon.common.carry.PickupHandler", remap = false)
public abstract class CarryOnPickupMixin {
    @Unique
    private static class_2680 aea$capturedState;
    @Unique
    private static class_2586 aea$capturedEntity;

    @Inject(method = "tryPickUpBlock(Lnet/minecraft/class_3222;Lnet/minecraft/class_2338;Lnet/minecraft/class_1937;Ljava/util/function/BiFunction;)Z", at = @At("HEAD"), remap = false)
    private static void aea$capture(class_3222 player, class_2338 pos, class_1937 level, BiFunction<?, ?, ?> callback, CallbackInfoReturnable<Boolean> cir) {
        aea$capturedState = level.method_8320(pos);
        aea$capturedEntity = level.method_8321(pos);
    }

    @Inject(method = "tryPickUpBlock(Lnet/minecraft/class_3222;Lnet/minecraft/class_2338;Lnet/minecraft/class_1937;Ljava/util/function/BiFunction;)Z", at = @At("RETURN"), remap = false)
    private static void aea$log(class_3222 player, class_2338 pos, class_1937 level, BiFunction<?, ?, ?> callback, CallbackInfoReturnable<Boolean> cir) {
        class_2680 state = aea$capturedState;
        class_2586 entity = aea$capturedEntity;
        // AEA: always clear, not only on success (the original kept a stale state/block entity after failed pickups)
        aea$capturedState = null;
        aea$capturedEntity = null;
        if (cir.getReturnValueZ() && state != null) {
            CarryOnLedgerLogging.pickedUp(level, pos, state, player, entity);
        }
    }
}
