package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.data.Vector;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3730;
import net.minecraft.class_7923;

public final class SpawnEntityBlockAction implements ActionType<BlockCtx, SpawnEntityBlockAction.Cfg> {
   @Override
   public MapCodec<SpawnEntityBlockAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("entity_type").forGetter(SpawnEntityBlockAction.Cfg::entityType),
               Nbt.CODEC.optionalFieldOf("tag").forGetter(SpawnEntityBlockAction.Cfg::tag),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(SpawnEntityBlockAction.Cfg::entityAction),
               Vector.CODEC.optionalFieldOf("offset", Vector.ZERO).forGetter(SpawnEntityBlockAction.Cfg::offset)
            )
            .apply(i, SpawnEntityBlockAction.Cfg::new)
      );
   }

   public void run(SpawnEntityBlockAction.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 serverLevel) {
         class_1299<?> type = class_7923.field_41177.method_10223(cfg.entityType);
         if (type != null) {
            class_243 pos = class_243.method_24955(ctx.pos()).method_1031(cfg.offset.x(), cfg.offset.y(), cfg.offset.z());
            class_2487 tag = (class_2487)cfg.tag.map(n -> n.tag().method_10553()).orElseGet(class_2487::new);
            tag.method_10582("id", cfg.entityType.toString());
            class_1297 entity = class_1299.method_17842(tag, serverLevel, loaded -> {
               loaded.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, loaded.method_36454(), loaded.method_36455());
               return loaded;
            });
            if (entity != null) {
               if (entity instanceof class_1308 mob) {
                  mob.method_5943(serverLevel, serverLevel.method_8404(ctx.pos()), class_3730.field_16471, null, null);
               }

               serverLevel.method_8649(entity);
               if (cfg.entityAction.isPresent()) {
                  ((EntityAction)cfg.entityAction.get()).run(new EntityCtx(entity, serverLevel));
               }
            }
         }
      }
   }

   public record Cfg(class_2960 entityType, Optional<Nbt> tag, Optional<EntityAction> entityAction, Vector offset) {
   }
}
