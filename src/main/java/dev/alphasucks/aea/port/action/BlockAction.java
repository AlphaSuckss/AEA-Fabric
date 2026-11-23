package dev.alphasucks.aea.port.action;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import org.apache.commons.lang3.tuple.Triple;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 block action so ported code can nest any action type (Apoli's, Sync's or ported). */
public record BlockAction(ActionFactory<Triple<class_1937, class_2338, class_2350>>.Instance instance, JsonElement json) {
    public static final Codec<BlockAction> CODEC = AeaCodecs.calio(ApoliDataTypes.BLOCK_ACTION, BlockAction::new, BlockAction::json);

    public class_2960 typeId() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("type")
            ? new class_2960(json.getAsJsonObject().get("type").getAsString()) : new class_2960("apoli", "nothing");
    }

    public Object config() {
        return instance;
    }

    public void run(BlockCtx ctx) {
        if (ctx.level() == null || ctx.pos() == null) {
            return;
        }
        AeaContext.pushActor(ctx.actor());
        try {
            instance.accept(Triple.of(ctx.level(), ctx.pos(), class_2350.field_11036));
        } finally {
            AeaContext.popActor();
        }
    }
}
