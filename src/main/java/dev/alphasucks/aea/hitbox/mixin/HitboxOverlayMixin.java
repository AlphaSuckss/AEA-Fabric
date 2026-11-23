package dev.alphasucks.aea.hitbox.mixin;

import dev.alphasucks.aea.hitbox.HitboxPart;
import dev.alphasucks.aea.hitbox.client.ClientHitboxParts;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_761;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** With hit boxes shown (F3 + B), an entity's {@code aea:hitbox_part} boxes are outlined in green, like dragon parts. */
@Mixin(class_898.class)
public abstract class HitboxOverlayMixin {
    @Inject(method = "method_3956", at = @At("TAIL"))
    private static void aea$drawHitboxParts(class_4587 matrices, class_4588 vertices, class_1297 entity, float tickDelta, CallbackInfo ci) {
        if (!(entity instanceof class_1309)) {
            return;
        }
        HitboxPart[] parts = ClientHitboxParts.partsOf(entity.method_5628());
        if (parts.length == 0) {
            return;
        }
        double ex = class_3532.method_16436(tickDelta, entity.field_6038, entity.method_23317());
        double ey = class_3532.method_16436(tickDelta, entity.field_5971, entity.method_23318());
        double ez = class_3532.method_16436(tickDelta, entity.field_5989, entity.method_23321());
        for (HitboxPart part : parts) {
            if (part.owner != entity) {
                continue;
            }
            matrices.method_22903();
            matrices.method_22904(
                class_3532.method_16436(tickDelta, part.field_6038, part.method_23317()) - ex,
                class_3532.method_16436(tickDelta, part.field_5971, part.method_23318()) - ey,
                class_3532.method_16436(tickDelta, part.field_5989, part.method_23321()) - ez);
            class_761.method_22982(matrices, vertices,
                part.method_5829().method_989(-part.method_23317(), -part.method_23318(), -part.method_23321()), 0.25F, 1.0F, 0.0F, 1.0F);
            matrices.method_22909();
        }
    }
}
