package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_2694;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 block condition so ported code can nest any condition type. */
public record BlockCondition(ConditionFactory<class_2694>.Instance instance, JsonElement json) {
    public static final Codec<BlockCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.BLOCK_CONDITION, BlockCondition::new, BlockCondition::json);

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

    public boolean test(BlockCtx ctx) {
        if (ctx.level() == null || ctx.pos() == null) {
            return inverted();
        }
        AeaContext.pushActor(ctx.actor());
        try {
            return instance.test(new class_2694(ctx.level(), ctx.pos(), true));
        } finally {
            AeaContext.popActor();
        }
    }
}
