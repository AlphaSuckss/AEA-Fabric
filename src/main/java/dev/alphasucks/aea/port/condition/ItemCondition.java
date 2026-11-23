package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1799;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 item condition so ported code can nest any condition type. */
public record ItemCondition(ConditionFactory<class_1799>.Instance instance, JsonElement json) {
    public static final Codec<ItemCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.ITEM_CONDITION, ItemCondition::new, ItemCondition::json);

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

    public boolean test(ItemCtx ctx) {
        if (ctx.stack() == null) {
            return inverted();
        }
        AeaContext.pushActor(ctx.holder());
        try {
            return instance.test(ctx.stack());
        } finally {
            AeaContext.popActor();
        }
    }
}
