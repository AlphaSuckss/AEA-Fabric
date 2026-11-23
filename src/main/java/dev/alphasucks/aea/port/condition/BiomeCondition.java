package dev.alphasucks.aea.port.condition;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1959;
import net.minecraft.class_6880;

/** AEA: wraps an Apoli 2.9.2 biome condition so ported code can nest any condition type. */
public record BiomeCondition(ConditionFactory<class_6880<class_1959>>.Instance instance, JsonElement json) {
    public static final Codec<BiomeCondition> CODEC = AeaCodecs.calio(ApoliDataTypes.BIOME_CONDITION, BiomeCondition::new, BiomeCondition::json);

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

    public boolean test(BiomeCtx ctx) {
        if (ctx.biome() == null) {
            return inverted();
        }
        return instance.test(ctx.biome());
    }
}
