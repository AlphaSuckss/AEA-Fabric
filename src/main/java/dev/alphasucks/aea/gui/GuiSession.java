package dev.alphasucks.aea.gui;

import com.mojang.brigadier.StringReader;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.bridge.BridgedPower;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_2300;
import net.minecraft.class_2303;
import net.minecraft.class_2487;
import net.minecraft.class_3222;

/**
 * Server-side logic of one open GUI (owned by the server's {@link GuiScreenHandler}, so it is per player
 * and never shared with the client). The client's packets only reach {@link #pressButton},
 * {@link #releaseButton} and {@link #moveSlider}; everything is validated here.
 */
public final class GuiSession {
    private static final int REFRESH_INTERVAL = 5;
    private static final int MEMORY_SAFETY_SAVE = 100;
    private static final int MAX_ENTITY_IDS = 16;

    private final GuiScreenHandler h;
    private final class_3222 player;
    private final GuiConfig config;
    private final GuiModel model;
    private final int n;

    private final BitSet sentVisible = new BitSet();
    private final float[] sentProgress;
    private final double[] sentSliders;
    private final int[][] sentEntities;

    private final double[] pendingSlider;
    private final boolean[] pendingRelease;
    private final boolean[] held;
    private final int[] cooldown;
    private final long[] lastPress;

    private int ticks;
    private int sinceSave;
    private boolean loading;
    private boolean closing;

    GuiSession(GuiScreenHandler h, class_3222 player) {
        this.h = h;
        this.player = player;
        this.config = h.config;
        this.model = h.model;
        this.n = model.elementCount();
        this.sentProgress = new float[n];
        this.sentSliders = new double[n];
        this.sentEntities = new int[n][];
        this.pendingSlider = new double[n];
        this.pendingRelease = new boolean[n];
        this.held = new boolean[n];
        this.cooldown = new int[n];
        this.lastPress = new long[n];
        Arrays.fill(pendingSlider, Double.NaN);
        Arrays.fill(lastPress, Long.MIN_VALUE);
        if (config.memory()) {
            h.inventory.method_5489(inv -> {
                if (!loading && !h.isClosed()) {
                    GuiMemory.save(player, h);
                    sinceSave = 0;
                }
            });
        }
    }

    public class_3222 player() {
        return player;
    }

    public GuiScreenHandler h() {
        return h;
    }

    public boolean closing() {
        return closing;
    }

    // ---------------------------------------------------------------- lifecycle

    void onOpen() {
        if (config.memory()) {
            loadMemory();
        }
        evaluateVisibility();
        evaluateProgress();
        resolveEntities();
        markSent();
    }

    private void loadMemory() {
        class_2487 stored = GuiMemory.get(player, h.guiId);
        if (stored == null) {
            return;
        }
        boolean orphans = false;
        loading = true;
        try {
            class_2487 slots = stored.method_10562("slots");
            for (String key : slots.method_10541()) {
                class_1799 stack = class_1799.method_7915(slots.method_10562(key));
                if (stack.method_7960()) {
                    continue;
                }
                List<Integer> idx = model.resolveSlots(key);
                GuiModel.SlotDef def = idx.size() == 1 ? model.slots().get(idx.get(0)) : null;
                if (def != null && def.id().equals(key) && !def.isDisplay()) {
                    h.inventory.method_5447(def.index(), stack);
                } else {
                    // the slot no longer exists (pack changed): give the item back instead of losing it
                    player.method_31548().method_7398(stack);
                    orphans = true;
                }
            }
            class_2487 sliders = stored.method_10562("sliders");
            for (String key : sliders.method_10541()) {
                int e = model.sliderElement(key);
                if (e >= 0 && model.element(e) instanceof GuiElement.Slider s && sliders.method_10573(key, 99)) {
                    h.sliders[e] = s.clamp(sliders.method_10574(key));
                }
            }
        } finally {
            loading = false;
        }
        if (orphans) {
            GuiMemory.save(player, h);
        }
    }

    /** First half of closing: stops every held button (runs its action_on_release) while the GUI is still open. */
    void releaseAll() {
        closing = true;
        for (int e = 0; e < n; e++) {
            if (held[e]) {
                release(e);
            }
        }
    }

    /** Second half of closing: remembers the GUI ({@code memory}) or gives its items back. */
    void finishClose() {
        closing = true;
        if (config.memory()) {
            GuiMemory.save(player, h);
        } else {
            returnItems();
        }
    }

    private void returnItems() {
        for (GuiModel.SlotDef def : model.slots()) {
            if (def.isDisplay()) {
                continue;
            }
            class_1799 stack = h.inventory.method_5441(def.index());
            if (stack.method_7960()) {
                continue;
            }
            if (player.method_5805() && !player.method_14239()) {
                player.method_31548().method_7398(stack);
            } else {
                player.method_7328(stack, false);
            }
        }
    }

    /** Called once per server tick while the GUI is the player's open menu. */
    public void tick() {
        if (h.isClosed() || closing) {
            return;
        }
        ticks++;
        if (ticks % REFRESH_INTERVAL == 0) {
            if (!powerStillActive()) {
                player.method_7346();
                return;
            }
            evaluateVisibility();
            resolveEntities();
        }
        for (int e = 0; e < n; e++) {
            if (!held[e]) {
                continue;
            }
            GuiElement.Button b = (GuiElement.Button) model.element(e);
            if (!h.isVisible(e) || !passes(b.common().condition()) || !passes(b.pressCondition())) {
                release(e);
            } else {
                run(b.whilePressed(), -1);
            }
            if (h.isClosed()) {
                return;
            }
        }
        for (int e = 0; e < n; e++) {
            if (!Double.isNaN(pendingSlider[e]) || pendingRelease[e]) {
                applySlider(e);
                if (h.isClosed()) {
                    return;
                }
            }
            if (cooldown[e] > 0) {
                cooldown[e]--;
            }
        }
        if (model.hasProgress()) {
            evaluateProgress();
        }
        flush();
        if (config.memory() && ++sinceSave >= MEMORY_SAFETY_SAVE) {
            sinceSave = 0;
            GuiMemory.save(player, h);
        }
    }

    private boolean powerStillActive() {
        BridgedPower<GuiConfig> power = GuiPowers.find(player, h.powerId);
        try {
            return power != null && power.isActive();
        } catch (RuntimeException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------- client input (already on the server thread)

    public void pressButton(int e) {
        if (h.isClosed() || closing || !(model.element(e) instanceof GuiElement.Button b) || held[e]) {
            return;
        }
        long now = player.method_37908().method_8510();
        if (lastPress[e] == now || cooldown[e] > 0) {
            return;     // at most one press per button per tick, and cooldown
        }
        if (!h.isVisible(e) || !passes(b.common().condition()) || !passes(b.pressCondition())) {
            return;
        }
        lastPress[e] = now;
        cooldown[e] = b.cooldown();
        held[e] = true;
        run(b.onPress(), -1);
    }

    public void releaseButton(int e) {
        if (h.isClosed() || e < 0 || e >= n || !held[e]) {
            return;
        }
        release(e);
    }

    private void release(int e) {
        held[e] = false;
        if (model.element(e) instanceof GuiElement.Button b) {
            run(b.onRelease(), -1);
        }
    }

    /** Stores the newest value; it is applied on the next tick (so at most one update per slider per tick). */
    public void moveSlider(int e, double value, boolean released) {
        if (h.isClosed() || closing || !(model.element(e) instanceof GuiElement.Slider s)) {
            return;
        }
        if (!Double.isNaN(value) && !Double.isInfinite(value)) {
            pendingSlider[e] = s.clamp(value);
        }
        if (released) {
            pendingRelease[e] = true;
        }
    }

    private void applySlider(int e) {
        GuiElement.Slider s = (GuiElement.Slider) model.element(e);
        double value = pendingSlider[e];
        boolean released = pendingRelease[e];
        pendingSlider[e] = Double.NaN;
        pendingRelease[e] = false;
        boolean allowed = h.isVisible(e) && passes(s.common().condition()) && passes(s.slideCondition());
        if (!Double.isNaN(value)) {
            if (allowed && value != h.sliders[e]) {
                h.sliders[e] = value;
                run(s.onSlide(), e);
                if (h.isClosed()) {
                    return;
                }
                if (config.memory()) {
                    GuiMemory.save(player, h);
                }
            } else if (!allowed) {
                sentSliders[e] = Double.NaN;    // force the server value back to the client
            }
        }
        if (released && allowed) {
            run(s.onRelease(), e);
        }
    }

    // ---------------------------------------------------------------- evaluation

    private boolean passes(Optional<EntityCondition> condition) {
        if (condition.isEmpty()) {
            return true;
        }
        try {
            return condition.get().test(EntityCtx.of(player, player.method_37908()));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void evaluateVisibility() {
        if (!model.hasConditions()) {
            return;
        }
        for (int e = 0; e < n; e++) {
            h.visible.set(e, passes(model.element(e).common().condition()));
        }
    }

    private void evaluateProgress() {
        for (int e = 0; e < n; e++) {
            if (model.element(e) instanceof GuiElement.Image img && img.progress().isPresent()) {
                double v;
                try {
                    v = img.progress().get().eval(player);
                } catch (RuntimeException ex) {
                    v = 0;
                }
                h.progress[e] = (float) Math.max(0.0, Math.min(1.0, v));
            }
        }
    }

    private void resolveEntities() {
        if (!model.hasServerEntities()) {
            return;
        }
        for (int e = 0; e < n; e++) {
            if (!(model.element(e) instanceof GuiElement.EntityView view) || !view.resolvedOnServer() || !h.isVisible(e)) {
                continue;
            }
            int[] ids;
            try {
                class_2300 selector = new class_2303(new StringReader(view.selector().get())).method_9882();
                List<? extends class_1297> found = selector.method_9816(player.method_5671().method_9206(2).method_9217());
                ids = found.stream().limit(MAX_ENTITY_IDS).mapToInt(class_1297::method_5628).toArray();
            } catch (Exception ex) {
                ids = new int[0];
            }
            h.entityIds[e] = ids;
        }
    }

    private void markSent() {
        sentVisible.clear();
        sentVisible.or(h.visible);
        System.arraycopy(h.progress, 0, sentProgress, 0, n);
        System.arraycopy(h.sliders, 0, sentSliders, 0, n);
        for (int e = 0; e < n; e++) {
            sentEntities[e] = h.entityIds[e];
        }
    }

    /** Sends whatever changed since the last packet. */
    private void flush() {
        GuiNetwork.Builder delta = new GuiNetwork.Builder();
        boolean any = false;
        if (!sentVisible.equals(h.visible)) {
            delta.visible(h.visible);
            sentVisible.clear();
            sentVisible.or(h.visible);
            any = true;
        }
        for (int e = 0; e < n; e++) {
            if (Math.abs(h.progress[e] - sentProgress[e]) > 1.0e-4f || (h.progress[e] != sentProgress[e] && (h.progress[e] == 0f || h.progress[e] == 1f))) {
                delta.progress(e, h.progress[e]);
                sentProgress[e] = h.progress[e];
                any = true;
            }
            if (!Double.isNaN(h.sliders[e]) && Double.compare(h.sliders[e], sentSliders[e]) != 0) {
                delta.slider(e, h.sliders[e]);
                sentSliders[e] = h.sliders[e];
                any = true;
            }
            if (!Arrays.equals(h.entityIds[e], sentEntities[e])) {
                delta.entities(e, h.entityIds[e]);
                sentEntities[e] = h.entityIds[e];
                any = true;
            }
        }
        if (any) {
            GuiNetwork.sendState(player, h.field_7763, delta.build());
        }
    }

    private void run(Optional<EntityAction> action, int sliderElement) {
        if (action.isEmpty()) {
            return;
        }
        int previous = h.contextSlider;
        h.contextSlider = sliderElement;
        boolean attributed = PowerCause.push(player, h.powerId);
        try {
            action.get().run(EntityCtx.of(player, player.method_37908()));
        } catch (RuntimeException ex) {
            AEA.LOGGER.error("[AEA] An action of aea:gui {} failed", h.powerId, ex);
        } finally {
            if (attributed) {
                PowerCause.pop();
            }
            h.contextSlider = previous;
        }
    }
}
