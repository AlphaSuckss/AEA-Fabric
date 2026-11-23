package dev.alphasucks.aea.mixin;

import dev.alphasucks.aea.power.VelocityImmunityPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import java.util.function.BiConsumer;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * aea:velocity_immunity - wraps the effect of the apoli:add_velocity factories (entity and bi-entity; the origins:/sync:
 * ids alias to them) so immune entities are skipped. Every other action factory is left untouched.
 */
@Mixin(value = ActionFactory.class, remap = false)
public abstract class ActionFactoryVelocityImmunityMixin<T> {
    @Shadow
    @Final
    private class_2960 identifier;

    @Shadow
    @Final
    @Mutable
    private BiConsumer<SerializableData.Instance, T> effect;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void aea$wrapAddVelocity(CallbackInfo ci) {
        if (VelocityImmunityPower.isAddVelocity(this.identifier)) {
            BiConsumer<SerializableData.Instance, T> original = this.effect;
            this.effect = (data, argument) -> {
                if (!VelocityImmunityPower.blocks(argument)) {
                    original.accept(data, argument);
                }
            };
        }
    }
}
