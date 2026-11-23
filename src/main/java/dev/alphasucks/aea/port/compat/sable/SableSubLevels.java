package dev.alphasucks.aea.port.compat.sable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;

public final class SableSubLevels {
   private static volatile Method transformPosition;
   private static volatile Method transformPositionInverse;
   private static volatile Method transformNormalInverse;
   private static volatile Method massTracker;
   private static volatile Method massValue;

   private SableSubLevels() {
   }

   public static boolean available() {
      return SableSubLevels.Bridge.INSTANCE != null;
   }

   @Nullable
   public static UUID subLevelAt(class_1937 level, class_243 pos) {
      SableSubLevels.Bridge bridge = SableSubLevels.Bridge.INSTANCE;
      if (bridge == null) {
         return null;
      }

      try {
         Object subLevel = bridge.getContaining.invoke(bridge.helper, new Object[]{level, pos});
         return subLevel == null ? null : (UUID)bridge.getUniqueId.invoke(subLevel, new Object[0]);
      } catch (Throwable t) {
         return null;
      }
   }

   public static boolean exists(class_1937 level, UUID subLevel) {
      return resolve(level, subLevel) != null;
   }

   @Nullable
   public static class_243 toWorld(class_1937 level, UUID subLevel, class_243 local) {
      SableSubLevels.Bridge bridge = SableSubLevels.Bridge.INSTANCE;
      Object handle = resolve(level, subLevel);
      if (bridge != null && handle != null) {
         if (bridge.projectOutOfSubLevel != null) {
            try {
               class_243 projected = (class_243)bridge.projectOutOfSubLevel.invoke(bridge.helper, new Object[]{level, local});
               if (projected != null) {
                  return projected;
               }
            } catch (Throwable var6) {
            }
         }

         return transform(handle, local, false);
      } else {
         return null;
      }
   }

   @Nullable
   public static class_243 toLocal(class_1937 level, UUID subLevel, class_243 world) {
      Object handle = resolve(level, subLevel);
      return handle == null ? null : transform(handle, world, true);
   }

   public static boolean pull(class_1937 level, UUID subLevel, class_243 localPoint, class_243 worldDirection, double speed, double forceScale) {
      SableSubLevels.Bridge bridge = SableSubLevels.Bridge.INSTANCE;
      if (bridge != null && bridge.rigidBodyOf != null && bridge.applyImpulseAtPoint != null) {
         if (worldDirection.method_1027() < 1.0E-8) {
            return false;
         }

         Object handle = resolve(level, subLevel);
         if (handle != null && bridge.rigidBodyOf.getParameterTypes()[0].isInstance(handle)) {
            try {
               Object body = bridge.rigidBodyOf.invoke(null, new Object[]{handle});
               if (body == null) {
                  return false;
               }

               Object pose = bridge.logicalPose.invoke(handle, new Object[0]);
               if (pose == null) {
                  return false;
               }

               Method normal = normalMethod(pose);
               if (normal == null) {
                  return false;
               }

               double magnitude = speed * forceScale * mass(handle);
               if (magnitude == 0.0) {
                  return false;
               }

               class_243 impulse = (class_243)normal.invoke(pose, new Object[]{worldDirection.method_1029().method_1021(magnitude)});
               if (impulse == null) {
                  return false;
               }

               bridge.applyImpulseAtPoint.invoke(body, new Object[]{localPoint, impulse});
               return true;
            } catch (Throwable t) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static double mass(Object subLevel) {
      try {
         Method tracker = massTracker;
         if (tracker == null) {
            tracker = subLevel.getClass().getMethod("getMassTracker", new Class[0]);
            massTracker = tracker;
         }

         Object data = tracker.invoke(subLevel, new Object[0]);
         if (data == null) {
            return 1.0;
         }

         Method value = massValue;
         if (value == null) {
            value = data.getClass().getMethod("getMass", new Class[0]);
            massValue = value;
         }

         double amount = ((Number)value.invoke(data, new Object[0])).doubleValue();
         return amount > 0.0 ? amount : 1.0;
      } catch (Throwable t) {
         return 1.0;
      }
   }

   @Nullable
   private static Object resolve(class_1937 level, UUID subLevel) {
      SableSubLevels.Bridge bridge = SableSubLevels.Bridge.INSTANCE;
      if (bridge == null) {
         return null;
      }

      try {
         Object container = bridge.getContainer.invoke(null, new Object[]{level});
         return container == null ? null : bridge.getSubLevel.invoke(container, new Object[]{subLevel});
      } catch (Throwable t) {
         return null;
      }
   }

   @Nullable
   private static class_243 transform(Object subLevel, class_243 point, boolean inverse) {
      SableSubLevels.Bridge bridge = SableSubLevels.Bridge.INSTANCE;
      if (bridge == null) {
         return null;
      }

      try {
         Object pose = bridge.logicalPose.invoke(subLevel, new Object[0]);
         if (pose == null) {
            return null;
         }

         Method method = poseMethod(pose, inverse);
         return method == null ? null : (class_243)method.invoke(pose, new Object[]{point});
      } catch (Throwable t) {
         return null;
      }
   }

   @Nullable
   private static Method poseMethod(Object pose, boolean inverse) {
      Method cached = inverse ? transformPositionInverse : transformPosition;
      if (cached != null) {
         return cached;
      }

      String name = inverse ? "transformPositionInverse" : "transformPosition";
      Method found = findVec3Method(pose.getClass(), name);
      if (found == null) {
         return null;
      }

      if (inverse) {
         transformPositionInverse = found;
      } else {
         transformPosition = found;
      }

      return found;
   }

   @Nullable
   private static Method normalMethod(Object pose) {
      Method cached = transformNormalInverse;
      if (cached != null) {
         return cached;
      }

      Method found = findVec3Method(pose.getClass(), "transformNormalInverse");
      if (found != null) {
         transformNormalInverse = found;
      }

      return found;
   }

   @Nullable
   private static Method findVec3Method(Class<?> owner, String name) {
      for (Method method : owner.getMethods()) {
         if (method.getName().equals(name)) {
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == class_243.class && method.getReturnType() == class_243.class) {
               return method;
            }
         }
      }

      return null;
   }

   private static final class Bridge {
      static final SableSubLevels.Bridge INSTANCE = resolve();
      final Object helper;
      final Method getContaining;
      final Method getUniqueId;
      final Method getContainer;
      final Method getSubLevel;
      final Method logicalPose;
      @Nullable
      final Method projectOutOfSubLevel;
      @Nullable
      final Method rigidBodyOf;
      @Nullable
      final Method applyImpulseAtPoint;

      private Bridge(
         Object helper,
         Method getContaining,
         Method getUniqueId,
         Method getContainer,
         Method getSubLevel,
         Method logicalPose,
         @Nullable Method projectOutOfSubLevel,
         @Nullable Method rigidBodyOf,
         @Nullable Method applyImpulseAtPoint
      ) {
         this.helper = helper;
         this.getContaining = getContaining;
         this.getUniqueId = getUniqueId;
         this.getContainer = getContainer;
         this.getSubLevel = getSubLevel;
         this.logicalPose = logicalPose;
         this.projectOutOfSubLevel = projectOutOfSubLevel;
         this.rigidBodyOf = rigidBodyOf;
         this.applyImpulseAtPoint = applyImpulseAtPoint;
      }

      @Nullable
      private static SableSubLevels.Bridge resolve() {
         if (!classPresent("dev/ryanhcode/sable/api/sublevel/SubLevelContainer")) {
            return null;
         }

         try {
            Class<?> sable = Class.forName("dev.ryanhcode.sable.Sable");
            Field helperField = sable.getField("HELPER");
            Object helper = helperField.get(null);
            if (helper == null) {
               return null;
            }

            Method getContaining = findMethod(helper.getClass(), "getContaining", class_1937.class, class_243.class);
            Class<?> containerClass = Class.forName("dev.ryanhcode.sable.api.sublevel.SubLevelContainer");
            Method getContainer = findMethod(containerClass, "getContainer", class_1937.class);
            Method getSubLevel = findMethod(containerClass, "getSubLevel", UUID.class);
            if (getContaining != null && getContainer != null && getSubLevel != null) {
               Class<?> subLevelClass = getContaining.getReturnType();
               Method getUniqueId = subLevelClass.getMethod("getUniqueId", new Class[0]);
               Method logicalPose = subLevelClass.getMethod("logicalPose", new Class[0]);
               Method projectOutOfSubLevel = null;

               for (Method method : helper.getClass().getMethods()) {
                  if (method.getName().equals("projectOutOfSubLevel")) {
                     Class<?>[] params = method.getParameterTypes();
                     if (params.length == 2 && params[0] == class_1937.class && params[1] == class_243.class && method.getReturnType() == class_243.class) {
                        projectOutOfSubLevel = method;
                        break;
                     }
                  }
               }

               Method rigidBodyOf = null;
               Method applyImpulseAtPoint = null;

               try {
                  Class<?> handleClass = Class.forName("dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle");

                  for (Method method : handleClass.getMethods()) {
                     if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("of") && method.getParameterCount() == 1) {
                        rigidBodyOf = method;
                     } else if (!Modifier.isStatic(method.getModifiers())
                        && method.getName().equals("applyImpulseAtPoint")
                        && method.getParameterCount() == 2
                        && method.getParameterTypes()[0] == class_243.class
                        && method.getParameterTypes()[1] == class_243.class) {
                        applyImpulseAtPoint = method;
                     }
                  }
               } catch (Throwable var18) {
               }

               return new SableSubLevels.Bridge(
                  helper, getContaining, getUniqueId, getContainer, getSubLevel, logicalPose, projectOutOfSubLevel, rigidBodyOf, applyImpulseAtPoint
               );
            } else {
               return null;
            }
         } catch (Throwable t) {
            return null;
         }
      }

      private static boolean classPresent(String internalName) {
         return SableSubLevels.class.getClassLoader().getResource(internalName + ".class") != null;
      }

      @Nullable
      private static Method findMethod(Class<?> owner, String name, Class<?>... argumentTypes) {
         for (Method method : owner.getMethods()) {
            if (method.getName().equals(name)) {
               Class<?>[] params = method.getParameterTypes();
               if (params.length == argumentTypes.length) {
                  boolean matches = true;

                  for (int i = 0; i < params.length; i++) {
                     if (!params[i].isAssignableFrom(argumentTypes[i])) {
                        matches = false;
                        break;
                     }
                  }

                  if (matches) {
                     return method;
                  }
               }
            }
         }

         return null;
      }
   }
}
