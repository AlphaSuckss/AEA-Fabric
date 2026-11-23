package dev.alphasucks.aea.gui.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.gui.GuiCodecs;
import dev.alphasucks.aea.gui.GuiModel;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import dev.alphasucks.aea.gui.GuiSlot;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1263;
import net.minecraft.class_1277;
import net.minecraft.class_1657;
import net.minecraft.class_1715;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1860;
import net.minecraft.class_1863;
import net.minecraft.class_1937;
import net.minecraft.class_2371;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import net.minecraft.class_3956;

/**
 * {@code aea:gui_craft}: runs one vanilla (or datapack) recipe on GUI slots - matches the inputs, consumes one
 * craft's ingredients (placing remainders such as empty buckets), and puts the result into the output slot.
 * Nothing happens if no recipe matches or the result does not fit.
 */
public final class GuiCraftAction implements ActionType<EntityCtx, GuiCraftAction.Cfg> {
    public enum Kind implements class_3542 {
        CRAFTING("crafting"),
        SMELTING("smelting"),
        BLASTING("blasting"),
        SMOKING("smoking"),
        CAMPFIRE_COOKING("campfire_cooking"),
        STONECUTTING("stonecutting"),
        SMITHING("smithing");

        public static final Codec<Kind> CODEC = class_3542.method_28140(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }

        @SuppressWarnings("rawtypes")
        class_3956 type() {
            return switch (this) {
                case CRAFTING -> class_3956.field_17545;
                case SMELTING -> class_3956.field_17546;
                case BLASTING -> class_3956.field_17547;
                case SMOKING -> class_3956.field_17548;
                case CAMPFIRE_COOKING -> class_3956.field_17549;
                case STONECUTTING -> class_3956.field_17641;
                case SMITHING -> class_3956.field_25388;
            };
        }
    }

    public record Cfg(List<String> input, String output, Kind kind, Optional<class_2960> recipe) {
    }

    @Override
    public MapCodec<Cfg> codec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
            GuiCodecs.STRING_OR_LIST.fieldOf("input").forGetter(Cfg::input),
            Codec.STRING.fieldOf("output").forGetter(Cfg::output),
            Kind.CODEC.optionalFieldOf("recipe_type", Kind.CRAFTING).forGetter(Cfg::kind),
            IdCodecs.ID.optionalFieldOf("recipe").forGetter(Cfg::recipe)
        ).apply(i, Cfg::new));
    }

    /** Recipe inventory plus, for each of its positions, the GUI slot it came from (null = empty filler). */
    private record Input(class_1263 inventory, GuiSlot[] sources) {
    }

    @Override
    public void run(Cfg cfg, EntityCtx ctx) {
        GuiScreenHandler h = GuiActions.serverGui(ctx.entity());
        if (h == null) {
            return;
        }
        class_1657 player = (class_1657) ctx.entity();
        class_1937 world = player.method_37908();
        List<GuiSlot> outputs = h.resolveSlots(cfg.output());
        if (outputs.isEmpty()) {
            return;
        }
        GuiSlot output = outputs.get(0);
        Input input = buildInput(h, cfg);
        if (input == null) {
            return;
        }
        class_1860<class_1263> recipe = find(world, cfg.kind().type(), input.inventory(), cfg.recipe());
        if (recipe == null) {
            return;
        }
        class_1799 result = recipe.method_8116(input.inventory(), world.method_30349());
        if (result.method_7960() || !fits(output, result)) {
            return;
        }
        List<class_1799> remainders = remainders(cfg.kind(), recipe, input);
        for (int pos = 0; pos < input.sources().length; pos++) {
            GuiSlot src = input.sources()[pos];
            if (src == null || src.method_7677().method_7960()) {
                continue;
            }
            src.method_7671(1);
            class_1799 rem = pos < remainders.size() ? remainders.get(pos) : class_1799.field_8037;
            if (rem.method_7960()) {
                continue;
            }
            class_1799 cur = src.method_7677();
            if (cur.method_7960()) {
                src.method_7673(rem.method_7972());
            } else if (class_1799.method_31577(cur, rem) && cur.method_7947() + rem.method_7947() <= ModifyGuiSlotAction.capacity(src, cur)) {
                cur.method_7933(rem.method_7947());
                src.method_7668();
            } else {
                player.method_31548().method_7398(rem.method_7972());
            }
        }
        result.method_7982(world, player, result.method_7947());
        class_1799 cur = output.method_7677();
        if (cur.method_7960()) {
            output.method_7673(result);
        } else {
            cur.method_7933(result.method_7947());
            output.method_7668();
        }
    }

    private static boolean fits(GuiSlot output, class_1799 result) {
        class_1799 cur = output.method_7677();
        int cap = ModifyGuiSlotAction.capacity(output, result);
        if (cur.method_7960()) {
            return result.method_7947() <= cap;
        }
        return class_1799.method_31577(cur, result) && cur.method_7947() + result.method_7947() <= cap;
    }

    private static Input buildInput(GuiScreenHandler h, Cfg cfg) {
        if (cfg.kind() == Kind.CRAFTING) {
            GuiModel.Grid grid = cfg.input().size() == 1 ? h.model.grid(cfg.input().get(0)) : null;
            if (grid != null) {
                return craftingFromGrid(h, grid);
            }
            List<GuiSlot> slots = flatten(h, cfg.input());
            int n = slots.size();
            int side = n <= 1 ? 1 : n <= 4 ? 2 : n <= 9 ? 3 : -1;
            if (side < 0 || n == 0) {
                return null;
            }
            GuiSlot[] sources = new GuiSlot[side * side];
            class_2371<class_1799> stacks = class_2371.method_10213(side * side, class_1799.field_8037);
            for (int i = 0; i < n; i++) {
                sources[i] = slots.get(i);
                stacks.set(i, slots.get(i).method_7677().method_7972());
            }
            return new Input(new class_1715(h, side, side, stacks), sources);
        }
        List<GuiSlot> slots = flatten(h, cfg.input());
        if (cfg.kind() == Kind.SMITHING) {
            if (slots.size() < 3) {
                return null;
            }
            class_1277 inv = new class_1277(3);
            GuiSlot[] sources = new GuiSlot[3];
            for (int i = 0; i < 3; i++) {
                sources[i] = slots.get(i);
                inv.method_5447(i, slots.get(i).method_7677().method_7972());
            }
            return new Input(inv, sources);
        }
        for (GuiSlot s : slots) {
            if (!s.method_7677().method_7960()) {
                return new Input(new class_1277(s.method_7677().method_46651(1)), new GuiSlot[]{s});
            }
        }
        return null;
    }

    private static Input craftingFromGrid(GuiScreenHandler h, GuiModel.Grid grid) {
        int minR = Integer.MAX_VALUE, maxR = -1, minC = Integer.MAX_VALUE, maxC = -1;
        for (int r = 0; r < grid.rows(); r++) {
            for (int c = 0; c < grid.cols(); c++) {
                GuiSlot s = h.guiSlot(grid.cells()[r][c]);
                if (s != null && !s.method_7677().method_7960()) {
                    minR = Math.min(minR, r);
                    maxR = Math.max(maxR, r);
                    minC = Math.min(minC, c);
                    maxC = Math.max(maxC, c);
                }
            }
        }
        if (maxR < 0) {
            return null;
        }
        int height = maxR - minR + 1;
        int width = maxC - minC + 1;
        if (width > 3 || height > 3) {
            return null;
        }
        GuiSlot[] sources = new GuiSlot[width * height];
        class_2371<class_1799> stacks = class_2371.method_10213(width * height, class_1799.field_8037);
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                GuiSlot s = h.guiSlot(grid.cells()[minR + r][minC + c]);
                if (s != null) {
                    sources[r * width + c] = s;
                    stacks.set(r * width + c, s.method_7677().method_7972());
                }
            }
        }
        return new Input(new class_1715(h, width, height, stacks), sources);
    }

    private static List<GuiSlot> flatten(GuiScreenHandler h, List<String> ids) {
        List<GuiSlot> out = new ArrayList<>();
        for (String id : ids) {
            out.addAll(h.resolveSlots(id));
        }
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class_1860<class_1263> find(class_1937 world, class_3956 type, class_1263 inventory, Optional<class_2960> id) {
        class_1863 manager = world.method_8433();
        try {
            if (id.isPresent()) {
                Optional<? extends class_1860<?>> named = manager.method_8130(id.get());
                if (named.isPresent() && named.get().method_17716() == type && ((class_1860) named.get()).method_8115(inventory, world)) {
                    return (class_1860<class_1263>) named.get();
                }
                return null;
            }
            Optional found = manager.method_8132(type, inventory, world);
            return found.isPresent() ? (class_1860<class_1263>) found.get() : null;
        } catch (RuntimeException e) {
            return null;    // a recipe that cannot handle this inventory type (e.g. a mod's crafting recipe)
        }
    }

    private static List<class_1799> remainders(Kind kind, class_1860<class_1263> recipe, Input input) {
        if (kind == Kind.CRAFTING) {
            return recipe.method_8111(input.inventory());
        }
        List<class_1799> out = new ArrayList<>();
        for (GuiSlot src : input.sources()) {
            class_1792 rem = src == null || src.method_7677().method_7960() ? null : src.method_7677().method_7909().method_7858();
            out.add(rem == null ? class_1799.field_8037 : new class_1799(rem));
        }
        return out;
    }
}
