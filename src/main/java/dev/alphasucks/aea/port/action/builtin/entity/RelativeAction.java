package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_3542;

public final class RelativeAction implements ActionType<EntityCtx, RelativeAction.Cfg> {
   private final RelativeAction.Target defaultTarget;

   public RelativeAction(RelativeAction.Target defaultTarget) {
      this.defaultTarget = defaultTarget;
   }

   public RelativeAction() {
      this(RelativeAction.Target.PASSENGER);
   }

   @Override
   public MapCodec<RelativeAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               RelativeAction.Target.CODEC.optionalFieldOf("target", this.defaultTarget).forGetter(RelativeAction.Cfg::target),
               LoggedOptionalField.of("action", EntityAction.CODEC).forGetter(RelativeAction.Cfg::action),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(RelativeAction.Cfg::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(RelativeAction.Cfg::bientityCondition),
               Codec.BOOL.optionalFieldOf("recursive", false).forGetter(RelativeAction.Cfg::recursive)
            )
            .apply(i, RelativeAction.Cfg::new)
      );
   }

   public void run(RelativeAction.Cfg cfg, EntityCtx ctx) {
      class_1297 self = ctx.entity();
      class_1937 level = ctx.level();
      if (cfg.target == RelativeAction.Target.PASSENGER) {
         if (cfg.recursive) {
            for (class_1297 p : self.method_5736()) {
               fire(cfg, self, p, level);
            }
         } else {
            for (class_1297 p : self.method_5685()) {
               fire(cfg, self, p, level);
            }
         }
      } else {
         for (class_1297 vehicle = self.method_5854(); vehicle != null; vehicle = vehicle.method_5854()) {
            fire(cfg, self, vehicle, level);
            if (!cfg.recursive) {
               break;
            }
         }
      }
   }

   private static void fire(RelativeAction.Cfg cfg, class_1297 actor, class_1297 related, class_1937 level) {
      if (!cfg.bientityCondition.isPresent() || ((BiEntityCondition)cfg.bientityCondition.get()).test(new BiEntityCtx(actor, related, level))) {
         cfg.action.ifPresent(a -> a.run(new EntityCtx(related, level)));
         cfg.bientityAction.ifPresent(a -> a.run(new BiEntityCtx(actor, related, level)));
      }
   }

   public record Cfg(
      RelativeAction.Target target,
      Optional<EntityAction> action,
      Optional<BiEntityAction> bientityAction,
      Optional<BiEntityCondition> bientityCondition,
      boolean recursive
   ) {
   }

   public enum Target implements class_3542 {
      PASSENGER("passenger"),
      VEHICLE("vehicle");

      public static final Codec<RelativeAction.Target> CODEC = class_3542.method_28140(RelativeAction.Target::values);
      private final String name;

      Target(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
