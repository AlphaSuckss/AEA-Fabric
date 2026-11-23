package dev.alphasucks.aea.gui;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.power.PowerType;

/**
 * {@code aea:gui}: a data-driven custom screen. The power does nothing by itself; {@code aea:open_gui}
 * opens it (usually from an {@code apoli:active_self} key power).
 */
public final class GuiPowerType extends PowerType<GuiConfig> {
    @Override
    public MapCodec<GuiConfig> configCodec() {
        return GuiConfig.CODEC;
    }
}
