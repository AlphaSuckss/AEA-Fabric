package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.SavedLocations;
import dev.alphasucks.aea.port.entity.SavedLocations.Location;
import net.minecraft.class_1297;
import net.minecraft.class_3218;

public final class SaveLocationAction implements ActionType<EntityCtx, SaveLocationAction.Cfg> {
   @Override
   public MapCodec<SaveLocationAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.fieldOf("id").forGetter(SaveLocationAction.Cfg::id),
               Codec.BOOL.optionalFieldOf("overwrite", true).forGetter(SaveLocationAction.Cfg::overwrite)
            )
            .apply(i, SaveLocationAction.Cfg::new)
      );
   }

   public void run(SaveLocationAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && entity.method_37908() instanceof class_3218 level) {
         SavedLocations locations = SavedLocations.of(entity);
         if (locations != null) {
            locations.put(
               entity.method_5667(),
               cfg.id,
               new Location(
                  entity.method_23317(), entity.method_23318(), entity.method_23321(), level.method_27983(), entity.method_36454(), entity.method_36455()
               ),
               cfg.overwrite
            );
         }
      }
   }

   public record Cfg(String id, boolean overwrite) {
   }
}
