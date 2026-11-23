package dev.alphasucks.aea.port.mixin.ghost;

import dev.alphasucks.aea.port.block.GhostBlocks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.class_1927;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1927.class)
public abstract class ExplosionGhostMixin {
   @Shadow
   @Final
   private ObjectArrayList<class_2338> field_9188;
   @Shadow
   @Final
   private class_1937 field_9187;

   @Inject(method = "method_8348", at = @At("RETURN"))
   private void apoli$spareGhostBlocks(CallbackInfo ci) {
      if (!this.field_9188.isEmpty()) {
         this.field_9188.removeIf(pos -> GhostBlocks.isGhost(this.field_9187, pos));
      }
   }
}
