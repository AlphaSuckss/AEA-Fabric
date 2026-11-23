package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.DestructionType;
import dev.alphasucks.aea.port.data.ExplosionHelper;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_243;

public final class ExplodeBiEntityAction implements ActionType<BiEntityCtx, ExplodeBiEntityAction.Cfg> {
   @Override
   public MapCodec<ExplodeBiEntityAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.FLOAT.fieldOf("power").forGetter(ExplodeBiEntityAction.Cfg::power),
               DestructionType.CODEC.optionalFieldOf("destruction_type", DestructionType.DESTROY).forGetter(ExplodeBiEntityAction.Cfg::destructionType),
               Codec.BOOL.optionalFieldOf("create_fire", false).forGetter(ExplodeBiEntityAction.Cfg::createFire),
               Codec.BOOL.optionalFieldOf("at_target", false).forGetter(ExplodeBiEntityAction.Cfg::atTarget),
               Codec.BOOL.optionalFieldOf("damage_self", true).forGetter(ExplodeBiEntityAction.Cfg::damageSelf),
               Codec.BOOL.optionalFieldOf("damage_targets", true).forGetter(ExplodeBiEntityAction.Cfg::damageTargets),
               LoggedOptionalField.strict("indestructible", BlockCondition.CODEC).forGetter(ExplodeBiEntityAction.Cfg::indestructible),
               LoggedOptionalField.strict("destructible", BlockCondition.CODEC).forGetter(ExplodeBiEntityAction.Cfg::destructible),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ExplodeBiEntityAction.Cfg::bientityCondition)
            )
            .apply(i, ExplodeBiEntityAction.Cfg::new)
      );
   }

   public void run(ExplodeBiEntityAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 origin = cfg.atTarget ? ctx.target() : actor;
      if (origin != null) {
         class_243 pos = origin.method_19538();
         boolean spareActor = !cfg.damageSelf && actor != null;
         boolean wasInvulnerable = actor != null && actor.method_5655();
         if (spareActor) {
            actor.method_5684(true);
         }

         try {
            ExplosionHelper.detonate(
               ctx.level(),
               actor,
               pos,
               cfg.power,
               cfg.createFire,
               cfg.destructionType,
               cfg.indestructible,
               cfg.destructible,
               cfg.damageTargets,
               cfg.bientityCondition
            );
         } finally {
            if (spareActor) {
               actor.method_5684(wasInvulnerable);
            }
         }
      }
   }

   public record Cfg(
      float power,
      DestructionType destructionType,
      boolean createFire,
      boolean atTarget,
      boolean damageSelf,
      boolean damageTargets,
      Optional<BlockCondition> indestructible,
      Optional<BlockCondition> destructible,
      Optional<BiEntityCondition> bientityCondition
   ) {
   }
}
