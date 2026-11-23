package dev.alphasucks.aea.gui.client;

import dev.alphasucks.aea.gui.GuiConfig;
import dev.alphasucks.aea.gui.GuiElement;
import dev.alphasucks.aea.gui.GuiModel;
import java.util.function.ToIntFunction;

/**
 * Canvas layout of a GUI for the current window (client only; recomputed on every screen init).
 * <p>
 * Every element has a pivot: the point on the element matching its anchor (e.g. the bottom-right corner
 * for {@code bottom_right}). The pivot is placed at the anchor point of the canvas plus {@code (x, y)}.
 * With {@code gui_scale_lock} the element is scaled around its pivot.
 * Without a fixed size (no {@code width}/{@code height} and no vanilla background) the canvas is the
 * bounding box of all elements (all anchors then coincide), and the player inventory goes below it.
 */
final class GuiLayout {
    static final int INVENTORY_WIDTH = 162;
    static final int INVENTORY_HEIGHT = 76;

    /** Unscaled box (x, y, w, h), pivot (px, py) and draw scale of one element, canvas-relative. */
    static final class Box {
        int x;
        int y;
        int w;
        int h;
        float px;
        float py;
        float scale = 1f;

        boolean scaled() {
            return scale != 1f;
        }

        /** Mouse (canvas-relative) to unscaled element space. */
        double ux(double mx) {
            return px + (mx - px) / scale;
        }

        double uy(double my) {
            return py + (my - py) / scale;
        }

        boolean contains(double mx, double my) {
            double x0 = ux(mx);
            double y0 = uy(my);
            return x0 >= x && x0 < x + w && y0 >= y && y0 < y + h;
        }
    }

    int width;
    int height;
    Box[] boxes;
    int inventoryX;
    int inventoryY;

    interface Scales {
        float of(int lock);
    }

    static GuiLayout compute(GuiConfig config, ToIntFunction<GuiElement> textWidth, Scales scales) {
        GuiModel model = config.model();
        int n = model.elementCount();
        GuiLayout out = new GuiLayout();
        out.boxes = new Box[n];
        boolean bg = config.showVanillaBackground();
        int fixedW = config.width().orElse(bg ? 176 : -1);
        int fixedH = config.height().orElse(bg ? 166 : -1);
        int w0 = Math.max(fixedW, 0);
        int h0 = Math.max(fixedH, 0);
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int e = 0; e < n; e++) {
            GuiElement el = model.element(e);
            GuiElement.Common c = el.common();
            Box b = new Box();
            int[] size = size(el, model, e, textWidth);
            b.w = size[0];
            b.h = size[1];
            b.scale = c.guiScaleLock().isPresent() ? scales.of(c.guiScaleLock().get()) : 1f;
            GuiElement.Anchor a = c.anchor();
            int ax = (int) Math.floor(a.fx * w0) + c.x();
            int ay = (int) Math.floor(a.fy * h0) + c.y();
            b.px = ax;
            b.py = ay;
            b.x = ax - (int) Math.floor(a.fx * b.w);
            b.y = ay - (int) Math.floor(a.fy * b.h);
            out.boxes[e] = b;
            float sx0 = b.px - a.fx * b.w * b.scale;
            float sy0 = b.py - a.fy * b.h * b.scale;
            minX = Math.min(minX, sx0);
            minY = Math.min(minY, sy0);
            maxX = Math.max(maxX, sx0 + b.w * b.scale);
            maxY = Math.max(maxY, sy0 + b.h * b.scale);
        }
        if (n == 0) {
            minX = minY = maxX = maxY = 0;
        }
        if (config.showInventory()) {
            out.inventoryX = fixedW >= 0 ? (w0 - INVENTORY_WIDTH) / 2 + config.inventoryX()
                : Math.round((minX + maxX) / 2f) - INVENTORY_WIDTH / 2 + config.inventoryX();
            out.inventoryY = fixedH >= 0 ? h0 - 83 + config.inventoryY()
                : Math.round(maxY) + (n == 0 ? 0 : 4) + config.inventoryY();
            minX = Math.min(minX, out.inventoryX);
            minY = Math.min(minY, out.inventoryY);
            maxX = Math.max(maxX, out.inventoryX + INVENTORY_WIDTH);
            maxY = Math.max(maxY, out.inventoryY + INVENTORY_HEIGHT);
        }
        int shiftX = fixedW >= 0 ? 0 : -(int) Math.floor(minX);
        int shiftY = fixedH >= 0 ? 0 : -(int) Math.floor(minY);
        out.width = fixedW >= 0 ? fixedW : Math.max(1, (int) Math.ceil(maxX) + shiftX);
        out.height = fixedH >= 0 ? fixedH : Math.max(1, (int) Math.ceil(maxY) + shiftY);
        for (Box b : out.boxes) {
            b.x += shiftX;
            b.y += shiftY;
            b.px += shiftX;
            b.py += shiftY;
        }
        out.inventoryX += shiftX;
        out.inventoryY += shiftY;
        return out;
    }

    private static int[] size(GuiElement el, GuiModel model, int index, ToIntFunction<GuiElement> textWidth) {
        GuiElement.Common c = el.common();
        if (el instanceof GuiElement.Image img) {
            return new int[]{img.width(), img.height()};
        }
        if (el instanceof GuiElement.Text) {
            return new int[]{c.width().orElseGet(() -> textWidth.applyAsInt(el)), c.height().orElse(9)};
        }
        if (el instanceof GuiElement.Button b) {
            return new int[]{b.width(), b.height()};
        }
        if (el instanceof GuiElement.Slider s) {
            return new int[]{s.width(), s.height()};
        }
        if (el instanceof GuiElement.EntityView v) {
            return new int[]{v.width(), v.height()};
        }
        if (el instanceof GuiElement.SlotGrid g) {
            GuiModel.Grid grid = model.gridOfElement(index);
            int cols = grid == null ? g.columns() : grid.shownCols();
            int rows = grid == null ? g.rows() : grid.shownRows();
            return new int[]{cols * g.cellSize(), rows * g.cellSize()};
        }
        return new int[]{18, 18};
    }
}
