package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import net.minecraft.class_2960;

public final class RemoveFromEntitySetAction implements ActionType<BiEntityCtx, RemoveFromEntitySetAction.Cfg> {
   @Override
   public MapCodec<RemoveFromEntitySetAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("set").forGetter(RemoveFromEntitySetAction.Cfg::set)).apply(i, RemoveFromEntitySetAction.Cfg::new)
      );
   }

   public void run(RemoveFromEntitySetAction.Cfg cfg, BiEntityCtx ctx) {
      if (ctx.actor() != null && ctx.target() != null) {
         PowerContainer container = PowerContainer.of(ctx.actor());
         if (container != null && container.hasPower(cfg.set)) {
            EntitySetPower.Cfg setCfg = EntitySetPower.resolveCfg(cfg.set);
            if (setCfg != null) {
               EntitySetPower.remove(ctx.actor(), cfg.set, setCfg, ctx.target());
            }
         }
      }
   }

   public record Cfg(class_2960 set) {
   }
}
