package dev.alphasucks.aea.gui.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.bridge.BridgedPower;
import dev.alphasucks.aea.gui.GuiConfig;
import dev.alphasucks.aea.gui.GuiElement;
import dev.alphasucks.aea.gui.GuiMemory;
import dev.alphasucks.aea.gui.GuiPowers;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import dev.alphasucks.aea.gui.GuiSlot;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Comparison;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

/** Entity conditions of the {@code aea:gui} feature. They read the menu of the side they run on. */
public final class GuiConditions {
    private GuiConditions() {
    }

    public static void register() {
        ConditionTypes.ENTITY.register(new class_2960("aea", "gui_open"), new GuiOpen());
        ConditionTypes.ENTITY.register(new class_2960("aea", "gui_slot"), new GuiSlotCondition());
        ConditionTypes.ENTITY.register(new class_2960("aea", "gui_value"), new GuiValue());
    }

    // ------------------------------------------------------------------ aea:gui_open

    public record OpenCfg(Optional<class_2960> gui) {
    }

    public static final class GuiOpen implements ConditionType<EntityCtx, OpenCfg> {
        @Override
        public MapCodec<OpenCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(IdCodecs.ID.optionalFieldOf("gui").forGetter(OpenCfg::gui)).apply(i, OpenCfg::new));
        }

        @Override
        public boolean test(OpenCfg cfg, EntityCtx ctx) {
            GuiScreenHandler h = GuiPowers.open(ctx.entity());
            return h != null && !h.broken && (cfg.gui().isEmpty() || cfg.gui().get().equals(h.guiId));
        }
    }

    // ------------------------------------------------------------------ aea:gui_slot

    public record SlotCfg(String slot, Optional<ItemCondition> itemCondition, Optional<Comparison> comparison, int compareTo) {
    }

    /**
     * Without {@code comparison}: true if any named slot passes {@code item_condition} (no condition = not empty).
     * With it: compares the total item count of the passing slots.
     */
    public static final class GuiSlotCondition implements ConditionType<EntityCtx, SlotCfg> {
        @Override
        public MapCodec<SlotCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("slot").forGetter(SlotCfg::slot),
                LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(SlotCfg::itemCondition),
                Comparison.CODEC.optionalFieldOf("comparison").forGetter(SlotCfg::comparison),
                Codec.INT.optionalFieldOf("compare_to", 1).forGetter(SlotCfg::compareTo)
            ).apply(i, SlotCfg::new));
        }

        @Override
        public boolean test(SlotCfg cfg, EntityCtx ctx) {
            GuiScreenHandler h = GuiPowers.open(ctx.entity());
            if (h == null || h.model == null) {
                return false;
            }
            class_1657 player = (class_1657) ctx.entity();
            List<GuiSlot> slots = h.resolveSlots(cfg.slot());
            boolean any = false;
            int count = 0;
            for (GuiSlot s : slots) {
                class_1799 stack = s.method_7677();
                boolean pass;
                if (cfg.itemCondition().isPresent()) {
                    try {
                        pass = cfg.itemCondition().get().test(new ItemCtx(stack, player.method_37908(), player));
                    } catch (RuntimeException e) {
                        pass = false;
                    }
                } else {
                    pass = !stack.method_7960();
                }
                if (pass) {
                    any = true;
                    count += stack.method_7947();
                }
            }
            return cfg.comparison().isPresent() ? cfg.comparison().get().compare(count, cfg.compareTo()) : any;
        }
    }

    // ------------------------------------------------------------------ aea:gui_value

    public record ValueCfg(Optional<class_2960> gui, String slider, Comparison comparison, double compareTo) {
    }

    /** Value of a slider: of the open GUI, else the remembered value ({@code memory}), else the slider's start value. */
    public static final class GuiValue implements ConditionType<EntityCtx, ValueCfg> {
        @Override
        public MapCodec<ValueCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                IdCodecs.ID.optionalFieldOf("gui").forGetter(ValueCfg::gui),
                Codec.STRING.fieldOf("slider").forGetter(ValueCfg::slider),
                Comparison.CODEC.fieldOf("comparison").forGetter(ValueCfg::comparison),
                Codec.DOUBLE.fieldOf("compare_to").forGetter(ValueCfg::compareTo)
            ).apply(i, ValueCfg::new));
        }

        @Override
        public boolean test(ValueCfg cfg, EntityCtx ctx) {
            double value = value(ctx.entity() instanceof class_1657 p ? p : null, cfg.gui(), cfg.slider());
            return !Double.isNaN(value) && cfg.comparison().compare(value, cfg.compareTo());
        }

        static double value(class_1657 player, Optional<class_2960> gui, String slider) {
            if (player == null) {
                return Double.NaN;
            }
            GuiScreenHandler h = GuiPowers.open(player);
            if (h != null && h.model != null && (gui.isEmpty() || gui.get().equals(h.guiId))) {
                double v = h.sliderValue(slider);
                if (!Double.isNaN(v)) {
                    return v;
                }
            }
            if (gui.isEmpty()) {
                return Double.NaN;
            }
            BridgedPower<GuiConfig> power = GuiPowers.byGuiId(player, gui.get());
            if (!player.method_37908().method_8608()) {
                double stored = GuiMemory.slider(player, gui.get(), slider);
                if (!Double.isNaN(stored)) {
                    return stored;
                }
            }
            if (power != null) {
                int e = power.config().model().sliderElement(slider);
                if (e >= 0 && power.config().model().element(e) instanceof GuiElement.Slider s) {
                    return s.start();
                }
            }
            return Double.NaN;
        }
    }
}
