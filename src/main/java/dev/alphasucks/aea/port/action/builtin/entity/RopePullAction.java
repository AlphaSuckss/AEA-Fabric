package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.compat.sable.SableSubLevels;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.network.VelocityUpdater;
import dev.alphasucks.aea.port.rope.Rope;
import dev.alphasucks.aea.port.rope.RopeAnchor;
import dev.alphasucks.aea.port.rope.RopeManager;
import dev.alphasucks.aea.port.rope.RopeAnchor.OfSubLevel;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_3542;

public final class RopePullAction implements ActionType<EntityCtx, RopePullAction.Cfg> {
   @Override
   public MapCodec<RopePullAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("slot").forGetter(RopePullAction.Cfg::slot),
               RopePullAction.Which.CODEC.optionalFieldOf("which", RopePullAction.Which.SELF).forGetter(RopePullAction.Cfg::which),
               Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(RopePullAction.Cfg::speed),
               Codec.BOOL.optionalFieldOf("set", false).forGetter(RopePullAction.Cfg::set),
               Codec.DOUBLE.optionalFieldOf("reel", 0.0).forGetter(RopePullAction.Cfg::reel),
               Codec.DOUBLE.optionalFieldOf("sublevel_force", 1.0).forGetter(RopePullAction.Cfg::subLevelForce)
            )
            .apply(i, RopePullAction.Cfg::new)
      );
   }

   public void run(RopePullAction.Cfg cfg, EntityCtx ctx) {
      class_1297 actor = ctx.raw();
      class_1937 level = ctx.level();
      if (actor != null) {
         for (Rope rope : RopeManager.forOwner(actor.method_5667())) {
            if (!cfg.slot.isPresent() || ((String)cfg.slot.get()).equals(rope.slot)) {
               class_243 fromPos = rope.from.position(level);
               class_243 toPos = rope.to.position(level);
               if (fromPos != null && toPos != null) {
                  boolean actorIsFrom = actor.method_5667().equals(rope.fromEntity);
                  boolean actorIsTo = actor.method_5667().equals(rope.toEntity);
                  if (actorIsFrom || actorIsTo) {
                     class_243 actorPos = actorIsFrom ? fromPos : toPos;
                     class_243 otherPos = actorIsFrom ? toPos : fromPos;
                     RopeAnchor otherAnchor = actorIsFrom ? rope.to : rope.from;
                     class_1297 otherEntity = otherAnchor.entity(level);
                     if (cfg.which != RopePullAction.Which.OTHER) {
                        push(actor, otherPos.method_1020(actorPos), cfg.speed, cfg.set);
                     }

                     if (cfg.which != RopePullAction.Which.SELF) {
                        if (otherEntity instanceof class_1309) {
                           push(otherEntity, actorPos.method_1020(otherPos), cfg.speed, cfg.set);
                        } else if (otherAnchor instanceof OfSubLevel subLevel) {
                           SableSubLevels.pull(level, subLevel.subLevel(), subLevel.local(), actorPos.method_1020(otherPos), cfg.speed, cfg.subLevelForce);
                        }
                     }

                     if (cfg.reel != 0.0) {
                        RopeManager.reel(rope.id, cfg.reel);
                     }
                  }
               }
            }
         }
      }
   }

   private static void push(class_1297 entity, class_243 toward, double speed, boolean set) {
      if (speed != 0.0 && !(toward.method_1027() < 1.0E-8)) {
         VelocityUpdater.apply(entity, toward.method_1029().method_1021(speed), set);
      }
   }

   public record Cfg(Optional<String> slot, RopePullAction.Which which, double speed, boolean set, double reel, double subLevelForce) {
   }

   public enum Which implements class_3542 {
      SELF("self"),
      OTHER("other"),
      BOTH("both");

      public static final Codec<RopePullAction.Which> CODEC = class_3542.method_28140(RopePullAction.Which::values);
      private final String name;

      Which(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
