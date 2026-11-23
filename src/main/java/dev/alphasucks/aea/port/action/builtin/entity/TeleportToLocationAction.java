package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.SavedLocations;
import dev.alphasucks.aea.port.entity.TeleportHelper;
import dev.alphasucks.aea.port.entity.SavedLocations.Location;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class TeleportToLocationAction implements ActionType<EntityCtx, TeleportToLocationAction.Cfg> {
   @Override
   public MapCodec<TeleportToLocationAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.fieldOf("id").forGetter(TeleportToLocationAction.Cfg::id),
               Codec.BOOL.optionalFieldOf("keep_rotation", false).forGetter(TeleportToLocationAction.Cfg::keepRotation),
               Codec.BOOL.optionalFieldOf("clear", false).forGetter(TeleportToLocationAction.Cfg::clear),
               LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(TeleportToLocationAction.Cfg::successAction),
               LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(TeleportToLocationAction.Cfg::failAction)
            )
            .apply(i, TeleportToLocationAction.Cfg::new)
      );
   }

   public void run(TeleportToLocationAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && entity.method_37908() instanceof class_3218 current) {
         SavedLocations locations = SavedLocations.of(entity);
         Location location = locations == null ? null : locations.get(entity.method_5667(), cfg.id);
         if (location == null) {
            cfg.failAction.ifPresent(a -> a.run(ctx));
         } else {
            MinecraftServer server = current.method_8503();
            class_3218 level = server.method_3847(location.dimension());
            if (level == null) {
               cfg.failAction.ifPresent(a -> a.run(ctx));
            } else {
               float yaw = cfg.keepRotation ? entity.method_36454() : location.yaw();
               float pitch = cfg.keepRotation ? entity.method_36455() : location.pitch();
               class_1297 moved = TeleportHelper.teleport(entity, level, location.x(), location.y(), location.z(), yaw, pitch);
               if (moved == null) {
                  cfg.failAction.ifPresent(a -> a.run(ctx));
               } else {
                  if (cfg.clear) {
                     locations.remove(entity.method_5667(), cfg.id);
                  }

                  cfg.successAction.ifPresent(a -> a.run(new EntityCtx(moved, moved.method_37908())));
               }
            }
         }
      }
   }

   public record Cfg(String id, boolean keepRotation, boolean clear, Optional<EntityAction> successAction, Optional<EntityAction> failAction) {
   }
}
