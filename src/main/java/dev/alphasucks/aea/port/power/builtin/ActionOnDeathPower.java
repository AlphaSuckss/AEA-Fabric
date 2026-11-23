package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class ActionOnDeathPower extends PowerType<ActionOnDeathPower.Config> {
   @Override
   public MapCodec<ActionOnDeathPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               BiEntityAction.CODEC.fieldOf("bientity_action").forGetter(ActionOnDeathPower.Config::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnDeathPower.Config::bientityCondition),
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(ActionOnDeathPower.Config::damageCondition)
            )
            .apply(i, ActionOnDeathPower.Config::new)
      );
   }

   public void onDeath(ActionOnDeathPower.Config cfg, class_1309 holder, @Nullable class_1309 killer, class_1282 source, class_3218 level) {
      if (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(new DamageCtx(source, holder, level, 0.0F))) {
         class_1309 actor = killer != null ? killer : holder;
         BiEntityCtx ctx = new BiEntityCtx(actor, holder, level);
         if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
            cfg.bientityAction().run(ctx);
         }
      }
   }

   public record Config(BiEntityAction bientityAction, Optional<BiEntityCondition> bientityCondition, Optional<DamageCondition> damageCondition) {
   }
}
