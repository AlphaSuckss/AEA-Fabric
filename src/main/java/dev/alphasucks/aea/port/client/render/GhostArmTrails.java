package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.access.DualSwingHolder;
import dev.alphasucks.aea.port.power.builtin.ShowBothArmsPower;
import dev.alphasucks.aea.port.power.builtin.ShowBothArmsPower.Arms;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;

public final class GhostArmTrails {
   private static final int X = 0;
   private static final int Z = 1;
   private static final int X_ROT = 2;
   private static final int Y_ROT = 3;
   private static final int Z_ROT = 4;
   private static final Arms ARMS = new Arms();
   private static final float[] RIGHT_ARM = new float[5];
   private static final float[] LEFT_ARM = new float[5];
   private static final float[] RIGHT_SLEEVE = new float[5];
   private static final float[] LEFT_SLEEVE = new float[5];

   private GhostArmTrails() {
   }

   public static void render(class_572<?> model, class_1309 entity, class_4587 poseStack, class_4597 buffers, class_2960 texture, int light) {
      float attack = model.field_3447;
      if (!(attack <= 0.0F)) {
         if (ShowBothArmsPower.resolve(entity, ARMS) && ARMS.ghostsIn(false)) {
            float spacing = ARMS.spacing;
            if (!(spacing <= 0.0F)) {
               class_1306 attacking = entity.field_6266 == class_1268.field_5808 ? entity.method_6068() : entity.method_6068().method_5928();
               boolean dual = DualSwingHolder.of(entity);
               int edits = ArmEdits.of(entity, false);
               boolean rightSwings = (dual || attacking == class_1306.field_6183) && !ArmEdits.held(edits, class_1306.field_6183);
               boolean leftSwings = (dual || attacking == class_1306.field_6182) && !ArmEdits.held(edits, class_1306.field_6182);
               if (rightSwings || leftSwings) {
                  boolean leftLead = attacking == class_1306.field_6182;
                  float headPitch = model.field_3398.field_3654;
                  class_591<?> player = model instanceof class_591<?> playerModel ? playerModel : null;
                  snapshot(model.field_3401, RIGHT_ARM);
                  snapshot(model.field_27433, LEFT_ARM);
                  if (player != null) {
                     snapshot(player.field_3486, RIGHT_SLEEVE);
                     snapshot(player.field_3484, LEFT_SLEEVE);
                  }

                  class_4588 consumer = buffers.getBuffer(class_1921.method_23580(texture));
                  float alpha = ARMS.alpha;
                  float yawNow = bodyYaw(attack, leftLead);
                  float liftNow = lift(attack, headPitch);
                  float rollNow = roll(attack);

                  try {
                     for (int n = ARMS.ghosts; n >= 1; n--) {
                        float lag = spacing * n;
                        if (!(lag >= 1.0F)) {
                           float then = attack - lag;
                           if (then <= 0.0F) {
                              then++;
                           }

                           float yawThen = bodyYaw(then, leftLead);
                           float yaw = yawThen - yawNow;
                           float lift = lift(then, headPitch) - liftNow;
                           float roll = roll(then) - rollNow;
                           float across = (class_3532.method_15362(yawThen) - class_3532.method_15362(yawNow)) * 5.0F;
                           float forward = (class_3532.method_15374(yawThen) - class_3532.method_15374(yawNow)) * 5.0F;
                           if (rightSwings) {
                              lag(model.field_3401, RIGHT_ARM, -across, forward, -lift, yaw * 3.0F, roll);
                              model.field_3401.method_22699(poseStack, consumer, light, class_4608.field_21444, 1.0F, 1.0F, 1.0F, alpha);
                              if (player != null) {
                                 lag(player.field_3486, RIGHT_SLEEVE, -across, forward, -lift, yaw * 3.0F, roll);
                                 player.field_3486.method_22699(poseStack, consumer, light, class_4608.field_21444, 1.0F, 1.0F, 1.0F, alpha);
                              }
                           }

                           if (leftSwings) {
                              lag(model.field_27433, LEFT_ARM, across, -forward, yaw - lift, yaw * 3.0F, roll);
                              model.field_27433.method_22699(poseStack, consumer, light, class_4608.field_21444, 1.0F, 1.0F, 1.0F, alpha);
                              if (player != null) {
                                 lag(player.field_3484, LEFT_SLEEVE, across, -forward, yaw - lift, yaw * 3.0F, roll);
                                 player.field_3484.method_22699(poseStack, consumer, light, class_4608.field_21444, 1.0F, 1.0F, 1.0F, alpha);
                              }
                           }
                        }
                     }
                  } finally {
                     restore(model.field_3401, RIGHT_ARM);
                     restore(model.field_27433, LEFT_ARM);
                     if (player != null) {
                        restore(player.field_3486, RIGHT_SLEEVE);
                        restore(player.field_3484, LEFT_SLEEVE);
                     }
                  }
               }
            }
         }
      }
   }

   private static float bodyYaw(float attack, boolean leftLead) {
      float yaw = class_3532.method_15374(class_3532.method_15355(attack) * (float) (Math.PI * 2)) * 0.2F;
      return leftLead ? -yaw : yaw;
   }

   private static float lift(float attack, float headPitch) {
      float eased = 1.0F - attack;
      eased *= eased;
      eased *= eased;
      eased = 1.0F - eased;
      return class_3532.method_15374(eased * (float) Math.PI) * 1.2F + class_3532.method_15374(attack * (float) Math.PI) * -(headPitch - 0.7F) * 0.75F;
   }

   private static float roll(float attack) {
      return class_3532.method_15374(attack * (float) Math.PI) * -0.4F;
   }

   private static void snapshot(class_630 part, float[] out) {
      out[0] = part.field_3657;
      out[1] = part.field_3655;
      out[2] = part.field_3654;
      out[3] = part.field_3675;
      out[4] = part.field_3674;
   }

   private static void lag(class_630 part, float[] now, float dx, float dz, float dxRot, float dyRot, float dzRot) {
      part.field_3657 = now[0] + dx;
      part.field_3655 = now[1] + dz;
      part.field_3654 = now[2] + dxRot;
      part.field_3675 = now[3] + dyRot;
      part.field_3674 = now[4] + dzRot;
   }

   private static void restore(class_630 part, float[] now) {
      part.field_3657 = now[0];
      part.field_3655 = now[1];
      part.field_3654 = now[2];
      part.field_3675 = now[3];
      part.field_3674 = now[4];
   }
}
