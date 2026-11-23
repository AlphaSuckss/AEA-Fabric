package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.CameraPerspective;
import dev.alphasucks.aea.port.entity.CameraPerspectives;
import dev.alphasucks.aea.port.network.payload.SetPerspectiveS2C;
import net.minecraft.class_3222;

public final class SetPerspectiveAction implements ActionType<EntityCtx, SetPerspectiveAction.Cfg> {
   @Override
   public MapCodec<SetPerspectiveAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(CameraPerspective.CODEC.fieldOf("perspective").forGetter(SetPerspectiveAction.Cfg::perspective)).apply(i, SetPerspectiveAction.Cfg::new)
      );
   }

   public void run(SetPerspectiveAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         CameraPerspectives.setType(player.method_5667(), cfg.perspective().id());
         ApoliNetwork.sendPerspective(player, new SetPerspectiveS2C(cfg.perspective().id()));
      }
   }

   public record Cfg(CameraPerspective perspective) {
   }
}
