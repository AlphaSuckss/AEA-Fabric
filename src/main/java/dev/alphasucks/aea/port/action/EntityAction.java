package dev.alphasucks.aea.port.action;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1297;
import dev.alphasucks.aea.bridge.AeaContext;

/** AEA: wraps an Apoli 2.9.2 entity action so ported code can nest any action type (Apoli's, Sync's or ported). */
public record EntityAction(ActionFactory<class_1297>.Instance instance, JsonElement json) {
    public static final Codec<EntityAction> CODEC = AeaCodecs.calio(ApoliDataTypes.ENTITY_ACTION, EntityAction::new, EntityAction::json);

    public class_2960 typeId() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("type")
            ? new class_2960(json.getAsJsonObject().get("type").getAsString()) : new class_2960("apoli", "nothing");
    }

    public Object config() {
        return instance;
    }

    public void run(EntityCtx ctx) {
        if (ctx.entity() != null) {
            instance.accept(ctx.entity());
        }
    }
}
