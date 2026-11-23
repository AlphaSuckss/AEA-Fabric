package dev.alphasucks.aea.port.command;

import java.util.function.Predicate;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.class_2168;

public final class ApoliPermissions {
   private static volatile boolean PERMISSIONS_API_LOADED = ApoliPermissions.class
         .getClassLoader()
         .getResource("me/lucko/fabric/api/permissions/v0/Permissions.class")
      != null;

   private ApoliPermissions() {
   }

   public static Predicate<class_2168> require(String node, int fallbackLevel) {
      return source -> check(source, node, fallbackLevel);
   }

   public static boolean check(class_2168 source, String node, int fallbackLevel) {
      if (PERMISSIONS_API_LOADED) {
         try {
            return ApoliPermissions.FabricPermissionsApi.check(source, node, fallbackLevel);
         } catch (LinkageError e) {
            // AEA: an incompatible fabric-permissions-api build must not crash the server (it runs on every
            // command-tree send, i.e. on join); fall back to vanilla op levels from now on
            PERMISSIONS_API_LOADED = false;
            dev.alphasucks.aea.port.Apoli.LOGGER.warn("fabric-permissions-api could not be used, falling back to op levels for AEA commands", e);
         }
      }
      return source.method_9259(fallbackLevel);
   }

   private static final class FabricPermissionsApi {
      static boolean check(class_2168 source, String node, int fallbackLevel) {
         return Permissions.check(source, node, fallbackLevel);
      }
   }
}
