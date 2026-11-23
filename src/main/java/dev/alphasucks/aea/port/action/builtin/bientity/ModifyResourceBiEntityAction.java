package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.builtin.entity.ModifyResourceAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.BiEntitySide;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1297;

public final class ModifyResourceBiEntityAction implements ActionType<BiEntityCtx, ModifyResourceBiEntityAction.Cfg> {
   @Override
   public MapCodec<ModifyResourceBiEntityAction.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  ModifyResourceAction.CONFIG_CODEC.forGetter(ModifyResourceBiEntityAction.Cfg::resource),
                  BiEntitySide.CODEC.optionalFieldOf("side", BiEntitySide.TARGET).forGetter(ModifyResourceBiEntityAction.Cfg::side),
                  LoggedOptionalField.strict("from_side", BiEntitySide.CODEC).forGetter(ModifyResourceBiEntityAction.Cfg::fromSide)
               )
               .apply(i, ModifyResourceBiEntityAction.Cfg::new)
         ),
         Map.of("recipient", "side", "from_entity", "from_side")
      );
   }

   public void run(ModifyResourceBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 recipient = cfg.side.of(ctx);
      if (recipient != null) {
         BiEntitySide fromSide = (BiEntitySide)cfg.fromSide.orElse(cfg.side.opposite());
         class_1297 source = fromSide.of(ctx);
         if (source == null) {
            source = recipient;
         }

         ModifyResourceAction.run(cfg.resource, recipient, source);
      }
   }

   public record Cfg(ModifyResourceAction.Cfg resource, BiEntitySide side, Optional<BiEntitySide> fromSide) {
   }
}
