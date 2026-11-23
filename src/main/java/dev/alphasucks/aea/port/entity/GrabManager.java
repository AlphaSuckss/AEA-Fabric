package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.data.Expression;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

public final class GrabManager {
   private static final Map<UUID, GrabManager.Grab> BY_GRABBED = new HashMap();
   private static final Set<UUID> KEYBINDS_DISABLED = new HashSet();
   private static final double MIN_HOLD_CLEARANCE = 0.5;

   private GrabManager() {
   }

   public static void start(
      class_1297 grabber,
      class_1297 grabbed,
      int duration,
      Expression distance,
      boolean disableGrabber,
      boolean disableGrabbed,
      boolean horizontalOnly,
      boolean verticalOnly
   ) {
      if (!grabber.method_37908().method_8608() && grabber != grabbed) {
         long expiry = duration > 0 ? grabber.method_37908().method_8510() + duration : -1L;
         BY_GRABBED.put(
            grabbed.method_5667(),
            new GrabManager.Grab(
               grabber.method_5667(),
               grabbed.method_5667(),
               distance,
               expiry,
               disableGrabber,
               disableGrabbed,
               horizontalOnly,
               verticalOnly,
               grabber.method_36454()
            )
         );
         rebuildDisabled();
      }
   }

   public static void release(UUID entity) {
      boolean changed = BY_GRABBED.remove(entity) != null;
      changed |= BY_GRABBED.values().removeIf(grab -> grab.grabber().equals(entity));
      if (changed) {
         rebuildDisabled();
      }
   }

   public static boolean isGrabbed(UUID entity) {
      return BY_GRABBED.containsKey(entity);
   }

   public static boolean isGrabbedBy(UUID grabbed, UUID grabber) {
      GrabManager.Grab grab = (GrabManager.Grab)BY_GRABBED.get(grabbed);
      return grab != null && grab.grabber().equals(grabber);
   }

   public static boolean keybindsDisabled(UUID entity) {
      return !KEYBINDS_DISABLED.isEmpty() && KEYBINDS_DISABLED.contains(entity);
   }

   public static void tick(MinecraftServer server) {
      if (!BY_GRABBED.isEmpty()) {
         boolean changed = false;
         Iterator<GrabManager.Grab> it = BY_GRABBED.values().iterator();

         while (it.hasNext()) {
            GrabManager.Grab grab = (GrabManager.Grab)it.next();
            class_1297 grabberEntity = find(server, grab.grabber());
            class_1297 grabbedEntity = find(server, grab.grabbed());
            if (grabberEntity != null
               && grabbedEntity != null
               && grabberEntity.method_5805()
               && grabbedEntity.method_5805()
               && grabberEntity.method_37908() == grabbedEntity.method_37908()
               && (grab.expiryTick() < 0L || server.method_30002().method_8510() < grab.expiryTick())) {
               double distance = grab.distance().eval(grabberEntity);
               float yaw = grab.verticalOnly() ? grab.lockedYaw() : grabberEntity.method_36454();
               float pitch = grab.horizontalOnly() ? 0.0F : grabberEntity.method_36455();
               class_243 look = class_243.method_1030(pitch, yaw);
               class_243 eye = grabberEntity.method_33571();
               class_243 current = grabbedEntity.method_19538();
               double holdY = Math.max(eye.field_1351 + look.field_1351 * distance - grabbedEntity.method_17682() / 2.0, grabberEntity.method_23318() + 0.5);
               class_243 delta = new class_243(
                  eye.field_1352 + look.field_1352 * distance - current.field_1352,
                  holdY - current.field_1351,
                  eye.field_1350 + look.field_1350 * distance - current.field_1350
               );
               class_243 allowed = class_1297.method_20736(grabbedEntity, delta, grabbedEntity.method_5829(), grabbedEntity.method_37908(), List.of());
               double x = current.field_1352 + allowed.field_1352;
               double y = current.field_1351 + allowed.field_1351;
               double z = current.field_1350 + allowed.field_1350;
               grabbedEntity.field_6017 = 0.0F;
               grabbedEntity.method_18799(class_243.field_1353);
               if (grabbedEntity instanceof class_3222 player) {
                  player.field_13987.method_14363(x, y, z, player.method_36454(), player.method_36455());
               } else {
                  grabbedEntity.method_5814(x, y, z);
               }
            } else {
               it.remove();
               changed = true;
            }
         }

         if (changed) {
            rebuildDisabled();
         }
      }
   }

   private static class_1297 find(MinecraftServer server, UUID id) {
      for (class_3218 level : server.method_3738()) {
         class_1297 entity = level.method_14190(id);
         if (entity != null) {
            return entity;
         }
      }

      return null;
   }

   private static void rebuildDisabled() {
      KEYBINDS_DISABLED.clear();

      for (GrabManager.Grab grab : BY_GRABBED.values()) {
         if (grab.disableGrabber()) {
            KEYBINDS_DISABLED.add(grab.grabber());
         }

         if (grab.disableGrabbed()) {
            KEYBINDS_DISABLED.add(grab.grabbed());
         }
      }
   }

   public static void clearAll() {
      BY_GRABBED.clear();
      KEYBINDS_DISABLED.clear();
   }

   private record Grab(
      UUID grabber,
      UUID grabbed,
      Expression distance,
      long expiryTick,
      boolean disableGrabber,
      boolean disableGrabbed,
      boolean horizontalOnly,
      boolean verticalOnly,
      float lockedYaw
   ) {
   }
}
