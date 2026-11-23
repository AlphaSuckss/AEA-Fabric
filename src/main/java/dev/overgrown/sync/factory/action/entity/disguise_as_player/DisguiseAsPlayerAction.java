package dev.overgrown.sync.factory.action.entity.disguise_as_player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.data.disguise.DisguiseData;
import dev.overgrown.sync.factory.data.disguise.DisguiseManager;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public class DisguiseAsPlayerAction {
   private static final class_2960 PLAYER_TYPE_ID = new class_2960("minecraft", "player");

   private static void action(Instance data, class_1297 entity) {
      if (entity instanceof class_1309 living) {
         if (entity.method_37908() instanceof class_3218 serverWorld) {
            boolean overwrite = data.getBoolean("overwrite");
            if (overwrite || !DisguiseManager.isDisguised(entity.method_5667())) {
               if (data.isPresent("before_action")) {
                  data.<io.github.apace100.apoli.power.factory.action.ActionFactory.Instance>get("before_action").accept(entity);
               }

               MinecraftServer server = serverWorld.method_8503();
               // the profile lookups can hit Mojang's servers: do them off the server thread, apply back on it
               CompletableFuture.supplyAsync(() -> resolveProfile(data, server).map(profile -> server.method_3844().fillProfileProperties(profile, false)), class_156.method_27958())
                  .whenCompleteAsync((profileOpt, error) -> {
                     if (error != null) {
                        Sync.LOGGER.warn("sync:disguise_as_player – profile lookup failed: {}", error.getMessage());
                     } else if (profileOpt.isEmpty()) {
                        Sync.LOGGER.warn("sync:disguise_as_player – no cached profile found. The player must have joined this server at least once.");
                     } else if (!living.method_31481()) {
                        GameProfile profile = (GameProfile)profileOpt.get();
                        class_2487 profileNbt = buildProfileNbt(profile);
                        DisguiseData disguiseData = new DisguiseData(PLAYER_TYPE_ID, -1, profile.getId(), profileNbt);
                        DisguiseManager.forceApplyDisguise(living, disguiseData);
                        if (data.isPresent("after_action")) {
                           data.<io.github.apace100.apoli.power.factory.action.ActionFactory.Instance>get("after_action").accept(entity);
                        }
                     }
                  }, server);
            }
         }
      } else {
         Sync.LOGGER.warn("sync:disguise_as_player – entity '{}' is not a LivingEntity, skipping.", entity.method_5864().method_35050());
      }
   }

   private static Optional<GameProfile> resolveProfile(Instance data, MinecraftServer server) {
      if (data.isPresent("player_uuid")) {
         String uuidStr = data.getString("player_uuid");

         try {
            UUID uuid = UUID.fromString(uuidStr);
            return Optional.of(new GameProfile(uuid, ""));
         } catch (IllegalArgumentException e) {
            Sync.LOGGER.warn("sync:disguise_as_player – invalid UUID '{}': {}", uuidStr, e.getMessage());
         }
      }

      return data.isPresent("player_name") ? server.method_3793().method_14515(data.getString("player_name")) : Optional.empty();
   }

   private static class_2487 buildProfileNbt(GameProfile profile) {
      Collection<Property> textureProps = profile.getProperties().get("textures");
      boolean hasTextures = textureProps != null && !textureProps.isEmpty();
      String name = profile.getName();
      boolean hasName = name != null && !name.isEmpty();
      if (!hasTextures && !hasName) {
         return null;
      }

      class_2487 nbt = new class_2487();
      if (hasName) {
         nbt.method_10582("sync$player_name", name);
      }

      if (hasTextures) {
         Property texture = (Property)textureProps.iterator().next();
         nbt.method_10582("sync$skin_value", texture.getValue());
         String sig = texture.getSignature();
         if (sig != null && !sig.isEmpty()) {
            nbt.method_10582("sync$skin_signature", sig);
         }

         nbt.method_10582("sync$skin_model", parseModelType(texture.getValue()));
      }

      return nbt;
   }

   private static String parseModelType(String base64Value) {
      try {
         String json = new String(Base64.getDecoder().decode(base64Value));
         JsonObject root = JsonParser.parseString(json).getAsJsonObject();
         JsonObject textures = root.getAsJsonObject("textures");
         if (textures != null && textures.has("SKIN")) {
            JsonObject skin = textures.getAsJsonObject("SKIN");
            if (skin.has("metadata")) {
               JsonObject metadata = skin.getAsJsonObject("metadata");
               if (metadata.has("model")) {
                  return metadata.get("model").getAsString();
               }
            }
         }
      } catch (Exception e) {
         Sync.LOGGER.warn("sync:disguise_as_player – failed to parse skin model type: {}", e.getMessage());
      }

      return "default";
   }

   public static ActionFactory<class_1297> getFactory() {
      return new ActionFactory<>(
         Sync.identifier("disguise_as_player"),
         new SerializableData()
            .add("player_name", SerializableDataTypes.STRING, null)
            .add("player_uuid", SerializableDataTypes.STRING, null)
            .add("overwrite", SerializableDataTypes.BOOLEAN, true)
            .add("before_action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("after_action", ApoliDataTypes.ENTITY_ACTION, null),
         DisguiseAsPlayerAction::action
      );
   }
}
