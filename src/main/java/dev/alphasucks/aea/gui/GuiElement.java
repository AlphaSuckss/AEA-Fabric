package dev.alphasucks.aea.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.data.ColorCodecs;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import net.minecraft.class_7923;

/** One element of an {@code aea:gui} canvas. Immutable; the decoded instances may be shared by client and server. */
public sealed interface GuiElement permits GuiElement.Image, GuiElement.Text, GuiElement.Button, GuiElement.Slider,
    GuiElement.EntityView, GuiElement.Slot, GuiElement.SlotGrid {

    Common common();

    String typeName();

    // ------------------------------------------------------------------ shared enums/fields

    enum Anchor implements class_3542 {
        CENTER("center", 0.5f, 0.5f),
        TOP_LEFT("top_left", 0f, 0f),
        TOP("top", 0.5f, 0f),
        TOP_RIGHT("top_right", 1f, 0f),
        LEFT("left", 0f, 0.5f),
        RIGHT("right", 1f, 0.5f),
        BOTTOM_LEFT("bottom_left", 0f, 1f),
        BOTTOM("bottom", 0.5f, 1f),
        BOTTOM_RIGHT("bottom_right", 1f, 1f);

        public static final Codec<Anchor> CODEC = class_3542.method_28140(Anchor::values);
        private final String name;
        public final float fx;
        public final float fy;

        Anchor(String name, float fx, float fy) {
            this.name = name;
            this.fx = fx;
            this.fy = fy;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    enum ProgressDirection implements class_3542 {
        HORIZONTAL("horizontal"),
        VERTICAL("vertical");

        public static final Codec<ProgressDirection> CODEC = class_3542.method_28140(ProgressDirection::values);
        private final String name;

        ProgressDirection(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    enum SlotType implements class_3542 {
        INPUT("input"),
        OUTPUT("output"),
        STORAGE("storage"),
        DISPLAY("display");

        public static final Codec<SlotType> CODEC = class_3542.method_28140(SlotType::values);
        private final String name;

        SlotType(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    /** Fields every element accepts. */
    record Common(int x, int y, Anchor anchor, Optional<Integer> width, Optional<Integer> height,
                  Optional<EntityCondition> condition, Optional<Integer> guiScaleLock) {
        public static final MapCodec<Common> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("x", 0).forGetter(Common::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(Common::y),
            Anchor.CODEC.optionalFieldOf("anchor", Anchor.CENTER).forGetter(Common::anchor),
            Codec.intRange(0, 4096).optionalFieldOf("width").forGetter(Common::width),
            Codec.intRange(0, 4096).optionalFieldOf("height").forGetter(Common::height),
            LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(Common::condition),
            Codec.intRange(1, 6).optionalFieldOf("gui_scale_lock").forGetter(Common::guiScaleLock)
        ).apply(i, Common::new));
    }

    // ------------------------------------------------------------------ element types

    record Image(Common common, class_2960 texture, Optional<Integer> textureWidth, Optional<Integer> textureHeight, int u, int v,
                 Optional<Expression> progress, ProgressDirection direction, boolean invert) implements GuiElement {
        static final MapCodec<Image> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(Image::common),
            class_2960.field_25139.fieldOf("texture_location").forGetter(Image::texture),
            Codec.intRange(1, 8192).optionalFieldOf("texture_width").forGetter(Image::textureWidth),
            Codec.intRange(1, 8192).optionalFieldOf("texture_height").forGetter(Image::textureHeight),
            Codec.INT.optionalFieldOf("u", 0).forGetter(Image::u),
            Codec.INT.optionalFieldOf("v", 0).forGetter(Image::v),
            Expression.DOUBLE_OR_EXPR.optionalFieldOf("progress").forGetter(Image::progress),
            ProgressDirection.CODEC.optionalFieldOf("progress_direction", ProgressDirection.HORIZONTAL).forGetter(Image::direction),
            Codec.BOOL.optionalFieldOf("progress_invert", false).forGetter(Image::invert)
        ).apply(i, Image::new));

        @Override
        public String typeName() {
            return "image";
        }

        public int width() {
            return common.width().orElse(textureWidth.orElse(16));
        }

        public int height() {
            return common.height().orElse(textureHeight.orElse(16));
        }

        public int texWidth() {
            return textureWidth.orElse(width());
        }

        public int texHeight() {
            return textureHeight.orElse(height());
        }
    }

    record Text(Common common, class_2561 text, int color, boolean shadow, boolean centered) implements GuiElement {
        static final MapCodec<Text> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(Text::common),
            TextComponent.CODEC.fieldOf("text").forGetter(Text::text),
            ColorCodecs.ARGB.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Text::color),
            Codec.BOOL.optionalFieldOf("shadow", true).forGetter(Text::shadow),
            Codec.BOOL.optionalFieldOf("centered", false).forGetter(Text::centered)
        ).apply(i, Text::new));

        @Override
        public String typeName() {
            return "text";
        }
    }

    record Button(Common common, Optional<class_2960> texture, Optional<class_2960> hoverTexture, Optional<class_2960> pressedTexture,
                  Optional<class_2561> text, int textColor, Optional<EntityAction> onPress, Optional<EntityAction> whilePressed,
                  Optional<EntityAction> onRelease, Optional<EntityCondition> pressCondition, int cooldown) implements GuiElement {
        static final MapCodec<Button> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(Button::common),
            class_2960.field_25139.optionalFieldOf("texture_location").forGetter(Button::texture),
            class_2960.field_25139.optionalFieldOf("hover_texture_location").forGetter(Button::hoverTexture),
            class_2960.field_25139.optionalFieldOf("pressed_texture_location").forGetter(Button::pressedTexture),
            TextComponent.CODEC.optionalFieldOf("text").forGetter(Button::text),
            ColorCodecs.ARGB.optionalFieldOf("text_color", 0xFFFFFFFF).forGetter(Button::textColor),
            LoggedOptionalField.strict("action_on_press", EntityAction.CODEC).forGetter(Button::onPress),
            LoggedOptionalField.strict("action_while_pressed", EntityAction.CODEC).forGetter(Button::whilePressed),
            LoggedOptionalField.strict("action_on_release", EntityAction.CODEC).forGetter(Button::onRelease),
            LoggedOptionalField.strict("press_condition", EntityCondition.CODEC).forGetter(Button::pressCondition),
            Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("cooldown", 0).forGetter(Button::cooldown)
        ).apply(i, Button::new));

        @Override
        public String typeName() {
            return "button";
        }

        public int width() {
            return common.width().orElse(80);
        }

        public int height() {
            return common.height().orElse(20);
        }
    }

    record Slider(Common common, String id, double min, double max, Optional<Double> startValue, double step,
                  Optional<class_2960> backgroundTexture, Optional<class_2960> handleTexture, int handleWidth, Optional<class_2561> text,
                  int textColor, Optional<EntityAction> onSlide, Optional<EntityAction> onRelease, Optional<EntityCondition> slideCondition)
        implements GuiElement {
        static final MapCodec<Slider> CODEC = RecordCodecBuilder.<Slider>mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(Slider::common),
            Codec.STRING.fieldOf("id").forGetter(Slider::id),
            Codec.DOUBLE.optionalFieldOf("min", 0.0).forGetter(Slider::min),
            Codec.DOUBLE.optionalFieldOf("max", 100.0).forGetter(Slider::max),
            Codec.DOUBLE.optionalFieldOf("start_value").forGetter(Slider::startValue),
            Codec.doubleRange(0.0, Double.MAX_VALUE).optionalFieldOf("step", 1.0).forGetter(Slider::step),
            class_2960.field_25139.optionalFieldOf("background_texture_location").forGetter(Slider::backgroundTexture),
            class_2960.field_25139.optionalFieldOf("slider_texture_location").forGetter(Slider::handleTexture),
            Codec.intRange(1, 4096).optionalFieldOf("slider_width", 8).forGetter(Slider::handleWidth),
            TextComponent.CODEC.optionalFieldOf("text").forGetter(Slider::text),
            ColorCodecs.ARGB.optionalFieldOf("text_color", 0xFFFFFFFF).forGetter(Slider::textColor),
            LoggedOptionalField.strict("action_on_slide", EntityAction.CODEC).forGetter(Slider::onSlide),
            LoggedOptionalField.strict("action_on_release", EntityAction.CODEC).forGetter(Slider::onRelease),
            LoggedOptionalField.strict("slide_condition", EntityCondition.CODEC).forGetter(Slider::slideCondition)
        ).apply(i, Slider::new)).flatXmap(Slider::validate, DataResult::success);

        private static DataResult<Slider> validate(Slider s) {
            if (s.id.isBlank()) {
                return DataResult.error(() -> "An aea:slider needs a non-empty \"id\"");
            }
            if (!(s.max > s.min) || Double.isNaN(s.min) || Double.isInfinite(s.min) || Double.isInfinite(s.max)) {
                return DataResult.error(() -> "aea:slider '" + s.id + "': \"max\" must be greater than \"min\"");
            }
            return DataResult.success(s);
        }

        @Override
        public String typeName() {
            return "slider";
        }

        public int width() {
            return common.width().orElse(120);
        }

        public int height() {
            return common.height().orElse(20);
        }

        public double start() {
            return clamp(startValue.orElse(min));
        }

        /** Clamps to [min, max] and snaps to {@code step} (counted from {@code min}). */
        public double clamp(double value) {
            if (Double.isNaN(value)) {
                return min;
            }
            double v = Math.max(min, Math.min(max, value));
            if (step > 0) {
                v = min + Math.round((v - min) / step) * step;
                v = Math.max(min, Math.min(max, v));
                // tidy binary noise such as 0.30000000000000004
                v = Math.round(v * 1.0e9) / 1.0e9;
            }
            return v;
        }
    }

    record Equipment(class_1304 slot, class_1799 item) {
        static final Codec<Equipment> CODEC = RecordCodecBuilder.create(i -> i.group(
            GuiCodecs.EQUIPMENT_SLOT.fieldOf("slot").forGetter(Equipment::slot),
            GuiCodecs.ITEM_STACK.fieldOf("item").forGetter(Equipment::item)
        ).apply(i, Equipment::new));
    }

    record EntityView(Common common, Optional<class_2960> entityType, List<Equipment> equipment, Optional<Nbt> nbt,
                      Optional<String> selector, Optional<class_2960> set, boolean followMouse, float scale,
                      boolean showScale, float fitScale) implements GuiElement {
        static final MapCodec<EntityView> CODEC = RecordCodecBuilder.<EntityView>mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(EntityView::common),
            IdCodecs.ID.optionalFieldOf("entity_type").forGetter(EntityView::entityType),
            Equipment.CODEC.listOf().optionalFieldOf("equipment", List.of()).forGetter(EntityView::equipment),
            Nbt.CODEC.optionalFieldOf("nbt").forGetter(EntityView::nbt),
            Codec.STRING.optionalFieldOf("selector").forGetter(EntityView::selector),
            IdCodecs.ID.optionalFieldOf("set").forGetter(EntityView::set),
            Codec.BOOL.optionalFieldOf("follow_mouse", false).forGetter(EntityView::followMouse),
            Codec.floatRange(0.01f, 100f).optionalFieldOf("scale", 1f).forGetter(EntityView::scale),
            Codec.BOOL.optionalFieldOf("show_scale", false).forGetter(EntityView::showScale),
            Codec.floatRange(0.01f, 100f).optionalFieldOf("fit_scale", 1f).forGetter(EntityView::fitScale)
        ).apply(i, EntityView::new)).flatXmap(EntityView::validate, DataResult::success);

        private static DataResult<EntityView> validate(EntityView e) {
            int sources = (e.entityType.isPresent() ? 1 : 0) + (e.selector.isPresent() ? 1 : 0) + (e.set.isPresent() ? 1 : 0);
            if (sources != 1) {
                return DataResult.error(() -> "An aea:entity element needs exactly one of \"entity_type\", \"selector\" or \"set\"");
            }
            if (e.entityType.isPresent() && class_7923.field_41177.method_17966(e.entityType.get()).isEmpty()) {
                return DataResult.error(() -> "aea:entity: unknown entity type '" + e.entityType.get() + "'");
            }
            if (e.selector.isPresent() && !e.isSelf()) {
                try {
                    new net.minecraft.class_2303(new com.mojang.brigadier.StringReader(e.selector.get())).method_9882();
                } catch (com.mojang.brigadier.exceptions.CommandSyntaxException ex) {
                    return DataResult.error(() -> "aea:entity: invalid selector '" + e.selector.get() + "': " + ex.getMessage());
                }
            }
            return DataResult.success(e);
        }

        public boolean isSelf() {
            return selector.isPresent() && selector.get().trim().equals("@s");
        }

        /** Server-resolved selectors (anything but {@code @s}). */
        public boolean resolvedOnServer() {
            return selector.isPresent() && !isSelf();
        }

        @Override
        public String typeName() {
            return "entity";
        }

        public int width() {
            return common.width().orElse(50);
        }

        public int height() {
            return common.height().orElse(70);
        }
    }

    record Slot(Common common, SlotType slotType, Optional<String> id, Optional<ItemCondition> itemCondition, Optional<Integer> maxStack,
                Optional<class_1799> item, boolean background) implements GuiElement {
        static final MapCodec<Slot> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(Slot::common),
            SlotType.CODEC.optionalFieldOf("slot_type", SlotType.STORAGE).forGetter(Slot::slotType),
            Codec.STRING.optionalFieldOf("id").forGetter(Slot::id),
            LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(Slot::itemCondition),
            Codec.intRange(1, 64).optionalFieldOf("max_stack").forGetter(Slot::maxStack),
            GuiCodecs.ITEM_STACK.optionalFieldOf("item").forGetter(Slot::item),
            Codec.BOOL.optionalFieldOf("background", true).forGetter(Slot::background)
        ).apply(i, Slot::new));

        @Override
        public String typeName() {
            return "slot";
        }
    }

    record SlotGrid(Common common, int rows, int columns, List<String> visible, boolean trim, int cellSize, SlotType slotType,
                    Optional<String> id, Optional<ItemCondition> itemCondition, Optional<Integer> maxStack, Optional<class_1799> item,
                    boolean background) implements GuiElement {
        static final MapCodec<SlotGrid> CODEC = RecordCodecBuilder.<SlotGrid>mapCodec(i -> i.group(
            Common.MAP_CODEC.forGetter(SlotGrid::common),
            Codec.intRange(1, 32).fieldOf("rows").forGetter(SlotGrid::rows),
            Codec.intRange(1, 32).fieldOf("columns").forGetter(SlotGrid::columns),
            GuiCodecs.STRING_OR_LIST.optionalFieldOf("visible", List.of()).forGetter(SlotGrid::visible),
            Codec.BOOL.optionalFieldOf("trim", true).forGetter(SlotGrid::trim),
            Codec.intRange(16, 256).optionalFieldOf("cell_size", 18).forGetter(SlotGrid::cellSize),
            SlotType.CODEC.optionalFieldOf("slot_type", SlotType.STORAGE).forGetter(SlotGrid::slotType),
            Codec.STRING.optionalFieldOf("id").forGetter(SlotGrid::id),
            LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(SlotGrid::itemCondition),
            Codec.intRange(1, 64).optionalFieldOf("max_stack").forGetter(SlotGrid::maxStack),
            GuiCodecs.ITEM_STACK.optionalFieldOf("item").forGetter(SlotGrid::item),
            Codec.BOOL.optionalFieldOf("background", true).forGetter(SlotGrid::background)
        ).apply(i, SlotGrid::new)).flatXmap(SlotGrid::validate, DataResult::success);

        private static DataResult<SlotGrid> validate(SlotGrid g) {
            for (String cell : g.visible) {
                DataResult<int[]> r = GuiCodecs.parseCellRange(cell, g.rows, g.columns);
                if (r.error().isPresent()) {
                    return DataResult.error(() -> r.error().get().message());
                }
            }
            return DataResult.success(g);
        }

        /** [row][col] (0-based) of the cells that hold a slot. */
        public boolean[][] cells() {
            boolean[][] out = new boolean[rows][columns];
            if (visible.isEmpty()) {
                for (boolean[] row : out) {
                    java.util.Arrays.fill(row, true);
                }
                return out;
            }
            for (String cell : visible) {
                int[] r = GuiCodecs.parseCellRange(cell, rows, columns).result().orElse(null);
                if (r == null) {
                    continue;
                }
                for (int row = r[0]; row <= r[1]; row++) {
                    for (int col = r[2]; col <= r[3]; col++) {
                        out[row - 1][col - 1] = true;
                    }
                }
            }
            return out;
        }

        @Override
        public String typeName() {
            return "slot_grid";
        }
    }

    // ------------------------------------------------------------------ dispatch

    private static MapCodec<? extends GuiElement> byName(String name) {
        return switch (name) {
            case "image" -> Image.CODEC;
            case "text" -> Text.CODEC;
            case "button" -> Button.CODEC;
            case "slider" -> Slider.CODEC;
            case "entity" -> EntityView.CODEC;
            case "slot" -> Slot.CODEC;
            case "slot_grid" -> SlotGrid.CODEC;
            default -> null;
        };
    }

    private static DataResult<String> normalize(String raw) {
        String name = raw.startsWith("aea:") ? raw.substring(4) : raw;
        return byName(name) == null
            ? DataResult.error(() -> "Unknown aea:gui element type '" + raw + "' (expected aea:image, aea:text, aea:button, aea:slider, aea:entity, aea:slot or aea:slot_grid)")
            : DataResult.success(name);
    }

    private static DataResult<Codec<? extends GuiElement>> codecFor(String name) {
        MapCodec<? extends GuiElement> codec = byName(name);
        return codec == null ? DataResult.error(() -> "Unknown aea:gui element type '" + name + "'") : DataResult.success(codec.codec());
    }

    Codec<String> TYPE_KEY = Codec.STRING.comapFlatMap(GuiElement::normalize, name -> "aea:" + name);

    Codec<GuiElement> CODEC = TYPE_KEY.<GuiElement>partialDispatch("type", e -> DataResult.success(e.typeName()), GuiElement::codecFor);
}
