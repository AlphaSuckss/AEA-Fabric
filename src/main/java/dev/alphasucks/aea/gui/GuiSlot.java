package dev.alphasucks.aea.gui;

import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1799;

/**
 * A slot of an {@code aea:gui}. Every check reads the menu's own state, so on the server the session
 * (not the client) decides visibility, and hidden/display slots refuse every insert and take.
 */
public class GuiSlot extends class_1735 {
    public final GuiScreenHandler menu;
    public final GuiModel.SlotDef def;
    /** Client only: drawn size factor ({@code gui_scale_lock}); x/y are the drawn top-left. */
    public float drawScale = 1f;

    public GuiSlot(GuiScreenHandler menu, GuiModel.SlotDef def, class_1263 inventory) {
        super(inventory, def.index(), 0, 0);
        this.menu = menu;
        this.def = def;
    }

    public boolean visible() {
        return menu.isVisible(def.element());
    }

    /** Whether the item condition and type allow this stack (ignores visibility and fullness). */
    public boolean accepts(class_1799 stack) {
        if (!def.acceptsInsert()) {
            return false;
        }
        if (def.itemCondition().isEmpty()) {
            return true;
        }
        try {
            ItemCondition cond = def.itemCondition().get();
            return cond.test(new ItemCtx(stack, menu.player.method_37908(), menu.player));
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean method_7680(class_1799 stack) {
        return !menu.isClosed() && visible() && accepts(stack);
    }

    @Override
    public boolean method_7674(class_1657 player) {
        return !menu.isClosed() && visible() && !def.isDisplay();
    }

    @Override
    public int method_7675() {
        return def.maxStack().isPresent() ? def.maxStack().get() : super.method_7675();
    }

    @Override
    public boolean method_7682() {
        return visible();
    }

    /** A placeholder used when the client cannot build the GUI the server opened (it closes right away). */
    public static final class Dead extends class_1735 {
        public Dead(class_1263 inventory, int index) {
            super(inventory, index, -10000, -10000);
        }

        @Override
        public boolean method_7680(class_1799 stack) {
            return false;
        }

        @Override
        public boolean method_7674(class_1657 player) {
            return false;
        }

        @Override
        public boolean method_7682() {
            return false;
        }
    }
}
