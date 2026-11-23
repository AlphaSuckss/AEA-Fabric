package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_1267;
import net.minecraft.class_1937;
import net.minecraft.class_3542;

public final class DifficultyCondition implements ConditionType<EntityCtx, DifficultyCondition.Cfg> {
   private static final Codec<List<DifficultyCondition.Level_>> LIST_OR_SINGLE = Codec.either(
         DifficultyCondition.Level_.CODEC, DifficultyCondition.Level_.CODEC.listOf()
      )
      .xmap(
         either -> (List)either.map(List::of, list -> list),
         list -> list.size() == 1 ? Either.left((DifficultyCondition.Level_)list.get(0)) : Either.right(list)
      );

   @Override
   public MapCodec<DifficultyCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LIST_OR_SINGLE.fieldOf("difficulty").forGetter(DifficultyCondition.Cfg::difficulties)).apply(i, DifficultyCondition.Cfg::new)
      );
   }

   public boolean test(DifficultyCondition.Cfg cfg, EntityCtx ctx) {
      class_1937 level = ctx.level();
      if (level == null) {
         return false;
      }

      class_1267 current = level.method_8407();

      for (int i = 0; i < cfg.difficulties.size(); i++) {
         if (((DifficultyCondition.Level_)cfg.difficulties.get(i)).vanilla() == current) {
            return true;
         }
      }

      return false;
   }

   public record Cfg(List<DifficultyCondition.Level_> difficulties) {
   }

   public enum Level_ implements class_3542 {
      PEACEFUL(class_1267.field_5801),
      EASY(class_1267.field_5805),
      NORMAL(class_1267.field_5802),
      HARD(class_1267.field_5807);

      public static final Codec<DifficultyCondition.Level_> CODEC = class_3542.method_28140(DifficultyCondition.Level_::values);
      private final class_1267 vanilla;

      Level_(class_1267 vanilla) {
         this.vanilla = vanilla;
      }

      public class_1267 vanilla() {
         return this.vanilla;
      }

      @Override
      public String method_15434() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
