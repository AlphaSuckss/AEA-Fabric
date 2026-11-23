package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Heightmap;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.entity.TeleportHelper;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_5819;

public final class RandomTeleportAction implements ActionType<EntityCtx, RandomTeleportAction.Cfg> {
   @Override
   public MapCodec<RandomTeleportAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.FLOAT.optionalFieldOf("area_width", 8.0F).forGetter(RandomTeleportAction.Cfg::areaWidth),
               Codec.FLOAT.optionalFieldOf("area_height", 8.0F).forGetter(RandomTeleportAction.Cfg::areaHeight),
               Heightmap.CODEC.optionalFieldOf("heightmap").forGetter(RandomTeleportAction.Cfg::heightmap),
               Codec.INT.optionalFieldOf("attempts").forGetter(RandomTeleportAction.Cfg::attempts),
               LoggedOptionalField.strict("landing_block_condition", BlockCondition.CODEC).forGetter(RandomTeleportAction.Cfg::landingBlockCondition),
               LoggedOptionalField.strict("landing_condition", EntityCondition.CODEC).forGetter(RandomTeleportAction.Cfg::landingCondition),
               Vector.CODEC.optionalFieldOf("landing_offset", Vector.ZERO).forGetter(RandomTeleportAction.Cfg::landingOffset),
               Codec.BOOL.optionalFieldOf("loaded_chunks_only", true).forGetter(RandomTeleportAction.Cfg::loadedChunksOnly),
               LoggedOptionalField.of("success_action", EntityAction.CODEC).forGetter(RandomTeleportAction.Cfg::successAction),
               LoggedOptionalField.of("fail_action", EntityAction.CODEC).forGetter(RandomTeleportAction.Cfg::failAction)
            )
            .apply(i, RandomTeleportAction.Cfg::new)
      );
   }

   public void run(RandomTeleportAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.entity();
      if (ctx.level() instanceof class_3218 serverLevel) {
         class_5819 random = serverLevel.field_9229;
         int attempts = (Integer)cfg.attempts.orElse((int)(cfg.areaWidth * 2.0F + cfg.areaHeight * 2.0F));
         int w = (int)cfg.areaWidth;
         int h = (int)cfg.areaHeight;
         class_2338 origin = entity.method_24515();

         for (int i = 0; i < attempts; i++) {
            int dx = random.method_43048(w * 2 + 1) - w;
            int dz = random.method_43048(w * 2 + 1) - w;
            int dy = random.method_43048(h * 2 + 1) - h;
            class_2338 candidate = origin.method_10069(dx, dy, dz);
            if (!cfg.loadedChunksOnly || serverLevel.method_8393(candidate.method_10263() >> 4, candidate.method_10260() >> 4)) {
               if (cfg.heightmap.isPresent()) {
                  int top = serverLevel.method_8624(((Heightmap)cfg.heightmap.get()).vanilla(), candidate.method_10263(), candidate.method_10260());
                  candidate = new class_2338(candidate.method_10263(), top, candidate.method_10260());
               }

               double x = candidate.method_10263() + cfg.landingOffset.x();
               double y = candidate.method_10264() + cfg.landingOffset.y();
               double z = candidate.method_10260() + cfg.landingOffset.z();
               if (isValidLanding(candidate, x, y, z, cfg, entity, serverLevel) && TeleportHelper.teleport(entity, serverLevel, x, y, z) != null) {
                  cfg.successAction.ifPresent(a -> a.run(ctx));
                  return;
               }
            }
         }

         cfg.failAction.ifPresent(a -> a.run(ctx));
      }
   }

   private static boolean isValidLanding(class_2338 pos, double x, double y, double z, RandomTeleportAction.Cfg cfg, class_1297 entity, class_3218 level) {
      if (cfg.landingBlockCondition.isEmpty() && !level.method_8320(pos.method_10074()).method_51366()) {
         return false;
      } else {
         return cfg.landingCondition.isEmpty() && !level.method_8320(pos).method_26220(level, pos).method_1110()
            ? false
            : TeleportHelper.landingAllowed(entity, level, x, y, z, cfg.landingBlockCondition, cfg.landingCondition);
      }
   }

   public record Cfg(
      float areaWidth,
      float areaHeight,
      Optional<Heightmap> heightmap,
      Optional<Integer> attempts,
      Optional<BlockCondition> landingBlockCondition,
      Optional<EntityCondition> landingCondition,
      Vector landingOffset,
      boolean loadedChunksOnly,
      Optional<EntityAction> successAction,
      Optional<EntityAction> failAction
   ) {
   }
}
