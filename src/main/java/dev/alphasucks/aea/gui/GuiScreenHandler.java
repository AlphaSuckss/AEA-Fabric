package dev.alphasucks.aea.gui;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import net.minecraft.class_1277;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

/**
 * The menu of an open {@code aea:gui}. One instance per player and side: the server instance owns a
 * {@link GuiSession} (all game logic), the client instance only mirrors the state the server sends.
 * <p>
 * Slot order: the GUI's slots (see {@link GuiModel#slots()}), then - with {@code show_inventory} - the
 * player's 27 main slots and 9 hotbar slots.
 */
public final class GuiScreenHandler extends class_1703 {
    public final class_2960 powerId;
    public final class_2960 guiId;
    @Nullable
    public final GuiConfig config;
    @Nullable
    public final GuiModel model;
    public final class_1657 player;
    public final class_1277 inventory;
    public final int guiSlotCount;
    /** Index of the first player-inventory slot, or -1. */
    public final int playerSlotStart;
    /** Per element: shown (condition passed on the server). */
    public final BitSet visible = new BitSet();
    /** Per element: last progress value (0..1) of image elements. */
    public final float[] progress;
    /** Per element: slider value (NaN for other elements). */
    public final double[] sliders;
    /** Per element: entity network ids resolved on the server for {@code selector} entity elements. */
    public final int[][] entityIds;
    /** True on the client when the GUI could not be built (config missing or different); the screen closes itself. */
    public final boolean broken;
    @Nullable
    GuiSession session;
    private boolean closed;
    private boolean closing;
    /** Server: element index of the slider whose actions are running ({@code {value}}), or -1. */
    int contextSlider = -1;

    private GuiScreenHandler(int syncId, class_1661 playerInventory, class_2960 powerId, class_2960 guiId, @Nullable GuiConfig config, int expectedSlots) {
        super(AeaGui.MENU, syncId);
        this.powerId = powerId;
        this.guiId = guiId;
        this.player = playerInventory.field_7546;
        GuiModel m = config == null ? null : config.model();
        int own = m == null ? 0 : m.slotCount();
        int total = config == null ? 0 : own + (config.showInventory() ? 36 : 0);
        boolean ok = m != null && (expectedSlots < 0 || expectedSlots == total);
        this.broken = !ok;
        if (!ok) {
            int n = Math.max(0, Math.min(expectedSlots, 4096));
            this.config = null;
            this.model = null;
            this.inventory = new class_1277(n);
            for (int i = 0; i < n; i++) {
                this.method_7621(new GuiSlot.Dead(inventory, i));
            }
            this.guiSlotCount = n;
            this.playerSlotStart = -1;
            this.progress = new float[0];
            this.sliders = new double[0];
            this.entityIds = new int[0][];
            return;
        }
        this.config = config;
        this.model = m;
        this.inventory = new class_1277(own);
        for (GuiModel.SlotDef def : m.slots()) {
            this.method_7621(new GuiSlot(this, def, inventory));
            if (def.isDisplay() && def.displayItem() != null) {
                inventory.method_5447(def.index(), def.displayItem().method_7972());
            }
        }
        this.guiSlotCount = own;
        if (config.showInventory()) {
            this.playerSlotStart = own;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.method_7621(new class_1735(playerInventory, col + row * 9 + 9, 0, 0));
                }
            }
            for (int col = 0; col < 9; col++) {
                this.method_7621(new class_1735(playerInventory, col, 0, 0));
            }
        } else {
            this.playerSlotStart = -1;
        }
        int n = m.elementCount();
        this.progress = new float[n];
        this.sliders = new double[n];
        this.entityIds = new int[n][];
        Arrays.fill(sliders, Double.NaN);
        for (int e = 0; e < n; e++) {
            entityIds[e] = new int[0];
            if (m.element(e) instanceof GuiElement.Slider s) {
                sliders[e] = s.start();
            }
        }
        visible.set(0, n);
    }

    /** Server side: builds the menu and its session (memory is loaded, conditions are evaluated). */
    public static GuiScreenHandler createServer(int syncId, class_1661 inventory, class_3222 player, class_2960 powerId, class_2960 guiId, GuiConfig config) {
        GuiScreenHandler handler = new GuiScreenHandler(syncId, inventory, powerId, guiId, config, -1);
        handler.session = new GuiSession(handler, player);
        GuiPowers.beginOpening(handler);
        try {
            handler.session.onOpen();
        } finally {
            GuiPowers.endOpening();
        }
        return handler;
    }

    /** Client side: rebuilds the menu from the open payload (see {@link GuiNetwork#writeOpenData}). */
    public static GuiScreenHandler createClient(int syncId, class_1661 inventory, class_2540 buf) {
        class_2960 powerId = buf.method_10810();
        class_2960 guiId = buf.method_10810();
        int totalSlots = buf.method_10816();
        GuiConfig config = GuiPowers.config(inventory.field_7546, powerId);
        GuiScreenHandler handler = new GuiScreenHandler(syncId, inventory, powerId, guiId, config, totalSlots);
        GuiNetwork.State state = GuiNetwork.State.read(buf);
        if (!handler.broken) {
            state.applyTo(handler);
        }
        return handler;
    }

    /** Server: element index of the slider whose actions are running right now, or -1. */
    public int contextSlider() {
        return contextSlider;
    }

    public boolean isClient() {
        return session == null;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isVisible(int element) {
        return visible.get(element);
    }

    @Nullable
    public GuiSession session() {
        return session;
    }

    @Nullable
    public GuiSlot guiSlot(int index) {
        return index >= 0 && index < guiSlotCount && this.field_7761.get(index) instanceof GuiSlot s ? s : null;
    }

    /** GUI slots named by a slot id or a grid id. */
    public List<GuiSlot> resolveSlots(String id) {
        if (model == null) {
            return List.of();
        }
        List<Integer> indices = model.resolveSlots(id);
        GuiSlot[] out = new GuiSlot[indices.size()];
        int k = 0;
        for (int index : indices) {
            GuiSlot s = guiSlot(index);
            if (s != null) {
                out[k++] = s;
            }
        }
        return List.of(Arrays.copyOf(out, k));
    }

    /** Current value of a slider by id, or NaN. */
    public double sliderValue(String id) {
        if (model == null) {
            return Double.NaN;
        }
        int e = model.sliderElement(id);
        return e < 0 ? Double.NaN : sliders[e];
    }

    // ---------------------------------------------------------------- vanilla menu behaviour

    @Override
    public boolean method_7597(class_1657 player) {
        return !closed && player.method_5805();
    }

    @Override
    public void method_7593(int slotIndex, int button, class_1713 actionType, class_1657 player) {
        if (closed || broken) {
            return;
        }
        if (slotIndex >= 0 && slotIndex < guiSlotCount) {
            GuiSlot slot = guiSlot(slotIndex);
            // hidden and display slots are never interactable, whatever the client sends
            if (slot == null || !slot.visible() || slot.def.isDisplay()) {
                return;
            }
        }
        super.method_7593(slotIndex, button, actionType, player);
    }

    @Override
    public boolean method_7613(class_1799 stack, class_1735 slot) {
        if (slot instanceof GuiSlot s && (s.def.isDisplay() || !s.visible())) {
            return false;
        }
        return super.method_7613(stack, slot);
    }

    @Override
    public class_1799 method_7601(class_1657 player, int index) {
        if (closed || broken || index < 0 || index >= this.field_7761.size()) {
            return class_1799.field_8037;
        }
        class_1735 slot = this.field_7761.get(index);
        if (!slot.method_7681()) {
            return class_1799.field_8037;
        }
        class_1799 stack = slot.method_7677();
        class_1799 original = stack.method_7972();
        if (index < guiSlotCount) {
            GuiSlot gs = guiSlot(index);
            if (gs == null || !gs.method_7674(player)) {
                return class_1799.field_8037;
            }
            if (playerSlotStart >= 0) {
                if (!this.method_7616(stack, playerSlotStart, playerSlotStart + 36, true)) {
                    return class_1799.field_8037;
                }
            } else {
                player.method_31548().method_7394(stack);
            }
        } else {
            if (!insertIntoGui(stack)) {
                return class_1799.field_8037;
            }
        }
        if (stack.method_7960()) {
            slot.method_48931(class_1799.field_8037);
        } else {
            slot.method_7668();
        }
        if (stack.method_7947() == original.method_7947()) {
            return class_1799.field_8037;
        }
        slot.method_7667(player, stack);
        return original;
    }

    /** Shift-click into the GUI: merge into matching stacks first, then empty slots, in slot order. */
    private boolean insertIntoGui(class_1799 stack) {
        int before = stack.method_7947();
        for (int pass = 0; pass < 2 && !stack.method_7960(); pass++) {
            for (int i = 0; i < guiSlotCount && !stack.method_7960(); i++) {
                GuiSlot slot = guiSlot(i);
                if (slot == null || !slot.visible() || !slot.def.acceptsInsert()) {
                    continue;
                }
                boolean empty = !slot.method_7681();
                if ((pass == 0) == empty) {
                    continue;
                }
                if (!empty && !class_1799.method_31577(slot.method_7677(), stack)) {
                    continue;
                }
                if (!slot.method_7680(stack)) {
                    continue;
                }
                slot.method_32756(stack);
            }
        }
        return stack.method_7947() != before;
    }

    /**
     * Server: held buttons are released first (their {@code action_on_release} can still read and change the
     * GUI), then the GUI is closed and its items are remembered or returned. Re-entrant calls are ignored.
     */
    @Override
    public void method_7595(class_1657 player) {
        if (closed || closing) {
            return;
        }
        closing = true;
        try {
            if (session != null) {
                session.releaseAll();
            }
        } finally {
            closed = true;
            super.method_7595(player);
            if (session != null) {
                session.finishClose();
            }
        }
    }
}
