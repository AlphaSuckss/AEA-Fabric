package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;

public final class ActionOnTamePower extends PowerType<ActionOnTamePower.Config> {
   @Override
   public MapCodec<ActionOnTamePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnTamePower.Config::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnTamePower.Config::bientityCondition)
            )
            .apply(i, ActionOnTamePower.Config::new)
      );
   }

   public static void fire(class_1657 tamer, class_1297 animal) {
      if (!tamer.method_37908().method_8608() && PowerLookup.holdsType(tamer, ApoliIds.ACTION_ON_TAME)) {
         BiEntityCtx ctx = BiEntityCtx.of(tamer, animal, tamer.method_37908());
         PowerLookup.forEach(tamer, ApoliIds.ACTION_ON_TAME, ActionOnTamePower.Config.class, cfg -> {
            if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
               cfg.bientityAction().ifPresent(action -> action.run(ctx));
            }
         });
      }
   }

   public record Config(Optional<BiEntityAction> bientityAction, Optional<BiEntityCondition> bientityCondition) {
   }
}
