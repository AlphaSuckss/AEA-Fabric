package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.access.DualSwingHolder;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import dev.alphasucks.aea.port.data.ModelPartTransformation.Type;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_3532;
import net.minecraft.class_4050;

public final class HumanoidPose {
   public static final int HEAD = 0;
   public static final int BODY = 1;
   public static final int RIGHT_ARM = 2;
   public static final int LEFT_ARM = 3;
   public static final int RIGHT_LEG = 4;
   public static final int LEFT_LEG = 5;
   public static final int PART_COUNT = 6;
   public static final float ARM_LENGTH = 10.0F;
   public static final float LEG_LENGTH = 12.0F;
   private static final int STRIDE = 9;
   private static final int X = 0;
   private static final int Y = 1;
   private static final int Z = 2;
   private static final int X_ROT = 3;
   private static final int Y_ROT = 4;
   private static final int Z_ROT = 5;
   private static final int X_SCALE = 6;
   private static final int Y_SCALE = 7;
   private static final int Z_SCALE = 8;
   private static final float[] BOXES = new float[]{
      -4.0F,
      -8.0F,
      -4.0F,
      4.0F,
      0.0F,
      4.0F,
      -4.0F,
      0.0F,
      -2.0F,
      4.0F,
      12.0F,
      2.0F,
      -3.0F,
      -2.0F,
      -2.0F,
      1.0F,
      10.0F,
      2.0F,
      -1.0F,
      -2.0F,
      -2.0F,
      3.0F,
      10.0F,
      2.0F,
      -2.0F,
      0.0F,
      -2.0F,
      2.0F,
      12.0F,
      2.0F,
      -2.0F,
      0.0F,
      -2.0F,
      2.0F,
      12.0F,
      2.0F
   };
   private static final String[] NAMES = new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"};
   private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   private static final int POSE_EMPTY = 0;
   private static final int POSE_ITEM = 1;
   private static final int POSE_BLOCK = 2;
   private static final int POSE_BOW = 3;
   private static final int POSE_SPEAR = 4;
   private static final ModelPartTimeline TIMELINE = new ModelPartTimeline();
   public static final HumanoidPose REST = new HumanoidPose();
   private final float[] parts = new float[54];
   private float[] pivot;
   private double[] scratch;
   private float[] body;
   private double[] matrix;

   private HumanoidPose() {
      this.rest();
   }

   public static HumanoidPose of(class_1297 entity) {
      HumanoidPose pose = new HumanoidPose();
      if (entity instanceof class_1309 living) {
         pose.animate(living, TIMELINE, living.field_6012, 1.0F, living.field_6012);
         pose.applyPowers(living, TIMELINE, living.field_6012, false);
      }

      return pose;
   }

   public static HumanoidPose of(class_1297 entity, ModelPartTimeline timeline, double now, float partialTick) {
      HumanoidPose pose = new HumanoidPose();
      if (entity instanceof class_1309 living) {
         pose.animate(living, timeline, now, partialTick, living.field_6012 + partialTick);
         pose.applyPowers(living, timeline, now, false);
      }

      return pose;
   }

   public static HumanoidPose firstPersonArms(class_1309 entity, ModelPartTimeline timeline, double now) {
      HumanoidPose pose = new HumanoidPose();
      pose.bob(2, 0.0F, 1.0F);
      pose.bob(3, 0.0F, -1.0F);
      pose.applyPowers(entity, timeline, now, true);
      return pose;
   }

   public static float boxMin(int limb, int axis) {
      return BOXES[limb * 6 + axis];
   }

   public static float boxMax(int limb, int axis) {
      return BOXES[limb * 6 + 3 + axis];
   }

   public static String nameOf(int limb) {
      return NAMES[limb];
   }

   public float[] poses() {
      return this.parts;
   }

   public float x(int part) {
      return this.parts[part * 9 + 0];
   }

   public float y(int part) {
      return this.parts[part * 9 + 1];
   }

   public float z(int part) {
      return this.parts[part * 9 + 2];
   }

   public float xRot(int part) {
      return this.parts[part * 9 + 3];
   }

   public float yRot(int part) {
      return this.parts[part * 9 + 4];
   }

   public float zRot(int part) {
      return this.parts[part * 9 + 5];
   }

   public float yScale(int part) {
      return this.parts[part * 9 + 7];
   }

   private void rest() {
      for (int part = 0; part < 6; part++) {
         int base = part * 9;
         this.parts[base + 6] = 1.0F;
         this.parts[base + 7] = 1.0F;
         this.parts[base + 8] = 1.0F;
      }

      this.set(2, -5.0F, 2.0F, 0.0F);
      this.set(3, 5.0F, 2.0F, 0.0F);
      this.set(4, -1.9F, 12.0F, 0.0F);
      this.set(5, 1.9F, 12.0F, 0.0F);
   }

   private void set(int part, float x, float y, float z) {
      int base = part * 9;
      this.parts[base + 0] = x;
      this.parts[base + 1] = y;
      this.parts[base + 2] = z;
   }

   private float get(int part, int field) {
      return this.parts[part * 9 + field];
   }

   private void put(int part, int field, float value) {
      this.parts[part * 9 + field] = value;
   }

   private void add(int part, int field, float value) {
      this.parts[part * 9 + field] = this.parts[part * 9 + field] + value;
   }

   private void animate(class_1309 entity, ModelPartTimeline timeline, double now, float partialTick, float ageInTicks) {
      boolean overridden = timeline.overridesPose(entity, now);
      class_4050 forced = overridden ? null : EntityPoses.forced(entity);
      boolean fallFlying = !overridden && EntityPoses.fallFlyingTicks(entity, entity.method_6003()) > 4;
      boolean riding = !overridden && entity.method_5765();
      boolean crouching = !overridden && entity.method_18276();
      float swimAmount = overridden ? 0.0F : entity.method_6024(partialTick);
      boolean visuallySwimming = entity.method_20232();
      float netHeadYaw = class_3532.method_15393(
         class_3532.method_17821(partialTick, entity.field_6259, entity.field_6241)
            - class_3532.method_17821(partialTick, entity.field_6220, entity.field_6283)
      );
      float headPitch = class_3532.method_16439(partialTick, entity.field_6004, entity.method_36455());
      float attackTime = entity.method_6055(partialTick);
      float limbSwing = 0.0F;
      float limbSwingAmount = 0.0F;
      if (!entity.method_5765() && entity.method_5805()) {
         limbSwingAmount = Math.min(1.0F, entity.field_42108.method_48570(partialTick));
         limbSwing = entity.field_42108.method_48572(partialTick);
         if (entity.method_6109()) {
            limbSwing *= 3.0F;
         }
      }

      if (forced != null && EntityPoses.holdsStill(forced)) {
         limbSwingAmount = 0.0F;
      }

      this.put(0, 4, netHeadYaw * (float) (Math.PI / 180.0));
      if (fallFlying) {
         this.put(0, 3, (float) (-Math.PI / 4));
      } else if (swimAmount > 0.0F) {
         this.put(
            0,
            3,
            visuallySwimming
               ? rotlerpRad(swimAmount, this.get(0, 3), (float) (-Math.PI / 4))
               : rotlerpRad(swimAmount, this.get(0, 3), headPitch * (float) (Math.PI / 180.0))
         );
      } else {
         this.put(0, 3, headPitch * (float) (Math.PI / 180.0));
      }

      this.put(1, 4, 0.0F);
      this.put(2, 2, 0.0F);
      this.put(2, 0, -5.0F);
      this.put(3, 2, 0.0F);
      this.put(3, 0, 5.0F);
      float damping = 1.0F;
      if (fallFlying) {
         damping = (float)entity.method_18798().method_1027();
         damping /= 0.2F;
         damping *= damping * damping;
      }

      if (damping < 1.0F) {
         damping = 1.0F;
      }

      this.put(2, 3, class_3532.method_15362(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / damping);
      this.put(3, 3, class_3532.method_15362(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / damping);
      this.put(2, 5, 0.0F);
      this.put(3, 5, 0.0F);
      this.put(4, 3, class_3532.method_15362(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / damping);
      this.put(5, 3, class_3532.method_15362(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / damping);
      this.put(4, 4, 0.005F);
      this.put(5, 4, -0.005F);
      this.put(4, 5, 0.005F);
      this.put(5, 5, -0.005F);
      if (riding) {
         this.add(2, 3, (float) (-Math.PI / 5));
         this.add(3, 3, (float) (-Math.PI / 5));
         this.put(4, 3, -1.4137167F);
         this.put(4, 4, (float) (Math.PI / 10));
         this.put(4, 5, 0.07853982F);
         this.put(5, 3, -1.4137167F);
         this.put(5, 4, (float) (-Math.PI / 10));
         this.put(5, 5, -0.07853982F);
      }

      this.put(2, 4, 0.0F);
      this.put(3, 4, 0.0F);
      class_1306 mainArm = entity.method_6068();
      boolean mainIsRight = mainArm == class_1306.field_6183;
      int rightPose = armPose(entity, mainIsRight ? class_1268.field_5808 : class_1268.field_5810);
      int leftPose = armPose(entity, mainIsRight ? class_1268.field_5810 : class_1268.field_5808);
      if (entity.method_6115()) {
         if (entity.method_6058() == class_1268.field_5808 == mainIsRight) {
            this.poseArm(2, rightPose, true);
         } else {
            this.poseArm(3, leftPose, false);
         }
      } else {
         this.poseArm(2, rightPose, true);
         this.poseArm(3, leftPose, false);
      }

      this.attackAnimation(entity, attackTime, mainArm);
      if (crouching) {
         this.put(1, 3, 0.5F);
         this.add(2, 3, 0.4F);
         this.add(3, 3, 0.4F);
         this.put(4, 2, 4.0F);
         this.put(5, 2, 4.0F);
         this.put(4, 1, 12.2F);
         this.put(5, 1, 12.2F);
         this.put(0, 1, 4.2F);
         this.put(1, 1, 3.2F);
         this.put(3, 1, 5.2F);
         this.put(2, 1, 5.2F);
      } else {
         this.put(1, 3, 0.0F);
         this.put(4, 2, 0.0F);
         this.put(5, 2, 0.0F);
         this.put(4, 1, 12.0F);
         this.put(5, 1, 12.0F);
         this.put(0, 1, 0.0F);
         this.put(1, 1, 0.0F);
         this.put(3, 1, 2.0F);
         this.put(2, 1, 2.0F);
      }

      this.bob(2, ageInTicks, 1.0F);
      this.bob(3, ageInTicks, -1.0F);
      if (swimAmount > 0.0F) {
         this.swim(entity, limbSwing, swimAmount, attackTime, mainArm);
      }

      if (forced != null && EntityPoses.shapes(forced)) {
         EntityPoses.shape(forced, this.parts, EntityPoses.progress(entity, partialTick), ageInTicks);
      }
   }

   private void poseArm(int arm, int pose, boolean right) {
      switch (pose) {
         case 1:
            this.put(arm, 3, this.get(arm, 3) * 0.5F - (float) (Math.PI / 10));
            this.put(arm, 4, 0.0F);
            break;
         case 2:
            this.put(arm, 3, this.get(arm, 3) * 0.5F - 0.9424779F + class_3532.method_15363(this.get(0, 3), (float) (-Math.PI * 4.0 / 9.0), 0.43633232F));
            this.put(
               arm,
               4,
               (right ? -30.0F : 30.0F) * (float) (Math.PI / 180.0) + class_3532.method_15363(this.get(0, 4), (float) (-Math.PI / 6), (float) (Math.PI / 6))
            );
            break;
         case 3:
            this.put(2, 4, -0.1F + this.get(0, 4) + (right ? 0.0F : -0.4F));
            this.put(3, 4, 0.1F + this.get(0, 4) + (right ? 0.4F : 0.0F));
            this.put(2, 3, (float) (-Math.PI / 2) + this.get(0, 3));
            this.put(3, 3, (float) (-Math.PI / 2) + this.get(0, 3));
            break;
         case 4:
            this.put(arm, 3, this.get(arm, 3) * 0.5F - (float) Math.PI);
            this.put(arm, 4, 0.0F);
            break;
         default:
            this.put(arm, 4, 0.0F);
      }
   }

   private void attackAnimation(class_1309 entity, float attackTime, class_1306 mainArm) {
      if (!(attackTime <= 0.0F)) {
         class_1306 attacking = entity.field_6266 == class_1268.field_5808 ? mainArm : mainArm.method_5928();
         int arm = attacking == class_1306.field_6183 ? 2 : 3;
         float bodyYRot = class_3532.method_15374(class_3532.method_15355(attackTime) * (float) (Math.PI * 2)) * 0.2F;
         if (attacking == class_1306.field_6182) {
            bodyYRot *= -1.0F;
         }

         this.put(1, 4, bodyYRot);
         this.put(2, 2, class_3532.method_15374(bodyYRot) * 5.0F);
         this.put(2, 0, -class_3532.method_15362(bodyYRot) * 5.0F);
         this.put(3, 2, -class_3532.method_15374(bodyYRot) * 5.0F);
         this.put(3, 0, class_3532.method_15362(bodyYRot) * 5.0F);
         this.add(2, 4, bodyYRot);
         this.add(3, 4, bodyYRot);
         this.add(3, 3, bodyYRot);
         float eased = 1.0F - attackTime;
         eased *= eased;
         eased *= eased;
         eased = 1.0F - eased;
         float swing = class_3532.method_15374(eased * (float) Math.PI);
         float lean = class_3532.method_15374(attackTime * (float) Math.PI) * -(this.get(0, 3) - 0.7F) * 0.75F;
         this.add(arm, 3, -(swing * 1.2F + lean));
         this.add(arm, 4, bodyYRot * 2.0F);
         this.add(arm, 5, class_3532.method_15374(attackTime * (float) Math.PI) * -0.4F);
         if (DualSwingHolder.of(entity)) {
            int other = arm == 2 ? 3 : 2;
            this.add(other, 3, -(swing * 1.2F + lean));
            this.add(other, 4, bodyYRot * 2.0F);
            this.add(other, 5, class_3532.method_15374(attackTime * (float) Math.PI) * -0.4F);
         }
      }
   }

   private void swim(class_1309 entity, float limbSwing, float swimAmount, float attackTime, class_1306 mainArm) {
      float cycle = limbSwing % 26.0F;
      class_1306 attacking = entity.field_6266 == class_1268.field_5808 ? mainArm : mainArm.method_5928();
      float right = attacking == class_1306.field_6183 && attackTime > 0.0F ? 0.0F : swimAmount;
      float left = attacking == class_1306.field_6182 && attackTime > 0.0F ? 0.0F : swimAmount;
      if (!entity.method_6115()) {
         if (cycle < 14.0F) {
            this.put(3, 3, rotlerpRad(left, this.get(3, 3), 0.0F));
            this.put(2, 3, class_3532.method_16439(right, this.get(2, 3), 0.0F));
            this.put(3, 4, rotlerpRad(left, this.get(3, 4), (float) Math.PI));
            this.put(2, 4, class_3532.method_16439(right, this.get(2, 4), (float) Math.PI));
            this.put(3, 5, rotlerpRad(left, this.get(3, 5), (float) Math.PI + 1.8707964F * quadraticArmUpdate(cycle) / quadraticArmUpdate(14.0F)));
            this.put(2, 5, class_3532.method_16439(right, this.get(2, 5), (float) Math.PI - 1.8707964F * quadraticArmUpdate(cycle) / quadraticArmUpdate(14.0F)));
         } else if (cycle < 22.0F) {
            float t = (cycle - 14.0F) / 8.0F;
            this.put(3, 3, rotlerpRad(left, this.get(3, 3), (float) (Math.PI / 2) * t));
            this.put(2, 3, class_3532.method_16439(right, this.get(2, 3), (float) (Math.PI / 2) * t));
            this.put(3, 4, rotlerpRad(left, this.get(3, 4), (float) Math.PI));
            this.put(2, 4, class_3532.method_16439(right, this.get(2, 4), (float) Math.PI));
            this.put(3, 5, rotlerpRad(left, this.get(3, 5), 5.012389F - 1.8707964F * t));
            this.put(2, 5, class_3532.method_16439(right, this.get(2, 5), 1.2707963F + 1.8707964F * t));
         } else if (cycle < 26.0F) {
            float t = (cycle - 22.0F) / 4.0F;
            this.put(3, 3, rotlerpRad(left, this.get(3, 3), (float) (Math.PI / 2) - (float) (Math.PI / 2) * t));
            this.put(2, 3, class_3532.method_16439(right, this.get(2, 3), (float) (Math.PI / 2) - (float) (Math.PI / 2) * t));
            this.put(3, 4, rotlerpRad(left, this.get(3, 4), (float) Math.PI));
            this.put(2, 4, class_3532.method_16439(right, this.get(2, 4), (float) Math.PI));
            this.put(3, 5, rotlerpRad(left, this.get(3, 5), (float) Math.PI));
            this.put(2, 5, class_3532.method_16439(right, this.get(2, 5), (float) Math.PI));
         }
      }

      this.put(5, 3, class_3532.method_16439(swimAmount, this.get(5, 3), 0.3F * class_3532.method_15362(limbSwing * 0.33333334F + (float) Math.PI)));
      this.put(4, 3, class_3532.method_16439(swimAmount, this.get(4, 3), 0.3F * class_3532.method_15362(limbSwing * 0.33333334F)));
   }

   private void bob(int arm, float ageInTicks, float sign) {
      this.add(arm, 5, sign * (class_3532.method_15362(ageInTicks * 0.09F) * 0.05F + 0.05F));
      this.add(arm, 3, sign * class_3532.method_15374(ageInTicks * 0.067F) * 0.05F);
   }

   private void applyPowers(class_1309 entity, ModelPartTimeline timeline, double now, boolean firstPerson) {
      List<Slot> slots = timeline.update(entity, now);

      for (int i = 0; i < slots.size(); i++) {
         Slot slot = (Slot)slots.get(i);
         float weight = slot.weight();
         if (!(weight <= 0.0F) && slot.rendersIn(firstPerson)) {
            ModelPartTransformation transformation = slot.transformation();
            BodyPart bodyPart = transformation.bodyPart();
            if (transformation.type() == Type.REACH) {
               this.reachLimbs(bodyPart.sided(entity), slot, weight, firstPerson);
            } else if (bodyPart.isGroup()) {
               if (PoseMath.isSpatial(transformation.type())) {
                  this.applyGroup(bodyPart, transformation, slot.value(), weight);
               }
            } else {
               int part = indexOf(bodyPart);
               if (part >= 0) {
                  this.apply(part, transformation, slot.value(), weight);
               }
            }
         }
      }
   }

   private void applyGroup(BodyPart bodyPart, ModelPartTransformation transformation, float value, float weight) {
      if (this.pivot == null) {
         this.pivot = new float[3];
         this.scratch = new double[3];
      }

      groupPivot(bodyPart, transformation, this.parts, this.scratch, this.pivot);
      int limbs = bodyPart.limbs();
      boolean override = transformation.overrideAnimation();

      for (int limb = 0; limb < 6; limb++) {
         if ((limbs & 1 << limb) != 0) {
            PoseMath.applyGroup(transformation.type(), this.parts, limb * 9, value, weight, override, this.pivot[0], this.pivot[1], this.pivot[2]);
         }
      }
   }

   private void reachLimbs(BodyPart bodyPart, Slot slot, float weight, boolean firstPerson) {
      if (slot.hasTarget() && bodyPart.models() != 0) {
         float blend = class_3532.method_15363(slot.value(), 0.0F, 1.0F) * weight;
         int limbs = bodyPart.limbs();

         for (int limb = 0; limb < 6; limb++) {
            if ((limbs & 1 << limb) != 0) {
               int box = limb * 6;
               float endX = (BOXES[box] + BOXES[box + 3]) * 0.5F;
               float endY = BOXES[box + 4];
               float endZ = (BOXES[box + 2] + BOXES[box + 5]) * 0.5F;
               if (firstPerson && (limb == 2 || limb == 3)) {
                  if (this.body == null) {
                     this.body = new float[9];
                     this.matrix = new double[18];
                  }

                  FirstPersonFrame.reach(
                     limb == 2, this.parts, limb * 9, endX, endY, endZ, slot.targetX(), slot.targetY(), slot.targetZ(), blend, this.body, this.matrix
                  );
               } else {
                  PoseMath.reach(this.parts, limb * 9, endX, endY, endZ, slot.targetX(), slot.targetY(), slot.targetZ(), blend);
               }
            }
         }
      }
   }

   public static void groupPivot(BodyPart bodyPart, ModelPartTransformation transformation, float[] limbPoses, double[] scratch, float[] out) {
      if (transformation.pivot().isPresent()) {
         Vector explicit = (Vector)transformation.pivot().get();
         out[0] = explicit.x();
         out[1] = explicit.y();
         out[2] = explicit.z();
      } else {
         if (!bodyPart.pointInto(limbPoses, scratch, out)) {
            out[0] = 0.0F;
            out[1] = 0.0F;
            out[2] = 0.0F;
         }
      }
   }

   private void apply(int part, ModelPartTransformation transformation, float value, float weight) {
      boolean override = transformation.overrideAnimation();
      switch (transformation.type()) {
         case PITCH:
            this.put(part, 3, override ? this.get(part, 3) + (value - this.get(part, 3)) * weight : this.get(part, 3) + value * weight);
            break;
         case YAW:
            this.put(part, 4, override ? this.get(part, 4) + (value - this.get(part, 4)) * weight : this.get(part, 4) + value * weight);
            break;
         case ROLL:
            this.put(part, 5, override ? this.get(part, 5) + (value - this.get(part, 5)) * weight : this.get(part, 5) + value * weight);
            break;
         case X_SCALE:
            this.put(part, 6, 1.0F + value * weight);
            break;
         case Y_SCALE:
            this.put(part, 7, 1.0F + value * weight);
            break;
         case Z_SCALE:
            this.put(part, 8, 1.0F + value * weight);
            break;
         case PIVOT_X:
            this.add(part, 0, value * weight);
            break;
         case PIVOT_Y:
            this.add(part, 1, value * weight);
            break;
         case PIVOT_Z:
            this.add(part, 2, value * weight);
      }
   }

   public static int indexOf(BodyPart part) {
      return !part.isGroup() && part.models() != 0 ? singleLimb(part) : -1;
   }

   public static int singleLimb(BodyPart part) {
      int limbs = part.limbs();
      return Integer.bitCount(limbs) == 1 ? Integer.numberOfTrailingZeros(limbs) : -1;
   }

   private static int armPose(class_1309 entity, class_1268 hand) {
      class_1799 stack = entity.method_5998(hand);
      if (stack.method_7960()) {
         return 0;
      }

      if (entity.method_6115() && entity.method_6058() == hand && entity.method_6014() > 0) {
         class_1839 anim = stack.method_7976();
         if (anim == class_1839.field_8949) {
            return 2;
         }

         if (anim == class_1839.field_8953) {
            return 3;
         }

         if (anim == class_1839.field_8951) {
            return 4;
         }
      }

      return 1;
   }

   private static float rotlerpRad(float delta, float from, float to) {
      float diff = (to - from) % (float) (Math.PI * 2);
      if (diff < (float) -Math.PI) {
         diff += (float) (Math.PI * 2);
      }

      if (diff >= (float) Math.PI) {
         diff -= (float) (Math.PI * 2);
      }

      return from + delta * diff;
   }

   private static float quadraticArmUpdate(float value) {
      return -65.0F * value + value * value;
   }
}
