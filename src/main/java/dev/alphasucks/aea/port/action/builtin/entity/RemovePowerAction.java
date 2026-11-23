package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import net.minecraft.class_2960;

public final class RemovePowerAction implements ActionType<EntityCtx, RemovePowerAction.Cfg> {
   @Override
   public MapCodec<RemovePowerAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("power").forGetter(RemovePowerAction.Cfg::power)).apply(i, RemovePowerAction.Cfg::new)
      );
   }

   public void run(RemovePowerAction.Cfg cfg, EntityCtx ctx) {
      if (PowerContainer.of(ctx.entity()) instanceof PowerContainerImpl impl) {
         impl.removePowerCompletely(cfg.power);
      }
   }

   public record Cfg(class_2960 power) {
   }
}
