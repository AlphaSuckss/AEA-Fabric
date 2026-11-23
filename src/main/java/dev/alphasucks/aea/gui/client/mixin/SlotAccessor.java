package dev.alphasucks.aea.gui.client.mixin;

import net.minecraft.class_1735;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** AEA GUI: slot positions depend on the window (anchors, gui_scale_lock), so the screen moves its slots. */
@Mixin(class_1735.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("field_7873")
    void aea$setX(int x);

    @Mutable
    @Accessor("field_7872")
    void aea$setY(int y);
}
