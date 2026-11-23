package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1282;
import net.minecraft.class_3545;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 damage condition so ported code can nest any condition type. */
public record DamageCondition(ConditionFactory<class_3545<class_1282, Float>>.Instance instance, JsonElement json) {
    public static final Codec<DamageCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.DAMAGE_CONDITION, DamageCondition::new, DamageCondition::json);

    public class_2960 typeId() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("type")
            ? new class_2960(json.getAsJsonObject().get("type").getAsString()) : new class_2960("apoli", "constant");
    }

    public Object config() {
        return instance;
    }

    public boolean inverted() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("inverted") && json.getAsJsonObject().get("inverted").getAsBoolean();
    }

    public boolean test(DamageCtx ctx) {
        if (ctx.source() == null) {
            return inverted();
        }
        if (ctx.target() != null) {
            AeaContext.pushDamageTarget(ctx.target());
        }
        try {
            return instance.test(new class_3545<>(ctx.source(), ctx.amount()));
        } finally {
            if (ctx.target() != null) {
                AeaContext.popDamageTarget();
            }
        }
    }
}
