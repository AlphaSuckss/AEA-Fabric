package dev.alphasucks.aea.port.compat.hardcorerevival.condition;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import net.blay09.mods.hardcorerevival.api.HardcoreRevivalAPI;
import net.minecraft.class_1657;

public final class KnockedOutCondition implements ConditionType<EntityCtx, KnockedOutCondition.Cfg> {
   @Override
   public MapCodec<KnockedOutCondition.Cfg> codec() {
      return MapCodec.unit(new KnockedOutCondition.Cfg());
   }

   public boolean test(KnockedOutCondition.Cfg cfg, EntityCtx ctx) {
      return ctx.raw() instanceof class_1657 player && HardcoreRevivalAPI.isKnockedOut(player);
   }

   public record Cfg() {
   }
}
