package dev.alphasucks.aea.port.compat.hardcorerevival.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import net.blay09.mods.hardcorerevival.api.HardcoreRevivalAPI;
import net.minecraft.class_1657;

public final class ReviveAction implements ActionType<EntityCtx, ReviveAction.Cfg> {
   @Override
   public MapCodec<ReviveAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.BOOL.optionalFieldOf("apply_effects", true).forGetter(ReviveAction.Cfg::applyEffects)).apply(i, ReviveAction.Cfg::new)
      );
   }

   public void run(ReviveAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.raw() instanceof class_1657 player && !player.method_37908().method_8608()) {
         HardcoreRevivalAPI.wakeup(player, cfg.applyEffects());
      }
   }

   public record Cfg(boolean applyEffects) {
   }
}
