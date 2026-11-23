package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.effect.MilkImmunityPower;
import java.util.Map;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code aea:milk_immunity}. Milk clears effects from inside the item's "finished using" step, so effects are only
 * kept when they are cleared while the entity is finishing eating or drinking something. That covers the vanilla milk
 * bucket and modded milk the same way, and leaves {@code /effect clear} alone. The kept effects are taken out before
 * the clear and put back after it; they were never removed, so nothing is sent to players and no attribute changes.
 */
@Mixin(class_1309.class)
public abstract class LivingEntityMilkMixin {
    @Shadow
    @Final
    private Map<class_1291, class_1293> field_6280;

    @Unique
    private int aea$consuming;

    @WrapMethod(method = "method_6040")
    private void aea$markConsuming(Operation<Void> original) {
        aea$consuming++;
        try {
            original.call();
        } finally {
            aea$consuming--;
        }
    }

    @WrapMethod(method = "method_6012")
    private boolean aea$keepMilkImmuneEffects(Operation<Boolean> original) {
        class_1309 self = (class_1309) (Object) this;
        if (aea$consuming <= 0 || self.method_37908().field_9236) {
            return original.call();
        }
        Map<class_1291, class_1293> kept = MilkImmunityPower.protectedEffects(self, field_6280);
        if (kept.isEmpty()) {
            return original.call();
        }
        field_6280.keySet().removeAll(kept.keySet());
        try {
            return original.call();
        } finally {
            field_6280.putAll(kept);
        }
    }
}
