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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1296;
import net.minecraft.class_1429;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class ActionOnBreedPower extends PowerType<ActionOnBreedPower.Config> {
   private static final List<class_2960> RUNNING = new ArrayList(2);

   @Override
   public MapCodec<ActionOnBreedPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnBreedPower.Config::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnBreedPower.Config::bientityCondition),
               LoggedOptionalField.of("parents_action", BiEntityAction.CODEC).forGetter(ActionOnBreedPower.Config::parentsAction)
            )
            .apply(i, ActionOnBreedPower.Config::new)
      );
   }

   public static void fire(class_1429 parent, class_1429 mate, class_1296 baby) {
      class_3222 breeder = parent.method_6478();
      if (breeder == null) {
         breeder = mate.method_6478();
      }

      if (breeder != null && PowerLookup.holdsType(breeder, ApoliIds.ACTION_ON_BREED)) {
         BiEntityCtx babyCtx = BiEntityCtx.of(breeder, baby, parent.method_37908());
         PowerLookup.forEachEntry(breeder, ApoliIds.ACTION_ON_BREED, ActionOnBreedPower.Config.class, (powerId, cfg) -> {
            if (!RUNNING.contains(powerId)) {
               if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(babyCtx)) {
                  RUNNING.add(powerId);

                  try {
                     cfg.bientityAction().ifPresent(action -> action.run(babyCtx));
                     cfg.parentsAction().ifPresent(action -> action.run(BiEntityCtx.of(parent, mate, parent.method_37908())));
                  } finally {
                     RUNNING.remove(RUNNING.size() - 1);
                  }
               }
            }
         });
      }
   }

   public record Config(Optional<BiEntityAction> bientityAction, Optional<BiEntityCondition> bientityCondition, Optional<BiEntityAction> parentsAction) {
   }
}
