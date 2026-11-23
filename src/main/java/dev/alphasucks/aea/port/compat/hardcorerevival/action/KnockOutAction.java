package dev.alphasucks.aea.port.compat.hardcorerevival.action;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import net.blay09.mods.hardcorerevival.api.HardcoreRevivalAPI;
import net.minecraft.class_1657;

public final class KnockOutAction implements ActionType<EntityCtx, KnockOutAction.Cfg> {
   @Override
   public MapCodec<KnockOutAction.Cfg> codec() {
      return MapCodec.unit(new KnockOutAction.Cfg());
   }

   public void run(KnockOutAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.raw() instanceof class_1657 player && !player.method_37908().method_8608()) {
         HardcoreRevivalAPI.knockout(player, player.method_48923().method_48830());
      }
   }

   public record Cfg() {
   }
}
