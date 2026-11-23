package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;

public final class DisguiseBiEntityAction implements ActionType<BiEntityCtx, DisguiseBiEntityAction.Cfg> {
   @Override
   public MapCodec<DisguiseBiEntityAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("overwrite", true).forGetter(DisguiseBiEntityAction.Cfg::overwrite),
               Codec.BOOL.optionalFieldOf("change_name", true).forGetter(DisguiseBiEntityAction.Cfg::changeName)
            )
            .apply(i, DisguiseBiEntityAction.Cfg::new)
      );
   }

   public void run(DisguiseBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      if (ctx.actor() != null && ctx.target() != null) {
         DisguiseManager.applyAs(ctx.actor(), ctx.target(), cfg.overwrite, cfg.changeName);
      }
   }

   public record Cfg(boolean overwrite, boolean changeName) {
   }
}
