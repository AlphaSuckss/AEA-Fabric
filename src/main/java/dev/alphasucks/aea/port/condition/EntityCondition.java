package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1297;

/** AEA: wraps an Apoli 2.9.2 entity condition so ported code can nest any condition type. */
public record EntityCondition(ConditionFactory<class_1297>.Instance instance, JsonElement json) {
    public static final Codec<EntityCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.ENTITY_CONDITION, EntityCondition::new, EntityCondition::json);

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

    public boolean test(EntityCtx ctx) {
        if (ctx.entity() == null) {
            return inverted();
        }
        return instance.test(ctx.entity());
    }
}
