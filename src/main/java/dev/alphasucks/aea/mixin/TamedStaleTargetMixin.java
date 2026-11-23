package dev.alphasucks.aea.mixin;

import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_3218;
import net.minecraft.class_5354;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tamed pets (Beastmaster wolves especially, since they hunt in packs) could keep a target or
 * anger that pointed at a mob someone else killed. Vanilla's anger tick only drops anger when the
 * angry-at entity is still loaded, so once the corpse was removed the pets stayed locked onto
 * nothing and crowded onto the same spot until they crammed each other to death.
 * Every server mob tick, a tamed pet whose target/anger points at a dead or missing entity lets go.
 */
@Mixin(class_1308.class)
public abstract class TamedStaleTargetMixin {
    @Inject(method = "method_5958", at = @At("HEAD"))
    private void aea$dropStaleTarget(CallbackInfo ci) {
        class_1308 self = (class_1308) (Object) this;
        if (!(self instanceof class_1321 tamed) || !tamed.method_6181() || !(self.method_37908() instanceof class_3218 world)) {
            return;
        }

        boolean stale = false;
        class_1309 target = self.method_5968();
        if (target != null && !target.method_5805()) {
            self.method_5980(null);
            stale = true;
        }

        if (self instanceof class_5354 angerable && angerable.method_29507() > 0) {
            UUID angryAt = angerable.method_29508();
            if (angryAt != null) {
                class_1297 subject = world.method_14190(angryAt);
                if (subject == null || !subject.method_5805()) {
                    angerable.method_29922();
                    stale = true;
                }
            } else if (target != null && !target.method_5805()) {
                angerable.method_29922();
            }
        }

        if (stale) {
            self.method_5942().method_6340();
        }
    }
}
