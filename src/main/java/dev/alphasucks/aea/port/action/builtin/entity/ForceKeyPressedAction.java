package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.keybind.KeyDispatch;

public final class ForceKeyPressedAction implements ActionType<EntityCtx, ForceKeyPressedAction.Cfg> {
   @Override
   public MapCodec<ForceKeyPressedAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Key.CODEC.optionalFieldOf("key", Key.DEFAULT_PRIMARY).forGetter(ForceKeyPressedAction.Cfg::key),
               Codec.INT.optionalFieldOf("duration", 1).forGetter(ForceKeyPressedAction.Cfg::duration),
               Codec.BOOL.optionalFieldOf("release", false).forGetter(ForceKeyPressedAction.Cfg::release)
            )
            .apply(i, ForceKeyPressedAction.Cfg::new)
      );
   }

   public void run(ForceKeyPressedAction.Cfg cfg, EntityCtx ctx) {
      KeyDispatch.force(ctx.raw(), cfg.key.key(), cfg.duration, cfg.release);
   }

   public record Cfg(Key key, int duration, boolean release) {
   }
}
