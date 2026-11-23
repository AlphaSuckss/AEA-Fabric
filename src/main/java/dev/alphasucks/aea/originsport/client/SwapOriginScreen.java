package dev.alphasucks.aea.originsport.client;

import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

import java.util.ArrayList;
import java.util.List;

/**
 * Overgrown's swap screen: a grid of the main origin plus every origin the player can swap into the
 * target layer; clicking one asks the server to swap. AEA: a standalone screen (Overgrown's extends
 * its own OriginDisplayScreen, which apace doesn't have), drawing each origin's icon item.
 */
@Environment(EnvType.CLIENT)
public final class SwapOriginScreen extends class_437 {
    private static final int COLUMNS = 4;
    private static final int CELL = 26;
    private static final int PITCH = 34;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 182;
    private static final int GRID_LEFT = 18;
    private static final int GRID_TOP = 30;
    private static final int GRID_BOTTOM = 164;

    private final class_2960 targetLayerId;
    /** entries[0] is the main origin (or apace's empty origin when the layer has none). */
    private final List<class_2960> entries = new ArrayList<>();
    private class_2960 activeOrigin;
    private int guiLeft;
    private int guiTop;
    private int scroll;

    public SwapOriginScreen(class_2960 targetLayerId) {
        super(class_2561.method_43471("origins.screen.swap_origin"));
        this.targetLayerId = targetLayerId;
    }

    public void refresh() {
        rebuild();
        clampScroll();
    }

    private void rebuild() {
        entries.clear();
        class_1657 player = class_310.method_1551().field_1724;
        if (player == null) {
            return;
        }
        class_2960 main = ApaceOrigins.mainOrigin(player, targetLayerId);
        entries.add(main == null ? ApaceOrigins.EMPTY_ID : main);
        entries.addAll(SwapManager.pool(player, targetLayerId, main));
        class_2960 swapped = SwapManager.activeSwap(player, targetLayerId);
        activeOrigin = swapped != null ? swapped : entries.get(0);
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        guiLeft = (field_22789 - PANEL_W) / 2;
        guiTop = (field_22790 - PANEL_H) / 2;
        rebuild();
        clampScroll();
        method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), b -> method_25419())
            .method_46434(guiLeft + PANEL_W / 2 - 50, guiTop + PANEL_H + 5, 100, 20)
            .method_46431());
    }

    private int rows() {
        return (entries.size() + COLUMNS - 1) / COLUMNS;
    }

    private int maxScroll() {
        int height = rows() <= 0 ? 0 : (rows() - 1) * PITCH + CELL;
        return Math.max(0, height - (GRID_BOTTOM - GRID_TOP));
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
    }

    private int cellX(int index) {
        return guiLeft + GRID_LEFT + (index % COLUMNS) * PITCH;
    }

    private int cellY(int index) {
        return guiTop + GRID_TOP + (index / COLUMNS) * PITCH - scroll;
    }

    private boolean visible(int y) {
        return y + CELL >= guiTop + GRID_TOP && y <= guiTop + GRID_BOTTOM;
    }

    private int indexAt(double mouseX, double mouseY) {
        if (mouseY < guiTop + GRID_TOP || mouseY > guiTop + GRID_BOTTOM) {
            return -1;
        }
        for (int i = 0; i < entries.size(); i++) {
            int x = cellX(i);
            int y = cellY(i);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                return i;
            }
        }
        return -1;
    }

    private void select(int index) {
        if (index < 0 || index >= entries.size()) {
            return;
        }
        if (ClientPlayNetworking.canSend(OriginsPortNetwork.SWAP_SELECT)) {
            class_2540 buf = PacketByteBufs.create();
            buf.method_10812(targetLayerId);
            buf.writeBoolean(index == 0);
            buf.method_10812(entries.get(index));
            ClientPlayNetworking.send(OriginsPortNetwork.SWAP_SELECT, buf);
        }
        method_25419();
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int index = indexAt(mouseX, mouseY);
            if (index >= 0) {
                select(index);
                return true;
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double amount) {
        scroll -= (int) Math.round(amount * PITCH / 2.0);
        clampScroll();
        return true;
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    private class_2561 titleText() {
        OriginLayer layer = ApaceOrigins.layer(targetLayerId);
        return layer == null
            ? class_2561.method_43471("origins.gui.swap_origin.title.generic")
            : class_2561.method_43469("origins.gui.swap_origin.title", class_2561.method_43471(layer.getTranslationKey()));
    }

    @Override
    public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
        method_25420(graphics);
        graphics.method_25294(guiLeft, guiTop, guiLeft + PANEL_W, guiTop + PANEL_H, 0xC0101010);
        graphics.method_49601(guiLeft, guiTop, PANEL_W, PANEL_H, 0xFF5A5A5A);
        graphics.method_27534(field_22793, titleText(), field_22789 / 2, guiTop + 10, 0xFFFFFF);
        int hovered = indexAt(mouseX, mouseY);
        if (entries.size() <= 1) {
            graphics.method_27534(field_22793, class_2561.method_43471("origins.gui.swap_origin.empty"), field_22789 / 2, guiTop + GRID_TOP + CELL + 12, 0xAAAAAA);
        }
        graphics.method_44379(guiLeft + 8, guiTop + GRID_TOP, guiLeft + PANEL_W - 8, guiTop + GRID_BOTTOM);
        for (int i = 0; i < entries.size(); i++) {
            int x = cellX(i);
            int y = cellY(i);
            if (!visible(y)) {
                continue;
            }
            class_2960 id = entries.get(i);
            boolean selected = id.equals(activeOrigin);
            graphics.method_25294(x, y, x + CELL, y + CELL, i == hovered ? 0x60FFFFFF : 0x40000000);
            graphics.method_49601(x, y, CELL, CELL, selected ? 0xFFFFFFFF : 0xFF3F3F3F);
            Origin origin = ApaceOrigins.origin(id);
            class_1799 icon = origin == null ? class_1799.field_8037 : origin.getDisplayItem();
            if (icon != null && !icon.method_7960()) {
                graphics.method_51427(icon, x + 5, y + 5);
            }
        }
        graphics.method_44380();
        int max = maxScroll();
        if (max > 0) {
            int trackTop = guiTop + GRID_TOP;
            int trackHeight = GRID_BOTTOM - GRID_TOP;
            int knob = Math.max(12, trackHeight * trackHeight / (trackHeight + max));
            int knobTop = trackTop + (trackHeight - knob) * scroll / max;
            int x = guiLeft + PANEL_W - 7;
            graphics.method_25294(x, trackTop, x + 3, trackTop + trackHeight, 0x40FFFFFF);
            graphics.method_25294(x, knobTop, x + 3, knobTop + knob, 0xC0FFFFFF);
        }
        super.method_25394(graphics, mouseX, mouseY, partialTick);
        if (hovered >= 0) {
            class_2960 id = entries.get(hovered);
            class_2561 name = hovered == 0 && ApaceOrigins.isEmpty(id)
                ? class_2561.method_43471("origins.gui.swap_origin.main")
                : ApaceOrigins.name(id);
            graphics.method_51438(field_22793, name, mouseX, mouseY);
        }
    }
}
