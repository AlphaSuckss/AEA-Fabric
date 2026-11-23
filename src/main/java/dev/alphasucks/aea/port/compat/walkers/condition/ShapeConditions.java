package dev.alphasucks.aea.port.compat.walkers.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.compat.walkers.WalkersBridge;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1297;

public final class ShapeConditions {
   private ShapeConditions() {
   }

   public static final class CanUseShapeAbility implements ConditionType<EntityCtx, EmptyCfg> {
      @Override
      public MapCodec<EmptyCfg> codec() {
         return MapCodec.unit(EmptyCfg.INSTANCE);
      }

      public boolean test(EmptyCfg cfg, EntityCtx ctx) {
         return WalkersBridge.canUseAbility(ctx.raw());
      }
   }

   public static final class HasShapeAbility implements ConditionType<EntityCtx, EmptyCfg> {
      @Override
      public MapCodec<EmptyCfg> codec() {
         return MapCodec.unit(EmptyCfg.INSTANCE);
      }

      public boolean test(EmptyCfg cfg, EntityCtx ctx) {
         return WalkersBridge.hasShapeAbility(ctx.raw());
      }
   }

   public static final class Shape implements ConditionType<EntityCtx, ShapeConditions.Shape.Cfg> {
      @Override
      public MapCodec<ShapeConditions.Shape.Cfg> codec() {
         return RecordCodecBuilder.mapCodec(
            i -> i.group(BiEntityCondition.CODEC.fieldOf("bientity_condition").forGetter(ShapeConditions.Shape.Cfg::bientityCondition))
               .apply(i, ShapeConditions.Shape.Cfg::new)
         );
      }

      public boolean test(ShapeConditions.Shape.Cfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         return entity == null ? false : cfg.bientityCondition.test(new BiEntityCtx(entity, WalkersBridge.shapeOrSelf(entity), ctx.level()));
      }

      public record Cfg(BiEntityCondition bientityCondition) {
      }
   }

   public static final class ShapeAbilityCooldown implements ConditionType<EntityCtx, ShapeConditions.ShapeAbilityCooldown.Cfg> {
      @Override
      public MapCodec<ShapeConditions.ShapeAbilityCooldown.Cfg> codec() {
         return RecordCodecBuilder.mapCodec(
            i -> i.group(
                  Comparison.CODEC.fieldOf("comparison").forGetter(ShapeConditions.ShapeAbilityCooldown.Cfg::comparison),
                  Codec.INT.fieldOf("compare_to").forGetter(ShapeConditions.ShapeAbilityCooldown.Cfg::compareTo)
               )
               .apply(i, ShapeConditions.ShapeAbilityCooldown.Cfg::new)
         );
      }

      public boolean test(ShapeConditions.ShapeAbilityCooldown.Cfg cfg, EntityCtx ctx) {
         class_1297 entity = ctx.raw();
         return entity == null ? false : cfg.comparison.compare(WalkersBridge.abilityCooldown(entity), cfg.compareTo);
      }

      public record Cfg(Comparison comparison, int compareTo) {
      }
   }
}
