package dev.alphasucks.aea.port.action.builtin.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import java.util.Optional;
import net.minecraft.class_1309;

public final class HolderActionItemAction implements ActionType<ItemCtx, HolderActionItemAction.Cfg> {
   @Override
   public MapCodec<HolderActionItemAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("action", EntityAction.CODEC).forGetter(HolderActionItemAction.Cfg::action),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(HolderActionItemAction.Cfg::entityAction)
            )
            .apply(i, HolderActionItemAction.Cfg::new)
      );
   }

   public void run(HolderActionItemAction.Cfg cfg, ItemCtx ctx) {
      class_1309 holder = ctx.holder();
      if (holder != null) {
         EntityAction action = (EntityAction)cfg.entityAction.orElse((EntityAction)cfg.action.orElse(null));
         if (action != null) {
            action.run(new EntityCtx(holder, ctx.level()));
         }
      }
   }

   public record Cfg(Optional<EntityAction> action, Optional<EntityAction> entityAction) {
   }
}
