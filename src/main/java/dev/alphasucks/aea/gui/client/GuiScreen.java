package dev.alphasucks.aea.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.gui.GuiConfig;
import dev.alphasucks.aea.gui.GuiElement;
import dev.alphasucks.aea.gui.GuiModel;
import dev.alphasucks.aea.gui.GuiNetwork;
import dev.alphasucks.aea.gui.GuiPlaceholders;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import dev.alphasucks.aea.gui.GuiSlot;
import dev.alphasucks.aea.gui.client.mixin.SlotAccessor;
import dev.alphasucks.aea.port.client.ClientEntitySets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.class_1041;
import net.minecraft.class_1109;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_4587;
import net.minecraft.class_465;
import net.minecraft.class_490;
import net.minecraft.class_638;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

/**
 * The client screen of an {@code aea:gui}: drawn entirely from the decoded power config plus the state
 * the server sends. Input is forwarded to the server as "button pressed/released" and "slider = value".
 */
public final class GuiScreen extends class_465<GuiScreenHandler> {
    private static final class_2960 WIDGETS = new class_2960("textures/gui/widgets.png");
    private static final class_2960 SLIDER = new class_2960("textures/gui/slider.png");
    private static final class_2960 PANEL = new class_2960("textures/gui/demo_background.png");

    private final GuiScreenHandler menu;
    @Nullable
    private final GuiConfig config;
    @Nullable
    private final GuiModel model;
    private GuiLayout layout;
    private final boolean[] dynamicText;
    private final class_1309[] dummies;
    private final class_1309[] setMembers;
    private boolean dummiesBuilt;
    private int ticks;

    private int heldButton = -1;
    private int draggingSlider = -1;
    private double dragValue;
    private double lastSentValue = Double.NaN;

    public GuiScreen(GuiScreenHandler handler, class_1661 inventory, class_2561 title) {
        super(handler, inventory, title);
        this.menu = handler;
        this.config = handler.config;
        this.model = handler.model;
        int n = model == null ? 0 : model.elementCount();
        this.dynamicText = new boolean[n];
        this.dummies = new class_1309[n];
        this.setMembers = new class_1309[n];
        for (int e = 0; e < n; e++) {
            GuiElement el = model.element(e);
            if (el instanceof GuiElement.Text t) {
                dynamicText[e] = GuiPlaceholders.has(t.text());
            } else if (el instanceof GuiElement.Button b) {
                dynamicText[e] = b.text().isPresent() && GuiPlaceholders.has(b.text().get());
            } else if (el instanceof GuiElement.Slider s) {
                dynamicText[e] = s.text().isPresent() && GuiPlaceholders.has(s.text().get());
            }
        }
    }

    // ---------------------------------------------------------------- layout

    @Override
    protected void method_25426() {
        if (config == null || model == null) {
            this.field_2792 = 0;
            this.field_2779 = 0;
            super.method_25426();
            return;
        }
        class_1041 window = this.field_22787.method_22683();
        double current = window.method_4495();
        this.layout = GuiLayout.compute(config, el -> el instanceof GuiElement.Text t ? this.field_22793.method_27525(resolve(t.text(), -1)) : 0,
            lock -> (float) (window.method_4476(lock, false) / current));
        this.field_2792 = layout.width;
        this.field_2779 = layout.height;
        super.method_25426();
        placeSlots();
        if (!dummiesBuilt) {
            dummiesBuilt = true;
            buildDummies();
        }
    }

    private void placeSlots() {
        for (GuiModel.SlotDef def : model.slots()) {
            GuiSlot slot = menu.guiSlot(def.index());
            if (slot == null) {
                continue;
            }
            GuiLayout.Box b = layout.boxes[def.element()];
            float ux = b.x + def.localX();
            float uy = b.y + def.localY();
            move(slot, Math.round(b.px + (ux - b.px) * b.scale), Math.round(b.py + (uy - b.py) * b.scale));
            slot.drawScale = b.scale;
        }
        if (menu.playerSlotStart >= 0) {
            for (int i = 0; i < 36; i++) {
                class_1735 slot = menu.field_7761.get(menu.playerSlotStart + i);
                int row = i / 9;
                int col = i % 9;
                int y = row < 3 ? layout.inventoryY + 1 + row * 18 : layout.inventoryY + 59;
                move(slot, layout.inventoryX + 1 + col * 18, y);
            }
        }
    }

    private static void move(class_1735 slot, int x, int y) {
        ((SlotAccessor) slot).aea$setX(x);
        ((SlotAccessor) slot).aea$setY(y);
    }

    private void buildDummies() {
        class_638 world = this.field_22787.field_1687;
        if (world == null) {
            return;
        }
        for (int e = 0; e < model.elementCount(); e++) {
            if (!(model.element(e) instanceof GuiElement.EntityView view) || view.entityType().isEmpty()) {
                continue;
            }
            try {
                class_2960 id = view.entityType().get();
                class_1297 entity;
                if (view.nbt().isPresent()) {
                    class_2487 tag = view.nbt().get().tag().method_10553();
                    tag.method_10582("id", id.toString());
                    entity = class_1299.method_5892(tag, world).orElse(null);
                } else {
                    entity = class_7923.field_41177.method_10223(id).method_5883(world);
                }
                if (entity instanceof class_1309 living) {
                    for (GuiElement.Equipment eq : view.equipment()) {
                        living.method_5673(eq.slot(), eq.item().method_7972());
                    }
                    dummies[e] = living;
                }
            } catch (RuntimeException ex) {
                dummies[e] = null;
            }
        }
    }

    // ---------------------------------------------------------------- state helpers

    private boolean shown(int e) {
        return menu.isVisible(e);
    }

    private double sliderValue(int e) {
        return e == draggingSlider ? dragValue : menu.sliders[e];
    }

    private Function<String, String> resolver(int ownSlider) {
        return GuiPlaceholders.resolver(menu, this.field_22787.field_1724, ownSlider, this::sliderValue);
    }

    private class_2561 resolve(class_2561 text, int ownSlider) {
        return GuiPlaceholders.has(text) ? GuiPlaceholders.apply(text, resolver(ownSlider)) : text;
    }

    // ---------------------------------------------------------------- rendering

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        if (config != null && config.showVanillaBackground()) {
            this.method_25420(context);
        }
        super.method_25394(context, mouseX, mouseY, delta);
        this.method_2380(context, mouseX, mouseY);
    }

    @Override
    protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
        if (layout == null) {
            return;
        }
        if (config.showVanillaBackground()) {
            RenderSystem.enableBlend();
            context.method_48587(PANEL, this.field_2776, this.field_2800, Math.max(8, layout.width), Math.max(8, layout.height), 4, 4, 4, 4, 248, 166, 0, 0);
            RenderSystem.disableBlend();
        }
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(this.field_2776, this.field_2800, 0f);
        double mx = mouseX - this.field_2776;
        double my = mouseY - this.field_2800;
        for (int e = 0; e < model.elementCount(); e++) {
            if (!shown(e)) {
                continue;
            }
            GuiElement el = model.element(e);
            if (el instanceof GuiElement.Slot || el instanceof GuiElement.SlotGrid) {
                drawSlotFrames(context, e);
                continue;
            }
            GuiLayout.Box b = layout.boxes[e];
            if (b.scaled()) {
                matrices.method_22903();
                matrices.method_46416(b.px, b.py, 0f);
                matrices.method_22905(b.scale, b.scale, 1f);
                matrices.method_46416(-b.px, -b.py, 0f);
            }
            try {
                drawElement(context, e, el, b, mx, my);
            } finally {
                if (b.scaled()) {
                    matrices.method_22909();
                }
            }
        }
        if (menu.playerSlotStart >= 0) {
            for (int i = 0; i < 36; i++) {
                class_1735 slot = menu.field_7761.get(menu.playerSlotStart + i);
                GuiDraw.slotFrame(context, slot.field_7873, slot.field_7872, 1f);
            }
        }
        matrices.method_22909();
    }

    private void drawSlotFrames(class_332 context, int e) {
        int first = model.firstSlotOf(e);
        int count = model.slotCountOf(e);
        for (int i = first; i < first + count; i++) {
            GuiSlot slot = menu.guiSlot(i);
            if (slot != null && slot.def.background()) {
                GuiDraw.slotFrame(context, slot.field_7873, slot.field_7872, slot.drawScale);
            }
        }
    }

    private void drawElement(class_332 context, int e, GuiElement el, GuiLayout.Box b, double mx, double my) {
        if (el instanceof GuiElement.Image img) {
            float p = img.progress().isPresent() ? menu.progress[e] : 1f;
            if (img.progress().isPresent() && img.invert()) {
                p = 1f - p;
            }
            GuiDraw.image(context, img.texture(), b.x, b.y, b.w, b.h, img.u(), img.v(), img.texWidth(), img.texHeight(), p,
                img.direction() == GuiElement.ProgressDirection.VERTICAL);
        } else if (el instanceof GuiElement.Text t) {
            class_2561 text = dynamicText[e] ? resolve(t.text(), -1) : t.text();
            int tw = this.field_22793.method_27525(text);
            int tx = t.centered() ? b.x + (b.w - tw) / 2 : b.x;
            int ty = b.y + Math.max(0, (b.h - 8) / 2);
            context.method_51439(this.field_22793, text, tx, ty, t.color(), t.shadow());
        } else if (el instanceof GuiElement.Button btn) {
            boolean pressed = heldButton == e;
            boolean hovered = b.contains(mx, my);
            class_2960 tex = btn.texture().orElse(null);
            if (pressed && btn.pressedTexture().isPresent()) {
                tex = btn.pressedTexture().get();
            } else if ((hovered || pressed) && btn.hoverTexture().isPresent()) {
                tex = btn.hoverTexture().get();
            }
            if (tex != null) {
                GuiDraw.stretched(context, tex, b.x, b.y, b.w, b.h);
            } else {
                GuiDraw.nineSlice(context, WIDGETS, b.x, b.y, b.w, b.h, hovered || pressed ? 86 : 66);
            }
            if (btn.text().isPresent()) {
                class_2561 text = dynamicText[e] ? resolve(btn.text().get(), -1) : btn.text().get();
                context.method_27534(this.field_22793, text, b.x + b.w / 2, b.y + (b.h - 8) / 2 + (pressed ? 1 : 0), btn.textColor());
            }
        } else if (el instanceof GuiElement.Slider s) {
            double value = sliderValue(e);
            double norm = Math.max(0, Math.min(1, (value - s.min()) / (s.max() - s.min())));
            boolean active = draggingSlider == e || b.contains(mx, my);
            int hw = Math.min(s.handleWidth(), b.w);
            int hx = b.x + (int) (norm * (b.w - hw));
            if (s.backgroundTexture().isPresent()) {
                GuiDraw.stretched(context, s.backgroundTexture().get(), b.x, b.y, b.w, b.h);
            } else {
                GuiDraw.nineSlice(context, SLIDER, b.x, b.y, b.w, b.h, 0);
            }
            if (s.handleTexture().isPresent()) {
                GuiDraw.stretched(context, s.handleTexture().get(), hx, b.y, hw, b.h);
            } else {
                GuiDraw.nineSlice(context, SLIDER, hx, b.y, hw, b.h, active ? 60 : 40);
            }
            if (s.text().isPresent()) {
                class_2561 text = dynamicText[e] ? resolve(s.text().get(), e) : s.text().get();
                context.method_27534(this.field_22793, text, b.x + b.w / 2, b.y + (b.h - 8) / 2, s.textColor());
            }
        } else if (el instanceof GuiElement.EntityView view) {
            class_1309 living = entityFor(e, view);
            if (living == null) {
                return;
            }
            // Size from the entity type, not the live entity: size mods (Pehkui) scale the live dimensions but
            // reset the scale while an entity is drawn in a GUI, so the live height made the doll tiny or huge.
            net.minecraft.class_4048 dims = living.method_5864().method_18386();
            float height = Math.max(dims.field_18068, 0.3f);
            float width = Math.max(dims.field_18067, 0.3f);
            float fit = Math.min(b.h / height, b.w / width) * 0.85f * view.scale();
            if (view.showScale()) {
                // the entity's own size (Pehkui etc.) relative to normal; the renderer draws it unscaled in GUIs
                float live = living.method_17681() / width;
                if (live > 0f && Float.isFinite(live)) {
                    fit *= Math.max(0.01f, Math.min(100f, live)) / view.fitScale();
                }
            }
            int size = (int) Math.max(1, fit);
            int cx = b.x + b.w / 2;
            int feet = b.y + b.h - 2;
            float offX = 0f;
            float offY = 0f;
            if (view.followMouse()) {
                offX = (float) (cx - b.ux(mx));
                offY = (float) (feet - height * 0.92f * size - b.uy(my));
            }
            try {
                class_490.method_2486(context, cx, feet, size, offX, offY, living);
            } catch (RuntimeException ex) {
                // a renderer that cannot draw outside the world: skip it rather than crash the screen
                if (!entityErrorLogged) {
                    entityErrorLogged = true;
                    dev.alphasucks.aea.AEA.LOGGER.warn("[AEA] aea:gui could not draw {} in an aea:entity element; it is skipped", living, ex);
                }
            }
        }
    }

    private static boolean entityErrorLogged;


    @Nullable
    private class_1309 entityFor(int e, GuiElement.EntityView view) {
        class_310 mc = this.field_22787;
        if (view.entityType().isPresent()) {
            return dummies[e];
        }
        if (view.isSelf()) {
            return mc.field_1724;
        }
        if (view.set().isPresent()) {
            return setMembers[e];
        }
        if (mc.field_1687 == null) {
            return null;
        }
        for (int id : menu.entityIds[e]) {
            if (mc.field_1687.method_8469(id) instanceof class_1309 living) {
                return living;
            }
        }
        return null;
    }

    private void refreshSets() {
        class_638 world = this.field_22787.field_1687;
        if (world == null) {
            return;
        }
        Map<UUID, class_1309> loaded = null;
        for (int e = 0; e < model.elementCount(); e++) {
            if (!(model.element(e) instanceof GuiElement.EntityView view) || view.set().isEmpty()) {
                continue;
            }
            List<UUID> members = ClientEntitySets.members(view.set().get());
            setMembers[e] = null;
            if (members.isEmpty()) {
                continue;
            }
            if (loaded == null) {
                loaded = new HashMap<>();
                Set<UUID> wanted = new HashSet<>();
                for (int k = 0; k < model.elementCount(); k++) {
                    if (model.element(k) instanceof GuiElement.EntityView v && v.set().isPresent()) {
                        wanted.addAll(ClientEntitySets.members(v.set().get()));
                    }
                }
                for (class_1297 entity : world.method_18112()) {
                    if (entity instanceof class_1309 living && wanted.contains(entity.method_5667())) {
                        loaded.put(entity.method_5667(), living);
                    }
                }
            }
            for (UUID uuid : members) {
                class_1309 living = loaded.get(uuid);
                if (living != null) {
                    setMembers[e] = living;
                    break;
                }
            }
        }
    }

    @Override
    protected void method_2388(class_332 context, int mouseX, int mouseY) {
        if (config == null) {
            return;
        }
        boolean bg = config.showVanillaBackground();
        if (config.title().isPresent()) {
            context.method_51439(this.field_22793, this.field_22785, 8, 6, bg ? 0x404040 : 0xFFFFFF, !bg);
        }
        if (bg && menu.playerSlotStart >= 0) {
            context.method_51439(this.field_22793, this.field_29347, layout.inventoryX + 1, layout.inventoryY - 11, 0x404040, false);
        }
    }

    // ---------------------------------------------------------------- input

    /** Topmost visible button or slider under the mouse, or -1. */
    private int interactiveAt(double mouseX, double mouseY) {
        if (layout == null) {
            return -1;
        }
        double mx = mouseX - this.field_2776;
        double my = mouseY - this.field_2800;
        for (int e = model.elementCount() - 1; e >= 0; e--) {
            GuiElement el = model.element(e);
            if ((el instanceof GuiElement.Button || el instanceof GuiElement.Slider) && shown(e) && layout.boxes[e].contains(mx, my)) {
                return e;
            }
        }
        return -1;
    }

    private double sliderAt(int e, double mouseX) {
        GuiElement.Slider s = (GuiElement.Slider) model.element(e);
        GuiLayout.Box b = layout.boxes[e];
        int hw = Math.min(s.handleWidth(), b.w);
        double local = b.ux(mouseX - this.field_2776) - (b.x + hw / 2.0);
        double norm = b.w - hw <= 0 ? 0 : Math.max(0, Math.min(1, local / (b.w - hw)));
        return s.clamp(s.min() + norm * (s.max() - s.min()));
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 0 && model != null) {
            int e = interactiveAt(mouseX, mouseY);
            if (e >= 0) {
                if (model.element(e) instanceof GuiElement.Button) {
                    heldButton = e;
                    sendButton(e, true);
                } else {
                    draggingSlider = e;
                    dragValue = sliderAt(e, mouseX);
                    lastSentValue = Double.NaN;
                }
                this.field_22787.method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0f));
                return true;
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider >= 0) {
            dragValue = sliderAt(draggingSlider, mouseX);
            return true;
        }
        return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean method_25406(double mouseX, double mouseY, int button) {
        if (button == 0 && heldButton >= 0) {
            sendButton(heldButton, false);
            heldButton = -1;
            return true;
        }
        if (button == 0 && draggingSlider >= 0) {
            int e = draggingSlider;
            dragValue = sliderAt(e, mouseX);
            sendSlider(e, dragValue, true);
            menu.sliders[e] = dragValue;    // shown until the server confirms or corrects it
            draggingSlider = -1;
            return true;
        }
        return super.method_25406(mouseX, mouseY, button);
    }

    @Override
    protected void method_37432() {
        super.method_37432();
        if (menu.broken) {
            this.method_25419();
            return;
        }
        ticks++;
        if (draggingSlider >= 0) {
            if (!shown(draggingSlider)) {
                draggingSlider = -1;
            } else if (Double.compare(dragValue, lastSentValue) != 0) {
                sendSlider(draggingSlider, dragValue, false);
                lastSentValue = dragValue;
            }
        }
        if (heldButton >= 0 && !shown(heldButton)) {
            heldButton = -1;    // the server stops it as well
        }
        if (ticks % 10 == 1 && model != null) {
            refreshSets();
        }
    }

    @Override
    public boolean method_25421() {
        return config != null && config.pausesGame();
    }

    private void sendButton(int element, boolean pressed) {
        class_2540 buf = PacketByteBufs.create();
        buf.method_10804(menu.field_7763);
        buf.method_10804(element);
        buf.writeBoolean(pressed);
        ClientPlayNetworking.send(GuiNetwork.BUTTON, buf);
    }

    private void sendSlider(int element, double value, boolean released) {
        class_2540 buf = PacketByteBufs.create();
        buf.method_10804(menu.field_7763);
        buf.method_10804(element);
        buf.writeDouble(value);
        buf.writeBoolean(released);
        ClientPlayNetworking.send(GuiNetwork.SLIDER, buf);
    }
}
