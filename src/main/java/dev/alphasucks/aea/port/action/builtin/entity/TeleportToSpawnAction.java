package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.TeleportHelper;
import dev.alphasucks.aea.port.power.builtin.ModifyPlayerSpawnHandler;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3481;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;

public final class TeleportToSpawnAction implements ActionType<EntityCtx, TeleportToSpawnAction.Cfg> {
   @Override
   public MapCodec<TeleportToSpawnAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("player_spawn", true).forGetter(TeleportToSpawnAction.Cfg::playerSpawn),
               LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(TeleportToSpawnAction.Cfg::successAction),
               LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(TeleportToSpawnAction.Cfg::failAction)
            )
            .apply(i, TeleportToSpawnAction.Cfg::new)
      );
   }

   public void run(TeleportToSpawnAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && entity.method_37908() instanceof class_3218 current) {
         MinecraftServer var15 = current.method_8503();
         class_3218 level = null;
         class_243 destination = null;
         float yaw = 0.0F;
         if (cfg.playerSpawn && entity instanceof class_3222 player) {
            class_2338 respawn = ModifyPlayerSpawnHandler.respawnPosition(player, player.method_26280());
            class_5321<class_1937> dimension = player.method_26281();
            class_3218 respawnLevel = respawn == null ? null : var15.method_3847(dimension);
            if (respawnLevel != null) {
               float angle = player.method_30631();
               Optional<class_243> found = class_1657.method_26091(respawnLevel, respawn, angle, player.method_26282(), true);
               if (found.isPresent()) {
                  level = respawnLevel;
                  destination = (class_243)found.get();
                  yaw = spawnYaw(respawnLevel, respawn, destination, angle);
               }
            }
         }

         if (level == null || destination == null) {
            level = var15.method_30002();
            class_2338 shared = level.method_43126();
            destination = new class_243(shared.method_10263() + 0.5, shared.method_10264(), shared.method_10260() + 0.5);
            yaw = level.method_43127();
         }

         class_1297 moved = TeleportHelper.teleport(entity, level, destination.field_1352, destination.field_1351, destination.field_1350, yaw, 0.0F);
         if (moved == null) {
            cfg.failAction.ifPresent(a -> a.run(ctx));
         } else {
            cfg.successAction.ifPresent(a -> a.run(new EntityCtx(moved, moved.method_37908())));
         }
      }
   }

   private static float spawnYaw(class_3218 level, class_2338 anchor, class_243 stand, float fallback) {
      class_2680 state = level.method_8320(anchor);
      if (!state.method_26164(class_3481.field_16443) && !state.method_27852(class_2246.field_23152)) {
         return fallback;
      }

      class_243 away = class_243.method_24955(anchor).method_1020(stand).method_1029();
      return (float)class_3532.method_15338(class_3532.method_15349(away.field_1350, away.field_1352) * (180.0 / Math.PI) - 90.0);
   }

   public record Cfg(boolean playerSpawn, Optional<EntityAction> successAction, Optional<EntityAction> failAction) {
   }
}
