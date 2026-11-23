package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class TeleportHelper {
   private TeleportHelper() {
   }

   @Nullable
   public static class_3218 level(class_1297 entity, Optional<class_2960> dimension) {
      if (entity.method_37908() instanceof class_3218 current) {
         if (dimension.isEmpty()) {
            return current;
         }

         MinecraftServer server = current.method_8503();
         return server.method_3847(class_5321.method_29179(class_7924.field_41223, (class_2960)dimension.get()));
      } else {
         return null;
      }
   }

   public static boolean chunkLoaded(class_3218 level, double x, double z) {
      return level.method_8393(class_3532.method_15357(x) >> 4, class_3532.method_15357(z) >> 4);
   }

   public static boolean landingAllowed(
      class_1297 entity, class_3218 level, double x, double y, double z, Optional<BlockCondition> blockCondition, Optional<EntityCondition> entityCondition
   ) {
      if (blockCondition.isPresent()) {
         class_2338 below = class_2338.method_49637(x, y - 1.0, z);
         if (!((BlockCondition)blockCondition.get()).test(new BlockCtx(below, level.method_8320(below), level))) {
            return false;
         }
      }

      if (entityCondition.isEmpty()) {
         return true;
      }

      if (entity.method_37908() != level) {
         return true;
      }

      class_243 origin = entity.method_19538();
      entity.method_5814(x, y, z);
      class_1297 previous = StagedLanding.begin(entity);

      boolean allowed;
      try {
         allowed = ((EntityCondition)entityCondition.get()).test(new EntityCtx(entity, level));
      } finally {
         StagedLanding.end(previous);
         entity.method_5814(origin.field_1352, origin.field_1351, origin.field_1350);
      }

      return allowed;
   }

   @Nullable
   public static class_1297 teleport(class_1297 entity, class_3218 level, double x, double y, double z, float yaw, float pitch) {
      class_1937 origin = entity.method_37908();
      if (!entity.method_48105(level, x, y, z, Set.of(), yaw, pitch)) {
         return null;
      } else {
         return origin != level && entity.method_31481() ? level.method_14190(entity.method_5667()) : entity;
      }
   }

   @Nullable
   public static class_1297 teleport(class_1297 entity, class_3218 level, double x, double y, double z) {
      return teleport(entity, level, x, y, z, entity.method_36454(), entity.method_36455());
   }
}
