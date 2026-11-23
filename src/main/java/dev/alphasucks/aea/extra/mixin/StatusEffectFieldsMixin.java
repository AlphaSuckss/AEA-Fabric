package dev.alphasucks.aea.extra.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code apoli:status_effect} takes {@code effect} or {@code effect_type}, exactly one, as in AEA for NeoForge, so a
 * pack that loads here also loads there.
 */
@Mixin(value = ConditionFactory.class, remap = false)
public abstract class StatusEffectFieldsMixin {
    @Unique
    private static final class_2960 AEA$STATUS_EFFECT = new class_2960("apoli", "status_effect");

    @Shadow
    public abstract class_2960 getSerializerId();

    @Inject(method = "read(Lcom/google/gson/JsonObject;)Lio/github/apace100/apoli/power/factory/condition/ConditionFactory$Instance;",
        at = @At("HEAD"))
    private void aea$effectOrEffectType(JsonObject json, CallbackInfoReturnable<?> cir) {
        if (AEA$STATUS_EFFECT.equals(getSerializerId()) && json.has("effect") == json.has("effect_type")) {
            throw new JsonSyntaxException("apoli:status_effect needs either \"effect\" or \"effect_type\", not both");
        }
    }
}
