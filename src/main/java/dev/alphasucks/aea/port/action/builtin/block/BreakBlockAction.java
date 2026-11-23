package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.ItemStackData;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3610;
import net.minecraft.class_5712;
import net.minecraft.class_5712.class_7397;

public final class BreakBlockAction implements ActionType<BlockCtx, BreakBlockAction.Cfg> {
   @Override
   public MapCodec<BreakBlockAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("drop_loot", true).forGetter(BreakBlockAction.Cfg::dropLoot),
               LoggedOptionalField.strict("stack", ItemStackData.CODEC).forGetter(BreakBlockAction.Cfg::stack),
               Codec.BOOL.optionalFieldOf("break_particles", true).forGetter(BreakBlockAction.Cfg::breakParticles),
               Codec.BOOL.optionalFieldOf("as_player", false).forGetter(BreakBlockAction.Cfg::asPlayer),
               Codec.BOOL.optionalFieldOf("mine", false).forGetter(BreakBlockAction.Cfg::mine)
            )
            .apply(i, BreakBlockAction.Cfg::new)
      );
   }

   public void run(BreakBlockAction.Cfg cfg, BlockCtx ctx) {
      class_1937 level = ctx.level();
      if (!level.field_9236) {
         class_2338 pos = ctx.pos();
         class_2680 state = level.method_8320(pos);
         if (!state.method_26215()) {
            class_1297 actor = ctx.actor();
            if (cfg.mine && actor instanceof class_3222 miner) {
               if (level.method_8505(miner, pos)) {
                  miner.field_13974.method_14266(pos);
               }
            } else {
               class_3222 breaker = cfg.asPlayer && actor instanceof class_3222 player ? player : null;
               if (cfg.breakParticles) {
                  level.method_20290(2001, pos, class_2248.method_9507(state));
               }

               if (cfg.dropLoot) {
                  class_2586 blockEntity = state.method_31709() ? level.method_8321(pos) : null;
                  class_1799 tool = (class_1799)cfg.stack.map(ItemStackData::stack).map(class_1799::method_7972).orElse(class_1799.field_8037);
                  if (breaker != null) {
                     state.method_26204().method_9556(level, breaker, pos, state, blockEntity, tool);
                  } else {
                     class_2248.method_9511(state, level, pos, blockEntity, actor, tool);
                  }
               }

               class_3610 fluid = level.method_8316(pos);
               if (level.method_8652(pos, fluid.method_15759(), 3)) {
                  level.method_43276(class_5712.field_28165, pos, class_7397.method_43286(actor, state));
               }
            }
         }
      }
   }

   public record Cfg(boolean dropLoot, Optional<ItemStackData> stack, boolean breakParticles, boolean asPlayer, boolean mine) {
   }
}
