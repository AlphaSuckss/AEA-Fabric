package dev.alphasucks.aea.port.compat.hardcorerevival.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_2960;

public final class ActionOnKnockoutPower extends PowerType<ActionOnKnockoutPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("action_on_knockout");

   @Override
   public MapCodec<ActionOnKnockoutPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnKnockoutPower.Config::entityAction))
            .apply(i, ActionOnKnockoutPower.Config::new)
      );
   }

   public static void handle(class_1657 player) {
      if (!player.method_37908().method_8608()) {
         EntityCtx ctx = EntityCtx.of(player, player.method_37908());
         PowerLookup.forEach(player, CANONICAL, ActionOnKnockoutPower.Config.class, cfg -> cfg.entityAction().ifPresent(a -> a.run(ctx)));
      }
   }

   public record Config(Optional<EntityAction> entityAction) {
   }
}
