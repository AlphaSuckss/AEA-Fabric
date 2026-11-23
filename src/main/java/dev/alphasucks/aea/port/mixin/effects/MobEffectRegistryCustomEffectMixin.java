package dev.alphasucks.aea.port.mixin.effects;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Lifecycle;
import dev.alphasucks.aea.port.effects.CustomMobEffect;
import dev.alphasucks.aea.port.effects.RuntimeMobEffectRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_2370;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.class_6880.class_6883;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Lets the frozen mob effect registry take (and drop) data-driven {@link CustomMobEffect}s at runtime.
 * AEA: unchanged from Overgrown apart from casts; only touches the mob effect registry.
 */
@Mixin(class_2370.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class MobEffectRegistryCustomEffectMixin<T> implements RuntimeMobEffectRegistry {
   @Final
   @Shadow
   class_5321<? extends class_2378<T>> field_41126;
   @Final
   @Shadow
   private ObjectList<class_6883<T>> field_26682;
   @Final
   @Shadow
   private Object2IntMap<T> field_26683;
   @Final
   @Shadow
   private Map<class_2960, class_6883<T>> field_11107;
   @Final
   @Shadow
   private Map<class_5321<T>, class_6883<T>> field_25067;
   @Final
   @Shadow
   private Map<T, class_6883<T>> field_36461;
   @Final
   @Shadow
   private Map<T, Lifecycle> field_26731;
   @Shadow
   private int field_11109;
   @Shadow
   @Nullable
   private List<class_6883<T>> field_36634;
   @Unique
   private boolean apoli$allowWrite;
   @Unique
   private Map<class_5321<T>, class_6883<T>> apoli$retired;

   @WrapMethod(method = "method_45939()V")
   private void apoli$allowRuntimeWrite(Operation<Void> original) {
      if (!this.apoli$allowWrite) {
         original.call(new Object[0]);
      }
   }

   @WrapMethod(method = "method_40274(Lnet/minecraft/class_5321;)V")
   private void apoli$allowRuntimeWriteKey(class_5321<T> resourceKey, Operation<Void> original) {
      if (!this.apoli$allowWrite) {
         original.call(new Object[]{resourceKey});
      }
   }

   @Override
   public void apoli$unregisterCustom(Set<class_2960> keep) {
      if (this.field_41126.equals(class_7924.field_41208)) {
         Iterator<Entry<class_5321<T>, class_6883<T>>> keys = this.field_25067.entrySet().iterator();

         while (keys.hasNext()) {
            Entry<class_5321<T>, class_6883<T>> entry = (Entry<class_5321<T>, class_6883<T>>)keys.next();
            if (apoli$retires((class_6883<?>)entry.getValue(), keep)) {
               class_5321<T> retiredKey = (class_5321<T>)entry.getKey();
               class_6883<T> retiredReference = (class_6883<T>)entry.getValue();
               keys.remove();
               if (this.apoli$retired == null) {
                  this.apoli$retired = new HashMap();
               }

               this.apoli$retired.put(retiredKey, retiredReference);
            }
         }

         Iterator<class_6883<T>> locations = this.field_11107.values().iterator();

         while (locations.hasNext()) {
            if (apoli$retires((class_6883<?>)locations.next(), keep)) {
               locations.remove();
            }
         }

         Iterator<T> values = this.field_36461.keySet().iterator();

         while (values.hasNext()) {
            if (apoli$retires(values.next(), keep)) {
               values.remove();
            }
         }

         Iterator<T> numbered = this.field_26683.keySet().iterator();

         while (numbered.hasNext()) {
            if (apoli$retires(numbered.next(), keep)) {
               numbered.remove();
            }
         }

         Iterator<T> lifecycleKeys = this.field_26731.keySet().iterator();

         while (lifecycleKeys.hasNext()) {
            if (apoli$retires(lifecycleKeys.next(), keep)) {
               lifecycleKeys.remove();
            }
         }

         this.field_36634 = null;
         int first = -1;

         for (int i = 0; i < this.field_26682.size(); i++) {
            if (apoli$retires((class_6883<?>)this.field_26682.get(i), keep)) {
               first = i;
               break;
            }
         }

         if (first >= 0) {
            int write = first;

            for (int read = first; read < this.field_26682.size(); read++) {
               class_6883<T> reference = (class_6883<T>)this.field_26682.get(read);
               if (reference != null && !apoli$retires(reference, keep)) {
                  this.field_26682.set(write, reference);
                  this.field_26683.put(reference.comp_349(), write);
                  write++;
               }
            }

            this.field_26682.size(write);
            this.field_11109 = write;
         }
      }
   }

   @Override
   public CustomMobEffect apoli$register(CustomMobEffect effect) {
      if (!this.field_41126.equals(class_7924.field_41208)) {
         return effect;
      }

      class_5321<T> resourceKey = class_5321.method_29179(this.field_41126, effect.id);
      CustomMobEffect installed = effect;
      if (this.apoli$retired != null) {
         class_6883<T> previous = (class_6883<T>)this.apoli$retired.remove(resourceKey);
         if (previous != null) {
            this.field_25067.put(resourceKey, previous);
            if (previous.method_40227() && previous.comp_349() instanceof CustomMobEffect earlier) {
               earlier.copyFrom(effect);
               installed = earlier;
            }
         }
      }

      this.apoli$allowWrite = true;

      try {
         class_2378.method_39197((class_2378<T>)(Object)this, resourceKey, (T)(Object)installed);
      } finally {
         this.apoli$allowWrite = false;
      }

      return installed;
   }

   @Unique
   private static boolean apoli$retires(class_6883<?> reference, Set<class_2960> keep) {
      return reference != null && reference.method_40227() && apoli$retires(reference.comp_349(), keep);
   }

   @Unique
   private static boolean apoli$retires(Object value, Set<class_2960> keep) {
      return value instanceof CustomMobEffect effect && !keep.contains(effect.id);
   }
}
