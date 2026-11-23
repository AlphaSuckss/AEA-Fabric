package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.access.ModifiedPoseHolder;
import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_3532;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import org.jetbrains.annotations.Nullable;

public final class EntityPoses {
   public static final Codec<class_4050> CODEC = Codec.STRING.comapFlatMap(EntityPoses::byName, pose -> pose.name().toLowerCase(Locale.ROOT));
   public static final int MAX_TICKS = 1200;
   public static final float PLAYER_MODEL_SCALE = 0.9375F;
   private static final float BLEND_TICKS = 4.0F;
   private static final float HALF_PI = (float) (Math.PI / 2);
   private static final int STRIDE = 9;
   private static final int X_ROT = 3;
   private static final int Y_ROT = 4;
   private static final int Z_ROT = 5;
   private static final int Y = 1;
   private static final int Z = 2;
   private static final int HEAD = 0;
   private static final int BODY = 1;
   private static final int RIGHT_ARM = 2;
   private static final int LEFT_ARM = 3;
   private static final int RIGHT_LEG = 4;
   private static final int LEFT_LEG = 5;

   private EntityPoses() {
   }

   private static DataResult<class_4050> byName(String name) {
      try {
         return DataResult.success(class_4050.valueOf(name.toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException e) {
         return DataResult.error(() -> "Unknown entity pose: '" + name + "'");
      }
   }

   @Nullable
   public static class_4050 forced(class_1297 entity) {
      return entity instanceof ModifiedPoseHolder holder ? holder.apoli$getModifiedEntityPose() : null;
   }

   public static int forcedTicks(class_1297 entity) {
      return entity instanceof ModifiedPoseHolder holder ? holder.apoli$getPoseTicks() : 0;
   }

   public static float progress(class_1297 entity, float partialTick) {
      return Math.min(1.0F, (forcedTicks(entity) + partialTick) / 4.0F);
   }

   public static int fallFlyingTicks(class_1297 entity, int original) {
      return original <= 0 && forced(entity) == class_4050.field_18077 ? forcedTicks(entity) : original;
   }

   public static int deathTime(class_1297 entity, int original) {
      return original <= 0 && forced(entity) == class_4050.field_18082 ? forcedTicks(entity) + 1 : original;
   }

   @Nullable
   public static class_4048 playerDimensions(class_4050 pose) {
      return pose == class_4050.field_40118 ? EntityPoses.PlayerDimensions.SITTING : null;
   }

   public static float playerEyeHeight(class_4050 pose) {
      return pose == class_4050.field_40118 ? 1.0F : -1.0F;
   }

   public static boolean resizesPlayer(@Nullable class_4050 pose) {
      return pose == class_4050.field_40118;
   }

   public static boolean holdsStill(class_4050 pose) {
      return pose == class_4050.field_40118 || pose == class_4050.field_18082;
   }

   public static boolean movesBody(class_4050 pose) {
      return pose == class_4050.field_40118;
   }

   public static float bodyPitch(class_4050 pose) {
      return 0.0F;
   }

   public static float bodyDrop(class_4050 pose) {
      return pose == class_4050.field_40118 ? 10.0F : 0.0F;
   }

   public static boolean shapes(class_4050 pose) {
      return switch (pose) {
         case field_40118, field_30095, field_37422, field_37423, field_38097, field_38098, field_38099, field_38100 -> true;
         default -> false;
      };
   }

   public static void shape(class_4050 pose, float[] parts, float t, float age) {
      switch (pose) {
         case field_40118:
            sitting(parts, t);
            break;
         case field_30095:
            longJumping(parts, t);
            break;
         case field_37422:
            croaking(parts, t, age);
            break;
         case field_37423:
            usingTongue(parts, t);
            break;
         case field_38097:
            roaring(parts, t, age);
            break;
         case field_38098:
            sniffing(parts, t, age);
            break;
         case field_38099:
            emerging(parts, t, age);
            break;
         case field_38100:
            digging(parts, t, age);
      }
   }

   private static void sitting(float[] p, float t) {
      put(p, 4, 3, (float) (-Math.PI / 2), t);
      put(p, 4, 4, 0.25F, t);
      put(p, 4, 5, 0.0785F, t);
      put(p, 5, 3, (float) (-Math.PI / 2), t);
      put(p, 5, 4, -0.25F, t);
      put(p, 5, 5, -0.0785F, t);
      add(p, 2, 3, -0.55F, t);
      add(p, 3, 3, -0.55F, t);
   }

   private static void longJumping(float[] p, float t) {
      lean(p, 0.25F, t, 0.0F);
      put(p, 4, 3, -0.9F, t);
      put(p, 5, 3, 0.7F, t);
      add(p, 2, 3, -1.5F, t);
      add(p, 2, 4, 0.3F, t);
      add(p, 3, 3, -1.5F, t);
      add(p, 3, 4, -0.3F, t);
   }

   private static void croaking(float[] p, float t, float age) {
      float puff = Math.abs(class_3532.method_15374(age * 0.35F)) * 0.15F;
      add(p, 0, 3, -0.3F, t);
      add(p, 2, 5, 0.45F + puff, t);
      add(p, 2, 3, -0.1F, t);
      add(p, 3, 5, -0.45F - puff, t);
      add(p, 3, 3, -0.1F, t);
      put(p, 4, 5, 0.2F, t);
      put(p, 5, 5, -0.2F, t);
   }

   private static void usingTongue(float[] p, float t) {
      lean(p, 0.4F, t, 0.0F);
      add(p, 0, 3, -0.55F, t);
      add(p, 2, 3, 0.7F, t);
      add(p, 2, 5, 0.2F, t);
      add(p, 3, 3, 0.7F, t);
      add(p, 3, 5, -0.2F, t);
   }

   private static void roaring(float[] p, float t, float age) {
      float shake = class_3532.method_15374(age * 2.0F) * 0.04F;
      lean(p, -0.2F, t, 0.0F);
      add(p, 0, 3, -0.6F + shake, t);
      add(p, 2, 5, 1.2F + shake, t);
      add(p, 2, 3, 0.15F, t);
      add(p, 3, 5, -1.2F - shake, t);
      add(p, 3, 3, 0.15F, t);
      put(p, 4, 5, 0.12F, t);
      put(p, 5, 5, -0.12F, t);
   }

   private static void sniffing(float[] p, float t, float age) {
      lean(p, 0.45F, t, 0.8F);
      add(p, 0, 3, 0.25F, t);
      add(p, 0, 4, class_3532.method_15374(age * 0.14F) * 0.45F, t);
   }

   private static void emerging(float[] p, float t, float age) {
      float reach = class_3532.method_15374(age * 0.25F) * 0.3F;
      add(p, 0, 3, -0.5F, t);
      add(p, 2, 3, -2.9F + reach, t);
      add(p, 2, 5, -0.25F, t);
      add(p, 3, 3, -2.9F - reach, t);
      add(p, 3, 5, 0.25F, t);
   }

   private static void digging(float[] p, float t, float age) {
      float scoop = class_3532.method_15374(age * 0.5F) * 0.5F;
      lean(p, 0.75F, t, 0.8F);
      add(p, 2, 3, -0.5F + scoop, t);
      add(p, 3, 3, -0.5F - scoop, t);
      add(p, 0, 3, 0.5F, t);
   }

   private static void lean(float[] p, float angle, float t, float armFollow) {
      float cos = class_3532.method_15362(angle);
      float sin = class_3532.method_15374(angle);
      float neck = 12.0F - 12.0F * cos;
      put(p, 1, 3, angle, t);
      put(p, 1, 1, neck, t);
      put(p, 0, 1, neck, t);
      put(p, 2, 1, neck + 2.0F * cos, t);
      put(p, 2, 2, 2.0F * sin, t);
      add(p, 2, 3, angle * armFollow, t);
      put(p, 3, 1, neck + 2.0F * cos, t);
      put(p, 3, 2, 2.0F * sin, t);
      add(p, 3, 3, angle * armFollow, t);
      put(p, 4, 1, 12.0F, t);
      put(p, 4, 2, 12.0F * sin, t);
      put(p, 5, 1, 12.0F, t);
      put(p, 5, 2, 12.0F * sin, t);
   }

   private static void put(float[] p, int part, int field, float value, float t) {
      int i = part * 9 + field;
      p[i] += (value - p[i]) * t;
   }

   private static void add(float[] p, int part, int field, float value, float t) {
      p[part * 9 + field] = p[part * 9 + field] + value * t;
   }

   private static final class PlayerDimensions {
      private static final class_4048 SITTING = class_4048.method_18384(0.6F, 1.2F);
   }
}
