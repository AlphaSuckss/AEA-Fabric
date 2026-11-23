package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.ModifyFogPower.Config;
import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;

public final class ModifyFogHandler {
   private static final int START = 0;
   private static final int END = 1;
   private static final int RED = 2;
   private static final int GREEN = 3;
   private static final int BLUE = 4;
   private static final int FADE = 5;
   private static final int SLOTS = 6;
   private static final ThreadLocal<ModifyFogHandler.Fold> FOLD = ThreadLocal.withInitial(ModifyFogHandler.Fold::new);

   private ModifyFogHandler() {
   }

   public static ModifyFogHandler.FogData getFog(@Nullable class_1297 entity) {
      ModifyFogHandler.Fold fold = (ModifyFogHandler.Fold)FOLD.get();
      fold.reset();
      PowerLookup.forEach(entity, ApoliIds.MODIFY_FOG, Config.class, cfg -> {
         int priority = cfg.priority();
         if (cfg.s().isPresent()) {
            fold.offer(0, priority, (float)((Expression)cfg.s().get()).eval(entity));
         }

         if (cfg.v().isPresent()) {
            fold.offer(1, priority, (float)((Expression)cfg.v().get()).eval(entity));
         }

         if (cfg.r().isPresent()) {
            fold.offer(2, priority, (float)((Expression)cfg.r().get()).eval(entity));
         }

         if (cfg.g().isPresent()) {
            fold.offer(3, priority, (float)((Expression)cfg.g().get()).eval(entity));
         }

         if (cfg.b().isPresent()) {
            fold.offer(4, priority, (float)((Expression)cfg.b().get()).eval(entity));
         }

         if (!fold.claimed[5] || priority > fold.priorities[5]) {
            fold.offer(5, priority, 0.0F);
            fold.fadeIn = (float)cfg.fadeIn().eval(entity);
            fold.fadeOut = (float)cfg.fadeOut().eval(entity);
         }
      });
      float unset = -1.0F;
      class_243 color = !fold.claimed[2] && !fold.claimed[3] && !fold.claimed[4]
         ? ModifyFogHandler.FogData.UNSET_COLOR
         : new class_243(fold.valueOr(2, unset), fold.valueOr(3, unset), fold.valueOr(4, unset));
      return new ModifyFogHandler.FogData(fold.valueOr(0, unset), fold.valueOr(1, unset), color, fold.fadeIn, fold.fadeOut);
   }

   public static final class FogData {
      public static final float UNSET = -1.0F;
      public static final class_243 UNSET_COLOR = new class_243(-1.0, -1.0, -1.0);
      public final float s;
      public final float v;
      public final class_243 color;
      public final float fadeIn;
      public final float fadeOut;

      public FogData(float s, float v, class_243 color, float fadeIn, float fadeOut) {
         this.s = s;
         this.v = v;
         this.color = color;
         this.fadeIn = fadeIn;
         this.fadeOut = fadeOut;
      }

      public static ModifyFogHandler.FogData empty() {
         return new ModifyFogHandler.FogData(-1.0F, -1.0F, UNSET_COLOR, 0.0F, 0.0F);
      }

      public boolean sameAs(ModifyFogHandler.FogData other) {
         return this.s == other.s && this.v == other.v && this.color.equals(other.color);
      }

      public ModifyFogHandler.FogData lerp(
         ModifyFogHandler.FogData target, float progress, @Nullable Float defaultS, @Nullable Float defaultV, @Nullable class_243 defaultColor
      ) {
         float span = this.fadeOut + target.fadeIn;
         float percent = span <= 0.0F ? 1.0F : Math.min(1.0F, progress / span);
         if (percent >= 1.0F) {
            return target;
         }

         float fromS = this.s == -1.0F ? (Float)Objects.requireNonNullElse(defaultS, target.s) : this.s;
         float fromV = this.v == -1.0F ? (Float)Objects.requireNonNullElse(defaultV, target.v) : this.v;
         class_243 fromColor = this.color.equals(UNSET_COLOR) ? (class_243)Objects.requireNonNullElse(defaultColor, target.color) : this.color;
         float toS = target.s == -1.0F ? (Float)Objects.requireNonNullElse(defaultS, fromS) : target.s;
         float toV = target.v == -1.0F ? (Float)Objects.requireNonNullElse(defaultV, fromV) : target.v;
         class_243 toColor = target.color.equals(UNSET_COLOR) ? (class_243)Objects.requireNonNullElse(defaultColor, fromColor) : target.color;
         return new ModifyFogHandler.FogData(
            fromS + (toS - fromS) * percent,
            fromV + (toV - fromV) * percent,
            fromColor.method_1019(toColor.method_1020(fromColor).method_1021(percent)),
            target.fadeIn,
            target.fadeOut
         );
      }
   }

   private static final class Fold {
      private final float[] values = new float[6];
      private final int[] priorities = new int[6];
      private final boolean[] claimed = new boolean[6];
      private float fadeIn;
      private float fadeOut;

      private void reset() {
         for (int i = 0; i < 6; i++) {
            this.claimed[i] = false;
         }

         this.fadeIn = 0.0F;
         this.fadeOut = 0.0F;
      }

      private void offer(int slot, int priority, float value) {
         if (!this.claimed[slot] || priority > this.priorities[slot]) {
            this.claimed[slot] = true;
            this.priorities[slot] = priority;
            this.values[slot] = value;
         }
      }

      private float valueOr(int slot, float fallback) {
         return this.claimed[slot] ? this.values[slot] : fallback;
      }
   }
}
