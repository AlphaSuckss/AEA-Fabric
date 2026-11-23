package dev.alphasucks.aea.gui;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/**
 * Per-player storage of {@code "memory": true} GUIs (Fabric data attachment, saved with the player and
 * copied on death). Layout: {@code { "<gui id>": { "slots": { "<slot id>": <item> }, "sliders": { "<slider id>": <double> } } }}.
 * Only the server touches it.
 */
public final class GuiMemory {
    public static final AttachmentType<class_2487> TYPE = AttachmentRegistry.<class_2487>builder()
        .persistent(class_2487.field_25128)
        .copyOnDeath()
        .buildAndRegister(new class_2960("aea", "gui_memory"));

    private GuiMemory() {
    }

    public static void bootstrap() {
        // class load registers the attachment
    }

    @Nullable
    public static class_2487 get(class_1657 player, class_2960 guiId) {
        class_2487 root = ((AttachmentTarget) player).getAttached(TYPE);
        if (root == null || !root.method_10573(guiId.toString(), 10)) {
            return null;
        }
        return root.method_10562(guiId.toString());
    }

    /** Stored slider value, or NaN. */
    public static double slider(class_1657 player, class_2960 guiId, String sliderId) {
        class_2487 gui = get(player, guiId);
        if (gui == null) {
            return Double.NaN;
        }
        class_2487 sliders = gui.method_10562("sliders");
        return sliders.method_10573(sliderId, 99) ? sliders.method_10574(sliderId) : Double.NaN;
    }

    /** Writes the current slots and sliders of an open GUI (replaces that GUI's entry). */
    public static void save(class_1657 player, GuiScreenHandler h) {
        if (h.model == null) {
            return;
        }
        class_2487 gui = new class_2487();
        class_2487 slots = new class_2487();
        for (GuiModel.SlotDef def : h.model.slots()) {
            if (def.isDisplay()) {
                continue;
            }
            class_1799 stack = h.inventory.method_5438(def.index());
            if (!stack.method_7960()) {
                slots.method_10566(def.id(), stack.method_7953(new class_2487()));
            }
        }
        class_2487 sliders = new class_2487();
        h.model.sliders().forEach((id, element) -> {
            if (!Double.isNaN(h.sliders[element])) {
                sliders.method_10549(id, h.sliders[element]);
            }
        });
        gui.method_10566("slots", slots);
        gui.method_10566("sliders", sliders);
        AttachmentTarget target = (AttachmentTarget) player;
        class_2487 root = target.getAttached(TYPE);
        class_2487 next = root == null ? new class_2487() : root.method_10553();
        next.method_10566(h.guiId.toString(), gui);
        target.setAttached(TYPE, next);
    }
}
