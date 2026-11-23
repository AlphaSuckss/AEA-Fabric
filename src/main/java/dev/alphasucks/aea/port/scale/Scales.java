package dev.alphasucks.aea.port.scale;

import dev.alphasucks.aea.port.compat.pehkui.PehkuiBridge;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import dev.alphasucks.aea.port.power.builtin.ScalePower;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_4048;
import org.jetbrains.annotations.Nullable;

public final class Scales {
   private static final PowerTypeGate SCALE_POWERS = new PowerTypeGate(ApoliIds.SCALE);
   @Nullable
   private static Consumer<class_1297> syncer;

   private Scales() {
   }

   public static void setSyncer(Consumer<class_1297> sender) {
      syncer = sender;
   }

   @Nullable
   public static ScaleState stateOf(class_1297 entity) {
      return ((ScaleHolder)entity).apoli$scales();
   }

   public static ScaleState stateOrCreate(class_1297 entity) {
      return ((ScaleHolder)entity).apoli$scalesOrCreate();
   }

   public static boolean anyPowerLoaded() {
      return SCALE_POWERS.inUse();
   }

   public static boolean untouched(class_1297 entity) {
      if (!anyPowerLoaded()) {
         return stateOf(entity) == null;
      } else {
         return stateOf(entity) != null ? false : !ScalePower.hasAny(entity);
      }
   }

   public static class_4048 scaledDimensions(class_1297 entity, class_4048 base) {
      if (untouched(entity)) {
         return base;
      }

      float width = applied(entity, ScaleTypes.HITBOX_WIDTH);
      float height = applied(entity, ScaleTypes.HITBOX_HEIGHT);
      return width == 1.0F && height == 1.0F ? base : base.method_19539(width, height);
   }

   public static float value(class_1297 entity, ScaleType type) {
      return value(entity, type, 1.0F);
   }

   public static float value(class_1297 entity, ScaleType type, float partial) {
      ScaleState state = stateOf(entity);
      if (state == null) {
         if (!anyPowerLoaded() || !ScalePower.hasAny(entity)) {
            return 1.0F;
         }

         state = stateOrCreate(entity);
      }

      return resolve(entity, state, state.isAnimating() ? partial : 1.0F)[type.index()];
   }

   public static float applied(class_1297 entity, ScaleType type) {
      return applied(entity, type, 1.0F);
   }

   public static float applied(class_1297 entity, ScaleType type, float partial) {
      if (!PehkuiBridge.ownsGeometry()) {
         return value(entity, type, partial);
      } else {
         return PehkuiBridge.handles(type) ? 1.0F : beyondPehkui(entity, type, partial);
      }
   }

   public static float appliedIfScaled(class_1297 entity, ScaleType type, float partial) {
      return untouched(entity) ? 1.0F : applied(entity, type, partial);
   }

   private static float beyondPehkui(class_1297 entity, ScaleType type, float partial) {
      ScaleState state = stateOf(entity);
      if (state == null) {
         if (!anyPowerLoaded() || !ScalePower.hasAny(entity)) {
            return 1.0F;
         }

         state = stateOrCreate(entity);
      }

      resolve(entity, state, state.isAnimating() ? partial : 1.0F);
      return ownBeyondPehkui(state, type);
   }

   private static float ownBeyondPehkui(ScaleState state, ScaleType type) {
      float v = state.ownAt(type.index());
      ScaleType[] multipliers = type.multipliers();

      for (int m = 0; m < multipliers.length; m++) {
         if (!PehkuiBridge.handles(multipliers[m])) {
            v *= ownBeyondPehkui(state, multipliers[m]);
         }
      }

      ScaleType[] divisors = type.divisors();

      for (int d = 0; d < divisors.length; d++) {
         if (!PehkuiBridge.handles(divisors[d])) {
            float f = ownBeyondPehkui(state, divisors[d]);
            if (f != 0.0F) {
               v /= f;
            }
         }
      }

      return ScaleState.clamp(v);
   }

   public static float own(class_1297 entity, ScaleType type) {
      ScaleState state = stateOf(entity);
      if (state == null) {
         if (!anyPowerLoaded() || !ScalePower.hasAny(entity)) {
            return 1.0F;
         }

         state = stateOrCreate(entity);
      }

      resolve(entity, state, 1.0F);
      return state.ownAt(type.index());
   }

   public static float base(class_1297 entity, ScaleType type) {
      ScaleState state = stateOf(entity);
      return state == null ? 1.0F : state.base(type);
   }

   public static void set(class_1297 entity, ScaleType type, float target, int overTicks, ScaleEasing easing) {
      ScaleState state = stateOrCreate(entity);
      state.set(type, target, overTicks, easing);
      onChanged(entity, state);
   }

   public static void setAll(class_1297 entity, List<ScaleType> types, ScaleOperation operation, float argument, int overTicks, ScaleEasing easing) {
      int n = types.size();
      if (n != 0) {
         ScaleState state = stateOrCreate(entity);

         for (int i = 0; i < n; i++) {
            ScaleType type = (ScaleType)types.get(i);
            state.set(type, operation.apply(state.target(type), argument), overTicks, easing);
         }

         onChanged(entity, state);
      }
   }

   public static void reset(class_1297 entity) {
      ScaleState state = stateOf(entity);
      if (state != null) {
         state.reset();
         onChanged(entity, state);
      }
   }

   public static void transfer(class_1297 from, class_1297 to) {
      ScaleState source = stateOf(from);
      if (source != null && !source.isDefault()) {
         ScaleState target = stateOrCreate(to);
         target.copyFrom(source);
         onChanged(to, target);
      }
   }

   public static void invalidate(class_1297 entity) {
      ScaleState state = stateOf(entity);
      if (state == null) {
         if (!anyPowerLoaded() || !ScalePower.hasAny(entity)) {
            return;
         }

         state = stateOrCreate(entity);
      }

      state.invalidate();
      state.forgetDimensions();
      refreshDimensions(entity, state);
   }

   public static void onChanged(class_1297 entity, ScaleState state) {
      state.invalidate();
      refreshDimensions(entity, state);
      sync(entity);
   }

   public static void sync(class_1297 entity) {
      if (!entity.method_37908().method_8608() && syncer != null) {
         syncer.accept(entity);
      }
   }

   public static void syncIfScaled(class_1297 entity) {
      ScaleState state = stateOf(entity);
      if (state != null && !state.isDefault()) {
         sync(entity);
      }
   }

   public static void tick(class_1297 entity, ScaleState state) {
      if (state.needsTick()) {
         state.tick();
         refreshDimensions(entity, state);
      }
   }

   public static float[] ownAll(class_1297 entity, ScaleState state) {
      resolve(entity, state, 1.0F);
      return state.ownSlot();
   }

   public static void refreshDimensions(class_1297 entity, ScaleState state) {
      if (PehkuiBridge.ownsGeometry()) {
         PehkuiBridge.push(entity, state);
      } else {
         float width = value(entity, ScaleTypes.HITBOX_WIDTH);
         float height = value(entity, ScaleTypes.HITBOX_HEIGHT);
         float eye = value(entity, ScaleTypes.EYE_HEIGHT);
         if (state.dimensionsChanged(width, height, eye)) {
            entity.method_18382();
         }
      }
   }

   private static float[] resolve(class_1297 entity, ScaleState state, float partial) {
      long gameTime = entity.method_37908().method_8510();
      int generation = ApoliPowers.generation();
      float[] hit = state.cached(gameTime, generation, partial);
      if (hit != null) {
         return hit;
      }

      float[] out = state.cacheSlot();
      int n = ScaleTypes.count();

      for (int i = 0; i < n; i++) {
         out[i] = state.baseAt(i, partial);
      }

      boolean powered = anyPowerLoaded() && ScalePower.applyFactors(entity, out);
      System.arraycopy(out, 0, state.ownSlot(), 0, n);

      for (int i = 0; i < n; i++) {
         ScaleType type = ScaleTypes.byIndex(i);
         float v = out[i];
         ScaleType[] multipliers = type.multipliers();

         for (int m = 0; m < multipliers.length; m++) {
            v *= out[multipliers[m].index()];
         }

         ScaleType[] divisors = type.divisors();

         for (int d = 0; d < divisors.length; d++) {
            float f = out[divisors[d].index()];
            if (f != 0.0F) {
               v /= f;
            }
         }

         out[i] = ScaleState.clamp(v);
      }

      state.storeCache(gameTime, generation, partial, powered || state.isAnimating());
      return out;
   }
}
