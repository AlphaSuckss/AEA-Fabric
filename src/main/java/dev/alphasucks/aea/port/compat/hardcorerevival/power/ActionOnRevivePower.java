package dev.alphasucks.aea.port.compat.hardcorerevival.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_2960;

public final class ActionOnRevivePower extends PowerType<ActionOnRevivePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("action_on_revive");

   @Override
   public MapCodec<ActionOnRevivePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnRevivePower.Config::entityAction),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnRevivePower.Config::bientityAction)
            )
            .apply(i, ActionOnRevivePower.Config::new)
      );
   }

   public static void handleRevived(class_1657 player) {
      if (!player.method_37908().method_8608()) {
         EntityCtx ctx = EntityCtx.of(player, player.method_37908());
         PowerLookup.forEach(player, CANONICAL, ActionOnRevivePower.Config.class, cfg -> cfg.entityAction().ifPresent(a -> a.run(ctx)));
      }
   }

   public static void handleRescued(class_1657 player, class_1657 rescuer) {
      if (!player.method_37908().method_8608()) {
         BiEntityCtx ctx = new BiEntityCtx(rescuer, player, player.method_37908());
         PowerLookup.forEach(player, CANONICAL, ActionOnRevivePower.Config.class, cfg -> cfg.bientityAction().ifPresent(a -> a.run(ctx)));
      }
   }

   public record Config(Optional<EntityAction> entityAction, Optional<BiEntityAction> bientityAction) {
   }
}
