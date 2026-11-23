package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.access.ExplosionDamageToggle;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import java.util.Optional;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_1927;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3610;
import net.minecraft.class_5362;
import org.jetbrains.annotations.Nullable;

public final class ExplosionHelper {
   private static final float INDESTRUCTIBLE_RESISTANCE = 1000000.0F;

   private ExplosionHelper() {
   }

   public static void detonate(
      class_1937 level,
      @Nullable class_1297 source,
      class_243 pos,
      float power,
      boolean createFire,
      DestructionType destructionType,
      Optional<BlockCondition> indestructible,
      Optional<BlockCondition> destructible,
      boolean damageTargets,
      Optional<BiEntityCondition> targetCondition
   ) {
      if (!level.method_8608()) {
         class_1282 damageSource = level.method_48963().method_48819(source, source);
         class_5362 calculator = !indestructible.isPresent() && !destructible.isPresent()
            ? null
            : new ExplosionHelper.ConditionedCalculator(level, (BlockCondition)indestructible.orElse(null), (BlockCondition)destructible.orElse(null));
         class_1927 explosion = new class_1927(
            level, source, damageSource, calculator, pos.field_1352, pos.field_1351, pos.field_1350, power, createFire, destructionType.vanilla()
         );
         if (!damageTargets) {
            ((ExplosionDamageToggle)explosion).apoli$sparePassersby(source);
         }

         if (targetCondition.isPresent()) {
            BiEntityCondition condition = (BiEntityCondition)targetCondition.get();
            ((ExplosionDamageToggle)explosion).apoli$setDamageFilter(entity -> condition.test(BiEntityCtx.of(source, entity, level)));
         }

         explosion.method_8348();
         explosion.method_8350(true);
      }
   }

   private static final class ConditionedCalculator extends class_5362 {
      private final class_1937 level;
      @Nullable
      private final BlockCondition indestructible;
      @Nullable
      private final BlockCondition destructible;

      ConditionedCalculator(class_1937 level, @Nullable BlockCondition indestructible, @Nullable BlockCondition destructible) {
         this.level = level;
         this.indestructible = indestructible;
         this.destructible = destructible;
      }

      @Override
      public Optional<Float> method_29555(class_1927 explosion, class_1922 blocks, class_2338 pos, class_2680 state, class_3610 fluid) {
         BlockCtx ctx = new BlockCtx(pos.method_10062(), state, this.level);
         if (this.destructible != null && this.destructible.test(ctx)) {
            return super.method_29555(explosion, blocks, pos, state, fluid);
         } else if (this.indestructible != null && this.indestructible.test(ctx)) {
            return Optional.of(1000000.0F);
         } else {
            return this.destructible != null ? Optional.of(1000000.0F) : super.method_29555(explosion, blocks, pos, state, fluid);
         }
      }
   }
}
