package dev.alphasucks.aea.gui.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.gui.GuiSlot;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_1735;
import net.minecraft.class_4587;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * AEA GUI: slots of an {@code aea:gui} element with {@code gui_scale_lock} are drawn scaled around their
 * top-left corner, and the hover/click area matches the drawn area. Other slots are untouched.
 * The wrappers are cosmetic, so they are optional ({@code require = 0}): if another mod removed a target,
 * only the scaling of locked slots is lost instead of the game failing to start.
 */
@Mixin(class_465.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected int field_2776;
    @Shadow
    protected int field_2800;
    @Shadow
    protected class_1735 field_2787;

    @Unique
    private static float aea$scaleOf(class_1735 slot) {
        return slot instanceof GuiSlot gs ? gs.drawScale : 1f;
    }

    @WrapOperation(
        method = "method_25394(Lnet/minecraft/class_332;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_465;method_2385(Lnet/minecraft/class_332;Lnet/minecraft/class_1735;)V"),
        require = 0
    )
    private void aea$drawScaledSlot(class_465<?> self, class_332 context, class_1735 slot, Operation<Void> original) {
        float s = aea$scaleOf(slot);
        if (s == 1f) {
            original.call(self, context, slot);
            return;
        }
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(slot.field_7873, slot.field_7872, 0f);
        matrices.method_22905(s, s, 1f);
        matrices.method_46416(-slot.field_7873, -slot.field_7872, 0f);
        try {
            original.call(self, context, slot);
        } finally {
            matrices.method_22909();
        }
    }

    @WrapOperation(
        method = "method_25394(Lnet/minecraft/class_332;IIF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_465;method_33285(Lnet/minecraft/class_332;III)V"),
        require = 0
    )
    private void aea$drawScaledHighlight(class_332 context, int x, int y, int z, Operation<Void> original) {
        class_1735 slot = this.field_2787;
        float s = slot == null ? 1f : aea$scaleOf(slot);
        if (s == 1f) {
            original.call(context, x, y, z);
            return;
        }
        class_4587 matrices = context.method_51448();
        matrices.method_22903();
        matrices.method_46416(x, y, 0f);
        matrices.method_22905(s, s, 1f);
        matrices.method_46416(-x, -y, 0f);
        try {
            original.call(context, x, y, z);
        } finally {
            matrices.method_22909();
        }
    }

    @WrapMethod(method = "method_2387(Lnet/minecraft/class_1735;DD)Z", require = 0)
    private boolean aea$scaledSlotHitbox(class_1735 slot, double mouseX, double mouseY, Operation<Boolean> original) {
        float s = aea$scaleOf(slot);
        if (s == 1f) {
            return original.call(slot, mouseX, mouseY);
        }
        double px = mouseX - this.field_2776;
        double py = mouseY - this.field_2800;
        return px >= slot.field_7873 - s && px < slot.field_7873 + 17f * s && py >= slot.field_7872 - s && py < slot.field_7872 + 17f * s;
    }
}
