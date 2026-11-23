package dev.alphasucks.aea.port.mixin.pose;

import dev.alphasucks.aea.port.access.ModifiedPoseHolder;
import dev.alphasucks.aea.port.data.ArmPoseReference;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import dev.alphasucks.aea.port.power.builtin.PosePower.Config;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class EntityPoseMixin implements ModifiedPoseHolder {
   @Unique
   private class_4050 apoli$modifiedEntityPose;
   @Unique
   private ArmPoseReference apoli$modifiedArmPoseMain;
   @Unique
   private ArmPoseReference apoli$modifiedArmPoseOff;
   @Unique
   private class_4050 apoli$previousEntityPose;
   @Unique
   private int apoli$poseTicks;

   @Override
   public class_4050 apoli$getModifiedEntityPose() {
      return this.apoli$modifiedEntityPose;
   }

   @Override
   public void apoli$setModifiedEntityPose(class_4050 entityPose) {
      this.apoli$modifiedEntityPose = entityPose;
   }

   @Override
   public ArmPoseReference apoli$getModifiedArmPose(class_1268 hand) {
      return hand == class_1268.field_5808 ? this.apoli$modifiedArmPoseMain : this.apoli$modifiedArmPoseOff;
   }

   @Override
   public void apoli$setModifiedArmPose(ArmPoseReference mainHand, ArmPoseReference offHand) {
      this.apoli$modifiedArmPoseMain = mainHand;
      this.apoli$modifiedArmPoseOff = offHand;
   }

   @Override
   public class_4050 apoli$getPreviousEntityPose() {
      return this.apoli$previousEntityPose;
   }

   @Override
   public void apoli$setPreviousEntityPose(class_4050 entityPose) {
      this.apoli$previousEntityPose = entityPose;
   }

   @Override
   public int apoli$getPoseTicks() {
      return this.apoli$poseTicks;
   }

   @Inject(method = "method_5773", at = @At("TAIL"))
   private void apoli$applyPose(CallbackInfo ci) {
      class_1297 self = (class_1297)(Object)this;
      class_4050 before = this.apoli$modifiedEntityPose;
      Config chosen = PosePower.chooseBest(self);
      if (chosen != null) {
         if (!(self instanceof class_1657) && this.apoli$previousEntityPose == null && chosen.entityPose().isPresent()) {
            this.apoli$previousEntityPose = self.method_18376();
         }

         this.apoli$modifiedEntityPose = (class_4050)chosen.entityPose().orElse(null);
         ArmPoseReference arm = (ArmPoseReference)chosen.armPose().orElse(null);
         if (arm != null) {
            this.apoli$modifiedArmPoseMain = chosen.hands().contains(Hand.MAIN_HAND) ? arm : null;
            this.apoli$modifiedArmPoseOff = chosen.hands().contains(Hand.OFF_HAND) ? arm : null;
         } else {
            this.apoli$modifiedArmPoseMain = null;
            this.apoli$modifiedArmPoseOff = null;
         }

         chosen.entityPose().ifPresent(self::method_18380);
      } else {
         this.apoli$modifiedEntityPose = null;
         this.apoli$modifiedArmPoseMain = null;
         this.apoli$modifiedArmPoseOff = null;
         if (!(self instanceof class_1657) && this.apoli$previousEntityPose != null) {
            self.method_18380(this.apoli$previousEntityPose);
            this.apoli$previousEntityPose = null;
         }
      }

      if (this.apoli$modifiedEntityPose != before) {
         this.apoli$poseTicks = 0;
         if (self instanceof class_1657 && (EntityPoses.resizesPlayer(before) || EntityPoses.resizesPlayer(this.apoli$modifiedEntityPose))) {
            self.method_18382();
         }
      } else if (this.apoli$modifiedEntityPose != null && this.apoli$poseTicks < 1200) {
         this.apoli$poseTicks++;
      }
   }
}
