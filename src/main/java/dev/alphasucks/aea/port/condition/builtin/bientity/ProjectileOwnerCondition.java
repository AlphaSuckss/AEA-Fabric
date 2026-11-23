package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1297;
import net.minecraft.class_1676;

public final class ProjectileOwnerCondition implements ConditionType<BiEntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public boolean test(EmptyCfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      if (actor != null && ctx.target() instanceof class_1676 projectile) {
         class_1297 owner = projectile.method_24921();
         return owner != null && owner.equals(actor);
      } else {
         return false;
      }
   }
}
