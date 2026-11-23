package dev.alphasucks.aea.port.dev;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.network.payload.DevModeS2C;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class DevMode {
   private static final Set<UUID> ENABLED = new HashSet();
   private static final Map<DevMode.Topic, Long> LAST_REPORT = new HashMap();
   private static final int MAX_TOPICS = 4096;
   private static volatile boolean any;

   private DevMode() {
   }

   public static boolean any() {
      return any;
   }

   public static boolean toggle(class_3222 player) {
      boolean enabled = !ENABLED.remove(player.method_5667());
      if (enabled) {
         ENABLED.add(player.method_5667());
      }

      any = !ENABLED.isEmpty();
      if (!any) {
         clearThrottles();
      }

      ApoliNetwork.sendDevMode(player, new DevModeS2C(enabled));
      return enabled;
   }

   public static boolean isEnabled(@Nullable class_1297 entity) {
      return any && entity != null && ENABLED.contains(entity.method_5667());
   }

   public static void echoCommand(@Nullable class_1297 actor, String command) {
      if (any && actor instanceof class_3222 player && ENABLED.contains(player.method_5667())) {
         player.method_43496(class_2561.method_43470("/" + command).method_27692(class_124.field_1062));
      }
   }

   public static void report(@Nullable class_1297 subject, String message) {
      if (any && subject != null) {
         if (subject.method_37908() instanceof class_3218 level) {
            List<class_3222> watchers = watchers(level);
            if (!watchers.isEmpty()) {
               class_2561 text = class_2561.method_43470("[apoli] " + subject.method_5477().getString() + ": " + message).method_27692(class_124.field_1061);

               for (int i = 0; i < watchers.size(); i++) {
                  ((class_3222)watchers.get(i)).method_43496(text);
               }
            }
         }
      }
   }

   public static void reportEvery(@Nullable class_1297 subject, Object topic, int ticks, String message) {
      if (any && subject != null) {
         long now = subject.method_37908().method_8510();
         DevMode.Topic key = new DevMode.Topic(subject.method_5667(), topic);
         Long last = (Long)LAST_REPORT.get(key);
         if (last == null || now < last || now - last >= ticks) {
            if (LAST_REPORT.size() >= 4096) {
               LAST_REPORT.clear();
            }

            LAST_REPORT.put(key, now);
            report(subject, message);
         }
      }
   }

   public static void announce(MinecraftServer server, List<class_2561> lines) {
      if (any && !lines.isEmpty()) {
         for (UUID uuid : ENABLED) {
            class_3222 player = server.method_3760().method_14602(uuid);
            if (player != null) {
               for (int i = 0; i < lines.size(); i++) {
                  player.method_43496((class_2561)lines.get(i));
               }
            }
         }
      }
   }

   public static void forget(UUID uuid) {
      if (ENABLED.remove(uuid)) {
         any = !ENABLED.isEmpty();
      }

      if (!any) {
         clearThrottles();
      }
   }

   private static void clearThrottles() {
      LAST_REPORT.clear();
      DevParticles.forget();
   }

   public static List<class_3222> watchers(@Nullable class_3218 level) {
      if (any && level != null) {
         MinecraftServer server = level.method_8503();
         if (server == null) {
            return List.of();
         }

         List<class_3222> out = null;

         for (UUID uuid : ENABLED) {
            class_3222 player = server.method_3760().method_14602(uuid);
            if (player != null && player.method_37908() == level) {
               if (out == null) {
                  out = new ArrayList(2);
               }

               out.add(player);
            }
         }

         return out == null ? List.of() : out;
      } else {
         return List.of();
      }
   }

   private record Topic(UUID subject, Object topic) {
   }
}
