package dev.alphasucks.aea.port.power.builtin;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;

public final class ModifyFogInterpolator {
   private static final float SECONDS_PER_TICK = 0.05F;
   private static final float MAX_PROGRESS = 1000.0F;
   private static ModifyFogHandler.FogData previous = ModifyFogHandler.FogData.empty();
   private static ModifyFogHandler.FogData target = ModifyFogHandler.FogData.empty();
   private static float progress;
   @Nullable
   private static class_1937 boundLevel;
   @Nullable
   private static Float defaultStart;
   @Nullable
   private static Float defaultEnd;
   @Nullable
   private static class_243 defaultColor;

   private ModifyFogInterpolator() {
   }

   public static void setDefaultS(float value) {
      defaultStart = value;
   }

   public static void setDefaultV(float value) {
      defaultEnd = value;
   }

   public static void setDefaultColor(class_243 value) {
      defaultColor = value;
   }

   public static void reset() {
      previous = ModifyFogHandler.FogData.empty();
      target = ModifyFogHandler.FogData.empty();
      progress = 0.0F;
      boundLevel = null;
      defaultStart = null;
      defaultEnd = null;
      defaultColor = null;
   }

   public static void tick(@Nullable class_1297 entity) {
      if (entity == null) {
         if (boundLevel != null) {
            reset();
         }
      } else {
         if (entity.method_37908() != boundLevel) {
            reset();
            boundLevel = entity.method_37908();
         }

         if (progress < 1000.0F) {
            progress += 0.05F;
         }

         setTarget(ModifyFogHandler.getFog(entity));
      }
   }

   private static void setTarget(ModifyFogHandler.FogData incoming) {
      if (!incoming.sameAs(target)) {
         previous = getCurrent(defaultStart, defaultEnd, defaultColor);
         target = incoming;
         progress = 0.0F;
      }
   }

   public static ModifyFogHandler.FogData getCurrent(@Nullable Float defaultS, @Nullable Float defaultV, @Nullable class_243 defaultColorValue) {
      return previous.lerp(target, progress, defaultS, defaultV, defaultColorValue);
   }
}
