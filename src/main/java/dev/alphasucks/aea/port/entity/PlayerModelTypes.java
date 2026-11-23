package dev.alphasucks.aea.port.entity;

import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.data.PlayerModelType;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

public final class PlayerModelTypes {
   private static final Map<UUID, PlayerModelType> KNOWN = new ConcurrentHashMap();
   private static final int DEFAULT_SKIN_COUNT = 18;
   private static final int DEFAULT_SLIM_COUNT = 9;

   private PlayerModelTypes() {
   }

   public static void set(UUID player, PlayerModelType type) {
      KNOWN.put(player, type);
   }

   public static void resolveFrom(UUID player, GameProfile profile) {
      PlayerModelType derived = fromProfile(profile);
      KNOWN.putIfAbsent(player, derived != null ? derived : defaultFor(player));
   }

   public static void remove(UUID player) {
      KNOWN.remove(player);
   }

   public static void clear() {
      KNOWN.clear();
   }

   public static PlayerModelType of(class_1297 entity) {
      if (!(entity instanceof class_1657)) {
         return PlayerModelType.WIDE;
      }

      UUID id = entity.method_5667();
      PlayerModelType known = (PlayerModelType)KNOWN.get(id);
      return known != null ? known : defaultFor(id);
   }

   public static PlayerModelType defaultFor(UUID player) {
      return Math.floorMod(player.hashCode(), 18) < 9 ? PlayerModelType.SLIM : PlayerModelType.WIDE;
   }

   @Nullable
   public static PlayerModelType fromProfile(@Nullable GameProfile profile) {
      if (profile == null) {
         return null;
      }

      for (Property property : profile.getProperties().get("textures")) {
         String encoded = property.getValue();
         if (encoded != null && !encoded.isEmpty()) {
            try {
               String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
               Dynamic<?> root = new Dynamic(JsonOps.INSTANCE, JsonParser.parseString(json));
               Dynamic<?> skin = (Dynamic<?>)root.get("textures").get("SKIN").result().orElse(null);
               if (skin != null) {
                  if (skin.get("metadata").result().isEmpty()) {
                     return PlayerModelType.WIDE;
                  }

                  return skin.get("metadata").get("model").asString().result().filter("slim"::equalsIgnoreCase).isPresent()
                     ? PlayerModelType.SLIM
                     : PlayerModelType.WIDE;
               }
            } catch (Exception var7) {
            }
         }
      }

      return null;
   }
}
