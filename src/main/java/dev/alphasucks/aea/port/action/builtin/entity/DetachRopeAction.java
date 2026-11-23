package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.rope.RopeManager;
import java.util.Optional;
import net.minecraft.class_1297;

public final class DetachRopeAction implements ActionType<EntityCtx, DetachRopeAction.Cfg> {
   @Override
   public MapCodec<DetachRopeAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.STRING.optionalFieldOf("slot").forGetter(DetachRopeAction.Cfg::slot)).apply(i, DetachRopeAction.Cfg::new)
      );
   }

   public void run(DetachRopeAction.Cfg cfg, EntityCtx ctx) {
      class_1297 actor = ctx.raw();
      if (actor != null) {
         RopeManager.removeMatching(actor.method_5667(), (String)cfg.slot.orElse(null), null);
      }
   }

   public record Cfg(Optional<String> slot) {
   }
}
