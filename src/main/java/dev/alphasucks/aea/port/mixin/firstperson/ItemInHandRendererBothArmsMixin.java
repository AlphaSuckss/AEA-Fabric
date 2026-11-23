package dev.alphasucks.aea.port.mixin.firstperson;

import dev.alphasucks.aea.port.client.render.ArmEdits;
import dev.alphasucks.aea.port.client.render.FirstPersonHeldItem;
import dev.alphasucks.aea.port.client.render.GhostArmState;
import dev.alphasucks.aea.port.power.builtin.ShowBothArmsPower;
import dev.alphasucks.aea.port.power.builtin.ShowBothArmsPower.Arms;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_742;
import net.minecraft.class_759;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_759.class)
@Environment(EnvType.CLIENT)
public abstract class ItemInHandRendererBothArmsMixin {
   @Unique
   private Arms apoli$arms;

   @Shadow
   protected abstract void method_3219(class_4587 var1, class_4597 var2, int var3, float var4, float var5, class_1306 var6);

   @Inject(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7960()Z", ordinal = 0), cancellable = true)
   private void apoli$showBothArms(
      class_742 player,
      float partialTick,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 pose,
      class_4597 buffers,
      int light,
      CallbackInfo ci
   ) {
      if (!player.method_5767() && !stack.method_31574(class_1802.field_8204)) {
         if (this.apoli$arms == null) {
            this.apoli$arms = new Arms();
         }

         boolean main = hand == class_1268.field_5808;
         class_1306 arm = ArmEdits.armFor(player, hand);
         boolean powered = ShowBothArmsPower.resolve(player, this.apoli$arms);
         int edits = ArmEdits.of(player, true);
         boolean edited = ArmEdits.edited(edits, arm);
         if (powered || edited) {
            boolean drawnByVanilla = main && stack.method_7960();
            boolean extraArm = !drawnByVanilla
               && (edited || (main ? !stack.method_7960() : stack.method_7960()) && (this.apoli$arms.mask & (main ? 1 : 2)) != 0);
            if (drawnByVanilla || extraArm) {
               this.apoli$renderGhostArms(pose, buffers, light, equippedProgress, swingProgress, arm);
               if (extraArm) {
                  boolean attach = ArmEdits.reaching(edits, arm) && FirstPersonHeldItem.attaches(player, hand, stack);
                  if (attach) {
                     FirstPersonHeldItem.begin((class_759)(Object)this, stack, arm);
                  }

                  pose.method_22903();
                  this.method_3219(pose, buffers, light, equippedProgress, swingProgress, arm);
                  pose.method_22909();
                  if (attach && FirstPersonHeldItem.end()) {
                     pose.method_22909();
                     ci.cancel();
                  }
               }
            }
         }
      }
   }

   @Unique
   private void apoli$renderGhostArms(class_4587 pose, class_4597 buffers, int light, float equippedProgress, float swingProgress, class_1306 arm) {
      if (this.apoli$arms.ghostsIn(true) && !(swingProgress <= 0.0F)) {
         int ghosts = this.apoli$arms.ghosts;
         float spacing = this.apoli$arms.spacing;
         if (!(spacing <= 0.0F)) {
            GhostArmState.begin(this.apoli$arms.alpha);

            try {
               for (int i = ghosts; i >= 1; i--) {
                  float lag = spacing * i;
                  if (!(lag >= 1.0F)) {
                     float progress = swingProgress - lag;
                     if (progress <= 0.0F) {
                        progress++;
                     }

                     pose.method_22903();
                     this.method_3219(pose, buffers, light, equippedProgress, progress, arm);
                     pose.method_22909();
                  }
               }
            } finally {
               GhostArmState.end();
            }
         }
      }
   }
}
