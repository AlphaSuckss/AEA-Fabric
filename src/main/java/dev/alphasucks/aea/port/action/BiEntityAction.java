package dev.alphasucks.aea.port.action;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import dev.alphasucks.aea.bridge.AeaCodecs;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import net.minecraft.class_2960;
import net.minecraft.class_1297;
import net.minecraft.class_3545;
import dev.alphasucks.aea.port.data.expr.ExprPeer;

/** AEA: wraps an Apoli 2.9.2 bi-entity action so ported code can nest any action type (Apoli's, Sync's or ported). */
public record BiEntityAction(ActionFactory<class_3545<class_1297, class_1297>>.Instance instance, JsonElement json) {
    public static final Codec<BiEntityAction> CODEC = AeaCodecs.calio(ApoliDataTypes.BIENTITY_ACTION, BiEntityAction::new, BiEntityAction::json);

    public class_2960 typeId() {
        return json != null && json.isJsonObject() && json.getAsJsonObject().has("type")
            ? new class_2960(json.getAsJsonObject().get("type").getAsString()) : new class_2960("apoli", "nothing");
    }

    public Object config() {
        return instance;
    }

    public void run(BiEntityCtx ctx) {
        if (ctx.actor() == null && ctx.target() == null) {
            return;
        }
        class_1297[] frame = ExprPeer.frame();
        class_1297 previousActor = frame[0];
        class_1297 previousTarget = frame[1];
        frame[0] = ctx.actor();
        frame[1] = ctx.target();
        try {
            instance.accept(new class_3545<>(ctx.actor(), ctx.target()));
        } finally {
            frame[0] = previousActor;
            frame[1] = previousTarget;
        }
    }
}
