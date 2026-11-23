package dev.alphasucks.aea.gui.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;

/** Drawing helpers of the AEA GUI screen. */
final class GuiDraw {
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_FILL = 0xFF8B8B8B;

    private GuiDraw() {
    }

    /** Vanilla-style 18x18 slot background around the 16x16 item position (x, y), scaled around (x, y). */
    static void slotFrame(class_332 context, int x, int y, float scale) {
        class_4587 m = context.method_51448();
        boolean scaled = scale != 1f;
        if (scaled) {
            m.method_22903();
            m.method_46416(x, y, 0f);
            m.method_22905(scale, scale, 1f);
            m.method_46416(-x, -y, 0f);
        }
        context.method_25294(x - 1, y - 1, x + 17, y + 17, SLOT_FILL);
        context.method_25294(x - 1, y - 1, x + 16, y, SLOT_DARK);
        context.method_25294(x - 1, y, x, y + 16, SLOT_DARK);
        context.method_25294(x, y + 16, x + 17, y + 17, SLOT_LIGHT);
        context.method_25294(x + 16, y, x + 17, y + 16, SLOT_LIGHT);
        if (scaled) {
            m.method_22909();
        }
    }

    /** The whole texture stretched over the box. */
    static void stretched(class_332 context, class_2960 texture, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.method_25293(texture, x, y, w, h, 0f, 0f, w, h, w, h);
        RenderSystem.disableBlend();
    }

    /** Vanilla 200x20 widget sprite (button/slider) as a nine-slice, like 1.20.1's own widgets. */
    static void nineSlice(class_332 context, class_2960 texture, int x, int y, int w, int h, int v) {
        if (w <= 0 || h <= 0) {
            return;
        }
        context.method_51422(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        context.method_49697(texture, x, y, w, h, 20, 4, 200, 20, 0, v);
        RenderSystem.disableBlend();
    }

    /**
     * Part of a texture at 1:1 scale. {@code progress} < 1 draws only the filled part: left to right, or
     * bottom to top when {@code vertical} (like a furnace flame).
     */
    static void image(class_332 context, class_2960 texture, int x, int y, int w, int h, int u, int v, int texW, int texH,
                      float progress, boolean vertical) {
        float p = Math.max(0f, Math.min(1f, progress));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (!vertical) {
            int pw = Math.round(w * p);
            if (pw > 0) {
                context.method_25293(texture, x, y, pw, h, u, v, pw, h, texW, texH);
            }
        } else {
            int ph = Math.round(h * p);
            if (ph > 0) {
                context.method_25293(texture, x, y + h - ph, w, ph, u, v + h - ph, w, ph, texW, texH);
            }
        }
        RenderSystem.disableBlend();
    }
}
