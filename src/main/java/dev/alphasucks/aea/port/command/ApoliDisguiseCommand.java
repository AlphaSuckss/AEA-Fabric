package dev.alphasucks.aea.port.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_1657;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2179;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

public final class ApoliDisguiseCommand {
   private static final class_2960 PLAYER_TYPE = new class_2960("player");
   private static final SuggestionProvider<class_2168> ENTITY_TYPES = (ctx, builder) -> class_2172.method_9270(class_7923.field_41177.method_10235(), builder);
   private static final SuggestionProvider<class_2168> ONLINE_PLAYERS = (ctx, builder) -> class_2172.method_9264(
      ((class_2168)ctx.getSource()).method_9211().method_3760().method_14571().stream().map(p -> p.method_7334().getName()), builder
   );
   private static final SuggestionProvider<class_2168> ENTITY_TYPES_OR_PLAYERS = (ctx, builder) -> {
      class_2172.method_9264(
         ((class_2168)ctx.getSource()).method_9211().method_3760().method_14571().stream().map(p -> p.method_7334().getName().toLowerCase(Locale.ROOT)),
         builder
      );
      return class_2172.method_9270(class_7923.field_41177.method_10235(), builder);
   };

   private ApoliDisguiseCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:disguise")
         .requires(ApoliPermissions.require("apoli.command.disguise", 2));
      root.then(
         class_2170.method_9247("entity")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("entity_type", class_2232.method_9441())
                           .suggests(ENTITY_TYPES)
                           .executes(ctx -> disguiseAsEntity(ctx, null)))
                        .then(
                           class_2170.method_9244("nbt", class_2179.method_9284()).executes(ctx -> disguiseAsEntity(ctx, class_2179.method_9285(ctx, "nbt")))
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("player")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     class_2170.method_9244("player_name", StringArgumentType.word()).suggests(ONLINE_PLAYERS).executes(ApoliDisguiseCommand::disguiseAsPlayer)
                  )
            )
      );
      root.then(
         class_2170.method_9247("random")
            .then(
               ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306())
                           .executes(ctx -> disguiseRandom(ctx, ApoliDisguiseCommand.RandomKind.ANY)))
                        .then(class_2170.method_9247("player").executes(ctx -> disguiseRandom(ctx, ApoliDisguiseCommand.RandomKind.PLAYER))))
                     .then(class_2170.method_9247("entity").executes(ctx -> disguiseRandom(ctx, ApoliDisguiseCommand.RandomKind.ENTITY))))
                  .then(class_2170.method_9247("any").executes(ctx -> disguiseRandom(ctx, ApoliDisguiseCommand.RandomKind.ANY)))
            )
      );
      root.then(
         class_2170.method_9244("targets", class_2186.method_9306())
            .then(
               ((RequiredArgumentBuilder)class_2170.method_9244("disguise", class_2232.method_9441())
                     .suggests(ENTITY_TYPES_OR_PLAYERS)
                     .executes(ctx -> disguiseAsAny(ctx, null)))
                  .then(class_2170.method_9244("nbt", class_2179.method_9284()).executes(ctx -> disguiseAsAny(ctx, class_2179.method_9285(ctx, "nbt"))))
            )
      );
      root.then(class_2170.method_9247("clear").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ApoliDisguiseCommand::clear)));
      root.then(class_2170.method_9247("query").then(class_2170.method_9244("target", class_2186.method_9309()).executes(ApoliDisguiseCommand::query)));
      // AEA: Overgrown also redirected "/disguise" here; Sync owns that root, so only /apoli:disguise is registered.
      dispatcher.register(root);
   }

   private static int disguiseAsEntity(CommandContext<class_2168> ctx, class_2487 nbt) throws CommandSyntaxException {
      class_2960 typeId = class_2232.method_9443(ctx, "entity_type");
      if (class_7923.field_41177.method_17966(typeId).isEmpty()) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown entity type: " + typeId));
         return 0;
      }

      DisguiseData data = new DisguiseData(typeId, Optional.empty(), Optional.ofNullable(nbt), Optional.empty());
      int count = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         DisguiseManager.apply(target, data, true);
         count++;
      }

      feedback(ctx, count, "as " + typeId);
      return count;
   }

   private static int disguiseAsAny(CommandContext<class_2168> ctx, class_2487 nbt) throws CommandSyntaxException {
      class_2960 typeId = class_2232.method_9443(ctx, "disguise");
      DisguiseData data = null;
      if (class_7923.field_41177.method_17966(typeId).isPresent()) {
         data = new DisguiseData(typeId, Optional.empty(), Optional.ofNullable(nbt), Optional.empty());
      } else if (typeId.method_12836().equals(PLAYER_TYPE.method_12836())) {
         Optional<GameProfile> profile = resolveProfile(((class_2168)ctx.getSource()).method_9211(), typeId.method_12832());
         if (profile.isPresent()) {
            data = playerData((GameProfile)profile.get());
         }
      }

      if (data == null) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown entity type or player: " + typeId));
         return 0;
      }

      int count = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         DisguiseManager.apply(target, data, true);
         count++;
      }

      feedback(ctx, count, "as " + typeId);
      return count;
   }

   private static int disguiseAsPlayer(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      String name = StringArgumentType.getString(ctx, "player_name");
      Optional<GameProfile> profile = resolveProfile(((class_2168)ctx.getSource()).method_9211(), name);
      if (profile.isEmpty()) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("Unknown player: " + name));
         return 0;
      }

      int count = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         DisguiseManager.apply(target, playerData((GameProfile)profile.get()), true);
         count++;
      }

      feedback(ctx, count, "as player " + ((GameProfile)profile.get()).getName());
      return count;
   }

   private static int disguiseRandom(CommandContext<class_2168> ctx, ApoliDisguiseCommand.RandomKind kind) throws CommandSyntaxException {
      MinecraftServer server = ((class_2168)ctx.getSource()).method_9211();
      int count = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         ApoliDisguiseCommand.RandomKind resolved = kind;
         if (resolved == ApoliDisguiseCommand.RandomKind.ANY) {
            resolved = ThreadLocalRandom.current().nextBoolean() ? ApoliDisguiseCommand.RandomKind.PLAYER : ApoliDisguiseCommand.RandomKind.ENTITY;
         }

         if (resolved == ApoliDisguiseCommand.RandomKind.PLAYER) {
            GameProfile profile = randomPlayerProfile(server, target.method_5667());
            if (profile != null) {
               DisguiseManager.apply(target, playerData(profile), true);
               count++;
               continue;
            }

            if (kind == ApoliDisguiseCommand.RandomKind.PLAYER) {
               continue;
            }
         }

         class_2960 typeId = randomEntityTypeId(target);
         if (typeId != null) {
            DisguiseManager.apply(target, new DisguiseData(typeId, Optional.empty(), Optional.empty(), Optional.empty()), true);
            count++;
         }
      }

      if (count == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No valid random disguise found for the given targets."));
         return 0;
      } else {
         feedback(ctx, count, "randomly");
         return count;
      }
   }

   private static int clear(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      int count = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         if (DisguiseManager.isDisguised(target)) {
            DisguiseManager.remove(target);
            count++;
         }
      }

      int result = count;
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("Removed disguises from " + result + " entit" + (result == 1 ? "y" : "ies") + "."), true);
      return count;
   }

   private static int query(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_1297 target = class_2186.method_9313(ctx, "target");
      DisguiseData data = DisguiseManager.get(target.method_5667());
      if (data == null) {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(target.method_5477().getString() + " is not disguised."), false);
         return 0;
      } else {
         String as = data.isPlayerDisguise()
            ? "player " + (String)data.name().filter(n -> !n.isEmpty()).orElseGet(() -> (String)data.playerUuid().map(UUID::toString).orElse("?"))
            : data.entityTypeId().toString();
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(target.method_5477().getString() + " is disguised as " + as + "."), false);
         return 1;
      }
   }

   private static DisguiseData playerData(GameProfile profile) {
      String name = profile.getName();
      return new DisguiseData(PLAYER_TYPE, Optional.of(profile.getId()), Optional.empty(), Optional.ofNullable(name != null && !name.isEmpty() ? name : null));
   }

   private static Optional<GameProfile> resolveProfile(MinecraftServer server, String name) {
      class_3222 online = server.method_3760().method_14566(name);
      if (online != null) {
         return Optional.of(online.method_7334());
      } else {
         return server.method_3793() != null ? server.method_3793().method_14515(name) : Optional.empty();
      }
   }

   private static GameProfile randomPlayerProfile(MinecraftServer server, UUID excluded) {
      List<class_3222> pool = new ArrayList();

      for (class_3222 player : server.method_3760().method_14571()) {
         if (!player.method_5667().equals(excluded)) {
            pool.add(player);
         }
      }

      return pool.isEmpty() ? null : ((class_3222)pool.get(ThreadLocalRandom.current().nextInt(pool.size()))).method_7334();
   }

   private static class_2960 randomEntityTypeId(class_1297 target) {
      List<class_2960> pool = new ArrayList();

      for (class_1299<?> type : class_7923.field_41177) {
         if (type.method_5896() && type.method_5891() != class_1311.field_17715 && (type != target.method_5864() || target instanceof class_1657)) {
            pool.add(class_7923.field_41177.method_10221(type));
         }
      }

      return pool.isEmpty() ? null : (class_2960)pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
   }

   private static void feedback(CommandContext<class_2168> ctx, int count, String what) {
      if (count == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No entities were disguised."));
      } else {
         ((class_2168)ctx.getSource())
            .method_9226(() -> class_2561.method_43470("Disguised " + count + " entit" + (count == 1 ? "y" : "ies") + " " + what + "."), true);
      }
   }

   private enum RandomKind {
      PLAYER,
      ENTITY,
      ANY;
   }
}
