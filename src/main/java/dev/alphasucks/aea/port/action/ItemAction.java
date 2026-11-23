package dev.alphasucks.aea.port.action;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_3545;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 item action so ported code can nest any action type (Apoli's, Sync's or ported). */
public record ItemAction(ActionFactory<class_3545<class_1937, class_1799>>.Instance instance, JsonElement json) {
    public static final Codec<ItemAction> CODEC = AeaCodecs.calio(ApoliDataTypes.ITEM_ACTION, ItemAction::new, ItemAction::json);

    public class_2960 typeId() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("type")
            ? new class_2960(json.getAsJsonObject().get("type").getAsString()) : new class_2960("apoli", "nothing");
    }

    public Object config() {
        return instance;
    }

    public void run(ItemCtx ctx) {
        if (ctx.stack() == null) {
            return;
        }
        AeaContext.pushActor(ctx.holder());
        try {
            instance.accept(new class_3545<>(ctx.level(), ctx.stack()));
        } finally {
            AeaContext.popActor();
        }
    }
}
