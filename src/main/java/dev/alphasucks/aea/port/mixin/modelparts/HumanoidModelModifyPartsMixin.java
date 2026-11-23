package dev.alphasucks.aea.port.mixin.modelparts;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.access.PartVisibilityUndo;
import dev.alphasucks.aea.port.client.render.AnimationPlayer;
import dev.alphasucks.aea.port.client.render.ForcedPoseShapes;
import dev.alphasucks.aea.port.client.render.HandRenderPass;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.client.render.ModelPartEdits;
import dev.alphasucks.aea.port.client.render.ModelPartLookup;
import dev.alphasucks.aea.port.compat.entitymodelfeatures.EntityModelFeaturesCompat;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.data.FirstPersonFrame;
import dev.alphasucks.aea.port.data.HumanoidPose;
import dev.alphasucks.aea.port.data.ModelAnimation;
import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.PoseMath;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import dev.alphasucks.aea.port.data.ModelPartTransformation.Type;
import dev.alphasucks.aea.port.power.builtin.ModifyPlayerModelPower;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_572.class)
@Environment(EnvType.CLIENT)
public abstract class HumanoidModelModifyPartsMixin implements PartVisibilityUndo {
   private static final String SETUP_ANIM = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V";
   @Unique
   private final Map<class_630, float[]> apoli$originals = new IdentityHashMap();
   @Unique
   private final List<class_630> apoli$scratch = new ArrayList(2);
   @Unique
   private final float[] apoli$limbs = new float[54];
   @Unique
   private final float[] apoli$pose = new float[9];
   @Unique
   private final float[] apoli$pivot = new float[3];
   @Unique
   private final double[] apoli$vector = new double[3];
   @Unique
   private final float[] apoli$end = new float[3];
   @Unique
   private final float[] apoli$body = new float[9];
   @Unique
   private final double[] apoli$matrix = new double[18];
   @Unique
   private ModelAnimation apoli$modelAnimation;
   @Unique
   private boolean apoli$hadPower;
   @Unique
   private boolean apoli$poseOverridden;
   @Unique
   private float apoli$savedSwimAmount;
   @Unique
   private boolean apoli$savedCrouching;
   @Unique
   private boolean apoli$savedRiding;
   @Unique
   private boolean apoli$forcedCrouch;
   @Unique
   private class_630[] apoli$visibilityParts = new class_630[4];
   @Unique
   private byte[] apoli$visibilityStates = new byte[4];
   @Unique
   private int apoli$visibilityCount;

   @Inject(method = "method_17087(Lnet/minecraft/class_1309;FFFFF)V", at = @At("HEAD"))
   private void apoli$modifyPartsHead(class_1309 entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
      if (this.apoli$visibilityCount > 0) {
         this.apoli$undoPartVisibility();
      }

      this.apoli$modelAnimation = (Object)this instanceof class_591 ? ModifyPlayerModelPower.firstActiveAnimations(entity) : null;
      boolean has = !ModelPartAnimator.update(entity).isEmpty() || this.apoli$modelAnimation != null;
      if (has) {
         if (this.apoli$originals.isEmpty()) {
            this.apoli$snapshot();
         }

         this.apoli$restore();
      } else if (this.apoli$hadPower) {
         this.apoli$restore();
         this.apoli$originals.clear();
      }

      this.apoli$hadPower = has;
      this.apoli$poseOverridden = ModelPartAnimator.overridesPose(entity);
      if (this.apoli$poseOverridden) {
         class_572<?> model = (class_572<?>)(Object)this;
         this.apoli$savedSwimAmount = model.field_3396;
         this.apoli$savedCrouching = model.field_3400;
         this.apoli$savedRiding = model.field_3449;
         model.field_3396 = 0.0F;
         model.field_3400 = false;
         model.field_3449 = false;
      }

      class_572<?> humanoid = (class_572<?>)(Object)this;
      this.apoli$forcedCrouch = !this.apoli$poseOverridden
         && !humanoid.field_3400
         && !HandRenderPass.active()
         && EntityPoses.forced(entity) == class_4050.field_18081;
      if (this.apoli$forcedCrouch) {
         humanoid.field_3400 = true;
      }
   }

   @ModifyVariable(method = "method_17087(Lnet/minecraft/class_1309;FFFFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
   private float apoli$holdStill(float limbSwingAmount, @Local(argsOnly = true) class_1309 entity) {
      class_4050 pose = EntityPoses.forced(entity);
      return pose != null && EntityPoses.holdsStill(pose) && !ModelPartAnimator.overridesPose(entity) ? 0.0F : limbSwingAmount;
   }

   @ModifyExpressionValue(
      method = "method_17087(Lnet/minecraft/class_1309;FFFFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_6003()I")
   )
   private int apoli$neutralizeFallFlying(int original, @Local(argsOnly = true) class_1309 entity) {
      return this.apoli$poseOverridden ? 0 : EntityPoses.fallFlyingTicks(entity, original);
   }

   @Inject(method = "method_17087(Lnet/minecraft/class_1309;FFFFF)V", at = @At("TAIL"))
   private void apoli$modifyPartsTail(class_1309 entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
      if (this.apoli$forcedCrouch) {
         ((class_572)(Object)this).field_3400 = false;
         this.apoli$forcedCrouch = false;
      }

      if ((Object)this instanceof class_591) {
         class_4050 forced = EntityPoses.forced(entity);
         if (forced != null && !this.apoli$poseOverridden && EntityPoses.shapes(forced) && !HandRenderPass.active()) {
            ForcedPoseShapes.apply((class_572<?>)(Object)this, entity, forced, h);
         }
      }

      List<Slot> slots = ModelPartAnimator.update(entity);
      if (!slots.isEmpty()) {
         class_572<?> model = (class_572<?>)(Object)this;
         boolean handPass = HandRenderPass.active();
         boolean handEdited = false;

         for (int s = 0; s < slots.size(); s++) {
            Slot slot = (Slot)slots.get(s);
            if (!(slot.weight() <= 0.0F) && slot.rendersIn(handPass)) {
               if (handPass && !handEdited) {
                  handEdited = true;
                  model.field_3401.field_3654 = 0.0F;
                  model.field_27433.field_3654 = 0.0F;
                  HandRenderPass.markEdited();
               }

               ModelPartTransformation transformation = slot.transformation();
               BodyPart part = transformation.bodyPart().sided(entity);
               this.apoli$scratch.clear();
               ModelPartLookup.resolveInto(model, part, this.apoli$scratch);
               if (transformation.type() == Type.REACH) {
                  this.apoli$reach(model, slot, handPass);
               } else if (part.isGroup() && PoseMath.isSpatial(transformation.type())) {
                  this.apoli$applyGroup(model, part, slot);
               } else {
                  boolean visibility = ModelPartEdits.changesVisibility(transformation.type());
                  boolean hidesCustom = visibility && ModelPartEdits.hides(slot) && EntityModelFeaturesCompat.hasCustomParts();

                  for (int p = 0; p < this.apoli$scratch.size(); p++) {
                     class_630 member = (class_630)this.apoli$scratch.get(p);
                     if (visibility) {
                        this.apoli$rememberVisibility(member);
                     }

                     ModelPartEdits.apply(member, slot, (float[])this.apoli$originals.get(member));
                     if (hidesCustom) {
                        this.apoli$hideCustomChildren(member);
                     }
                  }
               }
            }
         }

         this.apoli$scratch.clear();
      }

      if (this.apoli$modelAnimation != null) {
         AnimationPlayer.apply(
            (class_572<?>)(Object)this, entity, this.apoli$modelAnimation, ModifyPlayerModelPower.CANONICAL, class_3532.method_15363(h - entity.field_6012, 0.0F, 1.0F)
         );
         this.apoli$modelAnimation = null;
      }

      if (this.apoli$poseOverridden) {
         class_572<?> model = (class_572<?>)(Object)this;
         model.field_3396 = this.apoli$savedSwimAmount;
         model.field_3400 = this.apoli$savedCrouching;
         model.field_3449 = this.apoli$savedRiding;
         this.apoli$poseOverridden = false;
      }
   }

   @Unique
   private void apoli$applyGroup(class_572<?> model, BodyPart part, Slot slot) {
      ModelPartTransformation transformation = slot.transformation();
      ModelPartLookup.limbPosesInto(model, this.apoli$limbs);
      HumanoidPose.groupPivot(part, transformation, this.apoli$limbs, this.apoli$vector, this.apoli$pivot);

      for (int p = 0; p < this.apoli$scratch.size(); p++) {
         class_630 member = (class_630)this.apoli$scratch.get(p);
         ModelPartLookup.read(member, this.apoli$pose, 0);
         PoseMath.applyGroup(
            transformation.type(),
            this.apoli$pose,
            0,
            slot.value(),
            slot.weight(),
            transformation.overrideAnimation(),
            this.apoli$pivot[0],
            this.apoli$pivot[1],
            this.apoli$pivot[2]
         );
         ModelPartLookup.write(this.apoli$pose, 0, member);
      }

      this.apoli$scratch.clear();
   }

   @Unique
   private void apoli$reach(class_572<?> model, Slot slot, boolean handPass) {
      if (slot.hasTarget()) {
         float blend = class_3532.method_15363(slot.value(), 0.0F, 1.0F) * slot.weight();

         for (int p = 0; p < this.apoli$scratch.size(); p++) {
            class_630 member = (class_630)this.apoli$scratch.get(p);
            if (ModelPartEdits.endPoint(member, this.apoli$end)) {
               ModelPartLookup.read(member, this.apoli$pose, 0);
               int side = handPass ? apoli$armSide(model, member) : 0;
               if (side != 0) {
                  FirstPersonFrame.reach(
                     side > 0,
                     this.apoli$pose,
                     0,
                     this.apoli$end[0],
                     this.apoli$end[1],
                     this.apoli$end[2],
                     slot.targetX(),
                     slot.targetY(),
                     slot.targetZ(),
                     blend,
                     this.apoli$body,
                     this.apoli$matrix
                  );
               } else {
                  PoseMath.reach(
                     this.apoli$pose, 0, this.apoli$end[0], this.apoli$end[1], this.apoli$end[2], slot.targetX(), slot.targetY(), slot.targetZ(), blend
                  );
               }

               ModelPartLookup.write(this.apoli$pose, 0, member);
            }
         }
      }

      this.apoli$scratch.clear();
   }

   @Unique
   private static int apoli$armSide(class_572<?> model, class_630 part) {
      if (part == model.field_3401) {
         return 1;
      }

      if (part == model.field_27433) {
         return -1;
      }

      if (model instanceof class_591<?> player) {
         if (part == player.field_3486) {
            return 1;
         }

         if (part == player.field_3484) {
            return -1;
         }
      }

      return 0;
   }

   @Unique
   private void apoli$snapshot() {
      this.apoli$scratch.clear();
      ModelPartLookup.allPartsInto((class_572<?>)(Object)this, this.apoli$scratch);

      for (class_630 part : this.apoli$scratch) {
         this.apoli$originals.put(part, ModelPartEdits.snapshot(part));
      }

      this.apoli$scratch.clear();
   }

   @Unique
   private void apoli$restore() {
      for (Entry<class_630, float[]> entry : this.apoli$originals.entrySet()) {
         ModelPartEdits.restorePose((class_630)entry.getKey(), (float[])entry.getValue());
      }
   }

   @Unique
   private void apoli$rememberVisibility(class_630 part) {
      for (int i = 0; i < this.apoli$visibilityCount; i++) {
         if (this.apoli$visibilityParts[i] == part) {
            return;
         }
      }

      if (this.apoli$visibilityCount == this.apoli$visibilityParts.length) {
         this.apoli$visibilityParts = (class_630[])Arrays.copyOf(this.apoli$visibilityParts, this.apoli$visibilityCount * 2);
         this.apoli$visibilityStates = Arrays.copyOf(this.apoli$visibilityStates, this.apoli$visibilityCount * 2);
      }

      this.apoli$visibilityParts[this.apoli$visibilityCount] = part;
      this.apoli$visibilityStates[this.apoli$visibilityCount] = (byte)((part.field_3665 ? 1 : 0) | (part.field_38456 ? 2 : 0));
      this.apoli$visibilityCount++;
   }

   @Unique
   private void apoli$hideCustomChildren(class_630 part) {
      class_630[] custom = EntityModelFeaturesCompat.customChildren(part);

      for (int i = 0; i < custom.length; i++) {
         this.apoli$rememberVisibility(custom[i]);
         custom[i].field_3665 = false;
      }
   }

   @Override
   public void apoli$undoPartVisibility() {
      for (int i = this.apoli$visibilityCount - 1; i >= 0; i--) {
         class_630 part = this.apoli$visibilityParts[i];
         byte state = this.apoli$visibilityStates[i];
         part.field_3665 = (state & 1) != 0;
         part.field_38456 = (state & 2) != 0;
         this.apoli$visibilityParts[i] = null;
      }

      this.apoli$visibilityCount = 0;
   }
}
