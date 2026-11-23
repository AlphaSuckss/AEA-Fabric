package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class ProjectileTickManager {
   private static final int MISSING_GRACE_TICKS = 20;
   private static final List<ProjectileTickManager.Tracked> TRACKED = new ArrayList();
   private static boolean loggedActionFailure;

   private ProjectileTickManager() {
   }

   public static void track(class_1297 projectile, class_1297 owner, BiEntityAction action) {
      if (projectile != null && action != null) {
         TRACKED.add(new ProjectileTickManager.Tracked(projectile, owner, action));
      }
   }

   public static void tick(MinecraftServer server) {
      if (!TRACKED.isEmpty()) {
         for (int i = TRACKED.size() - 1; i >= 0; i--) {
            ProjectileTickManager.Tracked t = (ProjectileTickManager.Tracked)TRACKED.get(i);
            class_1297 projectile = t.projectile;
            if (projectile == null || projectile.method_31481()) {
               projectile = find(server, t.projectileId);
               t.projectile = projectile;
            }

            if (projectile != null && projectile.method_37908() instanceof class_3218 level) {
               t.missingTicks = 0;
               class_1297 var8 = t.owner;
               if (t.ownerId != null && (var8 == null || var8.method_31481())) {
                  var8 = find(server, t.ownerId);
                  t.owner = var8;
               }

               try {
                  t.action.run(BiEntityCtx.of(var8, projectile, level));
               } catch (Exception e) {
                  drop(i);
                  if (!loggedActionFailure) {
                     loggedActionFailure = true;
                     Apoli.LOGGER
                        .error("[Apoli] tick_bientity_action failed on a fired projectile; that projectile stopped ticking. Later failures are not logged.", e);
                  }
               }
            } else if (++t.missingTicks > 20) {
               drop(i);
            }
         }
      }
   }

   public static void clearAll() {
      TRACKED.clear();
      loggedActionFailure = false;
   }

   private static void drop(int index) {
      int last = TRACKED.size() - 1;
      if (index != last) {
         TRACKED.set(index, (ProjectileTickManager.Tracked)TRACKED.get(last));
      }

      TRACKED.remove(last);
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

   private static final class Tracked {
      private final UUID projectileId;
      private final UUID ownerId;
      private final BiEntityAction action;
      private class_1297 projectile;
      private class_1297 owner;
      private int missingTicks;

      private Tracked(class_1297 projectile, class_1297 owner, BiEntityAction action) {
         this.projectileId = projectile.method_5667();
         this.ownerId = owner == null ? null : owner.method_5667();
         this.action = action;
         this.projectile = projectile;
         this.owner = owner;
      }
   }
}
