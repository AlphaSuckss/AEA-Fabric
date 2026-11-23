package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.builtin.entity.PlaySoundAction.Category;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.sound.SoundTracker;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3419;

public final class SoundPlayingCondition implements ConditionType<EntityCtx, SoundPlayingCondition.Cfg> {
   @Override
   public MapCodec<SoundPlayingCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("sound").forGetter(SoundPlayingCondition.Cfg::sound),
               Category.CODEC.optionalFieldOf("category").forGetter(SoundPlayingCondition.Cfg::category),
               Codec.INT.optionalFieldOf("duration", 20).forGetter(SoundPlayingCondition.Cfg::duration),
               Codec.DOUBLE.optionalFieldOf("range", -1.0).forGetter(SoundPlayingCondition.Cfg::range)
            )
            .apply(i, SoundPlayingCondition.Cfg::new)
      );
   }

   public boolean test(SoundPlayingCondition.Cfg cfg, EntityCtx ctx) {
      return !SoundTracker.watching() ? false : SoundTracker.playing(ctx.raw(), cfg.sound, cfg.source(), Math.max(1, cfg.duration), cfg.range);
   }

   public record Cfg(class_2960 sound, Optional<Category> category, int duration, double range) {
      public Cfg {
         SoundTracker.watch(sound);
      }

      public class_3419 source() {
         return (class_3419)this.category.map(Category::vanilla).orElse(null);
      }
   }
}
