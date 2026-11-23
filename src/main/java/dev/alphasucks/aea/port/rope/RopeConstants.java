package dev.alphasucks.aea.port.rope;

import net.minecraft.class_243;

public final class RopeConstants {
   public static final float MIN_ROPE_LENGTH = 1.0F;
   public static final float GOAL_ROPE_SEGMENT_LENGTH = 0.5F;
   public static final float ROPE_LENGTH_CHANGE_STEP = 0.2F;
   public static final float SLACK_PULL_RATE_MULT = 5.0F;
   public static final float RADIAL_DAMPING = 0.85F;
   public static final float LEASH_STIFFNESS = 0.1F;
   public static final float SPRING_SCALING = 0.65F;
   public static final float SWING_BOOST = 1.08F;
   public static final float MAX_SWING_SPEED = 0.7F;
   public static final float SWING_CONTROL_ACCEL = 0.08F;
   public static final float ELYTRA_LENGTH_MOD = 5.0F;
   public static final int ELYTRA_TIME_LIMIT = 10;
   public static final class_243 GRAVITY = new class_243(0.0, -0.08, 0.0);
   public static final float ROPE_DAMPING = 0.99F;
   public static final float ROPE_STIFFNESS = 0.98F;
   public static final float ROPE_WIDTH = 0.1F;

   private RopeConstants() {
   }
}
