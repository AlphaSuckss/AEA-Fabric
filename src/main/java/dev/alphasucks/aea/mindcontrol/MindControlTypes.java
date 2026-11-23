package dev.alphasucks.aea.mindcontrol;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.mindcontrol.client.MindControlClientView;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

/** The data pack side of mind control: one action to start it, one to end it, and conditions to check it. */
public final class MindControlTypes {
    private MindControlTypes() {}

    private static final MapCodec<EmptyCfg> UNIT = MapCodec.unit(EmptyCfg.INSTANCE);

    private static class_2960 id(String path) {
        return new class_2960("aea", path);
    }

    public static void register() {
        ActionTypes.BI_ENTITY.register(id("mind_control"), new ActionType<BiEntityCtx, MindControl.Cfg>() {
            @Override public MapCodec<MindControl.Cfg> codec() { return MindControl.CODEC; }
            @Override public void run(MindControl.Cfg cfg, BiEntityCtx ctx) {
                if (ctx.actor() != null && !ctx.actor().method_37908().field_9236) MindControl.start(cfg, ctx.actor(), ctx.target());
            }
        });
        ActionTypes.ENTITY.register(id("release_mind_control"), new ActionType<EntityCtx, EmptyCfg>() {
            @Override public MapCodec<EmptyCfg> codec() { return UNIT; }
            @Override public void run(EmptyCfg cfg, EntityCtx ctx) {
                class_1297 e = ctx.raw();
                if (e != null && !e.method_37908().field_9236) MindControl.release(e);
            }
        });
        ConditionTypes.ENTITY.register(id("mind_controlled"), new ConditionType<EntityCtx, EmptyCfg>() {
            @Override public MapCodec<EmptyCfg> codec() { return UNIT; }
            @Override public boolean test(EmptyCfg cfg, EntityCtx ctx) {
                class_1297 e = ctx.raw();
                if (e == null) return false;
                return e.method_37908().field_9236 ? MindControlClientView.isControlled(e) : MindControl.isControlled(e);
            }
        });
        ConditionTypes.ENTITY.register(id("mind_controlling"), new ConditionType<EntityCtx, EmptyCfg>() {
            @Override public MapCodec<EmptyCfg> codec() { return UNIT; }
            @Override public boolean test(EmptyCfg cfg, EntityCtx ctx) {
                class_1297 e = ctx.raw();
                if (e == null) return false;
                return e.method_37908().field_9236 ? MindControlClientView.isControlling(e) : MindControl.isControlling(e);
            }
        });
        ConditionTypes.BI_ENTITY.register(id("mind_controls"), new ConditionType<BiEntityCtx, EmptyCfg>() {
            @Override public MapCodec<EmptyCfg> codec() { return UNIT; }
            @Override public boolean test(EmptyCfg cfg, BiEntityCtx ctx) {
                class_1297 a = ctx.actor(), t = ctx.target();
                if (a == null || t == null) return false;
                return a.method_37908().field_9236 ? MindControlClientView.controls(a, t) : MindControl.controls(a, t);
            }
        });
    }
}
