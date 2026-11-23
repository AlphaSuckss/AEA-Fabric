package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

public final class DisguiseAsAction implements ActionType<EntityCtx, DisguiseAsAction.Cfg> {
   private static final class_2960 PLAYER_TYPE = new class_2960("player");

   @Override
   public MapCodec<DisguiseAsAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("entity_type").forGetter(DisguiseAsAction.Cfg::entityType),
               Codec.STRING.optionalFieldOf("player_name").forGetter(DisguiseAsAction.Cfg::playerName),
               Codec.STRING.optionalFieldOf("player_uuid").forGetter(DisguiseAsAction.Cfg::playerUuid),
               Nbt.CODEC.optionalFieldOf("nbt").forGetter(DisguiseAsAction.Cfg::nbt),
               Codec.BOOL.optionalFieldOf("overwrite", true).forGetter(DisguiseAsAction.Cfg::overwrite),
               Codec.BOOL.optionalFieldOf("change_name", true).forGetter(DisguiseAsAction.Cfg::changeName),
               LoggedOptionalField.of("before_action", EntityAction.CODEC).forGetter(DisguiseAsAction.Cfg::beforeAction),
               LoggedOptionalField.of("after_action", EntityAction.CODEC).forGetter(DisguiseAsAction.Cfg::afterAction)
            )
            .apply(i, DisguiseAsAction.Cfg::new)
      );
   }

   public void run(DisguiseAsAction.Cfg cfg, EntityCtx ctx) {
      class_1297 actor = ctx.entity();
      if (!ctx.level().field_9236) {
         if (cfg.overwrite || !DisguiseManager.isDisguised(actor)) {
            cfg.beforeAction.ifPresent(a -> a.run(ctx));
            Optional<String> noName = cfg.changeName ? Optional.empty() : Optional.of("");
            Optional<DisguiseData> data = !cfg.playerName.isPresent() && !cfg.playerUuid.isPresent()
               ? cfg.entityType.map(type -> new DisguiseData(type, Optional.empty(), cfg.nbt.map(Nbt::tag), noName))
               : resolvePlayer(cfg, ctx.level().method_8503());
            data.ifPresent(d -> DisguiseManager.apply(actor, d, cfg.overwrite));
            cfg.afterAction.ifPresent(a -> a.run(ctx));
         }
      }
   }

   private static Optional<DisguiseData> resolvePlayer(DisguiseAsAction.Cfg cfg, MinecraftServer server) {
      if (server == null) {
         return Optional.empty();
      }

      Optional<GameProfile> profile = Optional.empty();
      if (cfg.playerUuid.isPresent()) {
         try {
            profile = server.method_3793().method_14512(UUID.fromString((String)cfg.playerUuid.get()));
         } catch (IllegalArgumentException var4) {
         }
      }

      if (profile.isEmpty() && cfg.playerName.isPresent()) {
         profile = server.method_3793().method_14515((String)cfg.playerName.get());
      }

      if (profile.isEmpty() && cfg.playerName.isPresent()) {
         class_3222 online = server.method_3760().method_14566((String)cfg.playerName.get());
         if (online != null) {
            profile = Optional.of(online.method_7334());
         }
      }

      return profile.map(
         p -> new DisguiseData(
            PLAYER_TYPE,
            Optional.of(p.getId()),
            Optional.empty(),
            cfg.changeName ? Optional.ofNullable(p.getName() != null && !p.getName().isEmpty() ? p.getName() : null) : Optional.of("")
         )
      );
   }

   public record Cfg(
      Optional<class_2960> entityType,
      Optional<String> playerName,
      Optional<String> playerUuid,
      Optional<Nbt> nbt,
      boolean overwrite,
      boolean changeName,
      Optional<EntityAction> beforeAction,
      Optional<EntityAction> afterAction
   ) {
   }
}
