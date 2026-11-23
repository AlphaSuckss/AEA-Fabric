package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_3610;

/** AEA: wraps an Apoli 2.9.2 fluid condition so ported code can nest any condition type. */
public record FluidCondition(ConditionFactory<class_3610>.Instance instance, JsonElement json) {
    public static final Codec<FluidCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.FLUID_CONDITION, FluidCondition::new, FluidCondition::json);

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

    public boolean test(FluidCtx ctx) {
        if (ctx.state() == null) {
            return inverted();
        }
        return instance.test(ctx.state());
    }
}
