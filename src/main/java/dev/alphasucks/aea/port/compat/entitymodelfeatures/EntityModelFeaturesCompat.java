package dev.alphasucks.aea.port.compat.entitymodelfeatures;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.mixin.custom_model.ModelPartChildrenAccessor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_630;

@Environment(EnvType.CLIENT)
public final class EntityModelFeaturesCompat {
   private static final class_630[] NO_PARTS = new class_630[0];
   private static final Map<class_630, EntityModelFeaturesCompat.CustomChildren> CUSTOM_CHILDREN = new WeakHashMap();
   private static boolean hooked;
   private static int held;
   private static Set<UUID> pausedEntities;
   private static MethodHandle currentEntity;
   private static Class<?> customPartClass;
   private static UUID pausedByApoli;

   private EntityModelFeaturesCompat() {
   }

   public static void init() {
      if (!hooked && ModCompat.ENTITY_MODEL_FEATURES) {
         try {
            Method register = Class.forName("traben.entity_model_features.EMFAnimationApi").getMethod("registerPauseCondition", new Class[]{Function.class});
            register.invoke(null, new Object[]{(Function)entity -> held > 0});
            hooked = true;
         } catch (Throwable failure) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] Entity Model Features is installed but its animation-pause API could not be reached ({}). Texture overlays and custom geometry may drift out of alignment with CEM animations such as Fresh Animations.",
                  failure.toString()
               );
         }

         pausedEntities = resolvePausedEntities();
         currentEntity = resolveCurrentEntity();
         customPartClass = resolveCustomPartClass();
      }
   }

   private static Set<UUID> resolvePausedEntities() {
      try {
         return (Set<UUID>)(Class.forName("traben.entity_model_features.utils.EMFAnimationPauseHandler").getField("entitiesPaused").get(null) instanceof Set<?> set
            ? set
            : null);
      } catch (Throwable failure) {
         return null;
      }
   }

   private static MethodHandle resolveCurrentEntity() {
      try {
         Method current = Class.forName("traben.entity_model_features.EMFAnimationApi").getMethod("getCurrentEntity", new Class[0]);
         return MethodHandles.publicLookup().unreflect(current).asType(MethodType.methodType(Object.class));
      } catch (Throwable failure) {
         return null;
      }
   }

   private static Class<?> resolveCustomPartClass() {
      try {
         return Class.forName("traben.entity_model_features.models.parts.EMFModelPartCustom");
      } catch (Throwable failure) {
         return null;
      }
   }

   public static void holdPose() {
      if (hooked) {
         if (held++ == 0) {
            pauseCurrentEntity();
         }
      }
   }

   public static void releasePose() {
      if (hooked && held != 0) {
         if (--held == 0 && pausedByApoli != null) {
            pausedEntities.remove(pausedByApoli);
            pausedByApoli = null;
         }
      }
   }

   private static void pauseCurrentEntity() {
      if (pausedEntities != null && currentEntity != null) {
         try {
            if ((Object)currentEntity.invokeExact() instanceof class_1297 entity) {
               UUID id = entity.method_5667();
               if (pausedEntities.add(id)) {
                  pausedByApoli = id;
               }
            }
         } catch (Throwable var3) {
         }
      }
   }

   public static boolean hasCustomParts() {
      return customPartClass != null;
   }

   public static class_630[] customChildren(class_630 part) {
      if (customPartClass == null) {
         return NO_PARTS;
      }

      Map<String, class_630> children = ((ModelPartChildrenAccessor)(Object)part).apoli$children();
      EntityModelFeaturesCompat.CustomChildren cached = (EntityModelFeaturesCompat.CustomChildren)CUSTOM_CHILDREN.get(part);
      if (cached != null && cached.source() == children && cached.size() == children.size()) {
         return cached.parts();
      }

      int count = 0;

      for (class_630 child : children.values()) {
         if (customPartClass.isInstance(child)) {
            count++;
         }
      }

      class_630[] parts = count == 0 ? NO_PARTS : new class_630[count];
      int i = 0;

      for (class_630 child : children.values()) {
         if (customPartClass.isInstance(child)) {
            parts[i++] = child;
         }
      }

      CUSTOM_CHILDREN.put(part, new EntityModelFeaturesCompat.CustomChildren(children, children.size(), parts));
      return parts;
   }

   private record CustomChildren(Map<String, class_630> source, int size, class_630[] parts) {
   }
}
