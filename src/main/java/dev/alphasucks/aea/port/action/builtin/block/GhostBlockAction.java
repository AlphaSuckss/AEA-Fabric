package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.block.GhostBlocks;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Nbt;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2248;
import net.minecraft.class_2487;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_7923;

public final class GhostBlockAction implements ActionType<BlockCtx, GhostBlockAction.Cfg> {
   private static final MapCodec<GhostBlockAction.Cfg> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            IdCodecs.ID.fieldOf("block").forGetter(GhostBlockAction.Cfg::block),
            Nbt.CODEC.optionalFieldOf("nbt").forGetter(GhostBlockAction.Cfg::nbt),
            Codec.INT.optionalFieldOf("duration", 20).forGetter(GhostBlockAction.Cfg::duration),
            LoggedOptionalField.of("block_action", BlockAction.CODEC).forGetter(GhostBlockAction.Cfg::blockAction),
            LoggedOptionalField.of("end_action", BlockAction.CODEC).forGetter(GhostBlockAction.Cfg::endAction)
         )
         .apply(i, GhostBlockAction.Cfg::new)
   );

   @Override
   public MapCodec<GhostBlockAction.Cfg> codec() {
      return AliasingMapCodec.wrap(INNER, Map.of("tick", "duration"));
   }

   public void run(GhostBlockAction.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 level) {
         class_2248 block = class_7923.field_41175.method_10223(cfg.block);
         if (block != null) {
            class_2680 state = block.method_9564();
            GhostBlocks.place(
               level, ctx.pos(), state, (class_2487)cfg.nbt.map(n -> n.tag().method_10553()).orElse(null), cfg.duration, cfg.blockAction, cfg.endAction
            );
         }
      }
   }

   public record Cfg(class_2960 block, Optional<Nbt> nbt, int duration, Optional<BlockAction> blockAction, Optional<BlockAction> endAction) {
   }
}
