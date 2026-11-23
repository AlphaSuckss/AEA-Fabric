package dev.alphasucks.aea.gui.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.gui.GuiCodecs;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import dev.alphasucks.aea.gui.GuiSlot;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_3542;

/**
 * {@code aea:modify_gui_slot}: {@code set}, {@code add}, {@code consume} or {@code clear} the stack of a slot
 * (or of every cell of a grid) of the open GUI. {@code add} and {@code consume} are all-or-nothing.
 */
public final class ModifyGuiSlotAction implements ActionType<EntityCtx, ModifyGuiSlotAction.Cfg> {
    public enum Operation implements class_3542 {
        SET("set"),
        ADD("add"),
        CONSUME("consume"),
        CLEAR("clear");

        public static final Codec<Operation> CODEC = class_3542.method_28140(Operation::values);
        private final String name;

        Operation(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    public record Cfg(String slot, Operation operation, Optional<class_1799> item, Optional<Integer> count) {
    }

    @Override
    public MapCodec<Cfg> codec() {
        return RecordCodecBuilder.<Cfg>mapCodec(i -> i.group(
            Codec.STRING.fieldOf("slot").forGetter(Cfg::slot),
            Operation.CODEC.fieldOf("operation").forGetter(Cfg::operation),
            GuiCodecs.ITEM_STACK.optionalFieldOf("item").forGetter(Cfg::item),
            Codec.intRange(0, 4096).optionalFieldOf("count").forGetter(Cfg::count)
        ).apply(i, Cfg::new)).flatXmap(cfg -> (cfg.operation == Operation.SET || cfg.operation == Operation.ADD) && cfg.item.isEmpty()
            ? DataResult.error(() -> "aea:modify_gui_slot: \"" + cfg.operation.method_15434() + "\" needs an \"item\"")
            : DataResult.success(cfg), DataResult::success);
    }

    @Override
    public void run(Cfg cfg, EntityCtx ctx) {
        GuiScreenHandler h = GuiActions.serverGui(ctx.entity());
        if (h == null) {
            return;
        }
        List<GuiSlot> slots = h.resolveSlots(cfg.slot());
        if (slots.isEmpty()) {
            return;
        }
        switch (cfg.operation) {
            case CLEAR -> {
                for (GuiSlot s : slots) {
                    s.method_7673(class_1799.field_8037);
                }
            }
            case SET -> {
                class_1799 template = cfg.item.get();
                int count = cfg.count.orElse(template.method_7947());
                for (GuiSlot s : slots) {
                    if (count <= 0) {
                        s.method_7673(class_1799.field_8037);
                    } else {
                        s.method_7673(template.method_46651(Math.min(count, capacity(s, template))));
                    }
                }
            }
            case ADD -> add(slots, cfg.item.get(), cfg.count.orElse(cfg.item.get().method_7947()));
            case CONSUME -> consume(slots, cfg.item.orElse(null), cfg.count.orElse(1));
        }
    }

    /** Most of {@code stack} a slot can hold (slot limit and item limit). */
    static int capacity(GuiSlot slot, class_1799 stack) {
        return Math.max(1, Math.min(slot.method_7675(), stack.method_7914()));
    }

    /** Inserts {@code amount} of {@code template} only if all of it fits. Returns whether it did. */
    static boolean add(List<GuiSlot> slots, class_1799 template, int amount) {
        if (amount <= 0 || template.method_7960()) {
            return false;
        }
        int room = 0;
        for (GuiSlot s : slots) {
            class_1799 cur = s.method_7677();
            int cap = capacity(s, template);
            if (cur.method_7960()) {
                room += cap;
            } else if (class_1799.method_31577(cur, template)) {
                room += Math.max(0, cap - cur.method_7947());
            }
            if (room >= amount) {
                break;
            }
        }
        if (room < amount) {
            return false;
        }
        int left = amount;
        for (int pass = 0; pass < 2 && left > 0; pass++) {
            for (GuiSlot s : slots) {
                if (left <= 0) {
                    break;
                }
                class_1799 cur = s.method_7677();
                int cap = capacity(s, template);
                if (pass == 0 && !cur.method_7960() && class_1799.method_31577(cur, template)) {
                    int put = Math.min(left, cap - cur.method_7947());
                    if (put > 0) {
                        cur.method_7933(put);
                        s.method_7668();
                        left -= put;
                    }
                } else if (pass == 1 && cur.method_7960()) {
                    int put = Math.min(left, cap);
                    s.method_7673(template.method_46651(put));
                    left -= put;
                }
            }
        }
        return true;
    }

    /** Removes {@code amount} items (matching {@code filter} if given) only if that many are there. */
    static boolean consume(List<GuiSlot> slots, class_1799 filter, int amount) {
        if (amount <= 0) {
            return false;
        }
        int available = 0;
        for (GuiSlot s : slots) {
            if (matches(s.method_7677(), filter)) {
                available += s.method_7677().method_7947();
            }
        }
        if (available < amount) {
            return false;
        }
        int left = amount;
        for (GuiSlot s : slots) {
            if (left <= 0) {
                break;
            }
            class_1799 cur = s.method_7677();
            if (!matches(cur, filter)) {
                continue;
            }
            int take = Math.min(left, cur.method_7947());
            cur.method_7934(take);
            left -= take;
            if (cur.method_7960()) {
                s.method_7673(class_1799.field_8037);
            } else {
                s.method_7668();
            }
        }
        return true;
    }

    private static boolean matches(class_1799 stack, class_1799 filter) {
        if (stack.method_7960()) {
            return false;
        }
        if (filter == null) {
            return true;
        }
        return filter.method_7985() ? class_1799.method_31577(stack, filter) : stack.method_31574(filter.method_7909());
    }
}
