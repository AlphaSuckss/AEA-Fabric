package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.SetIteration;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;

public final class ActionOnEntitySetAction implements ActionType<EntityCtx, ActionOnEntitySetAction.Cfg> {
   @Override
   public MapCodec<ActionOnEntitySetAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("set").forGetter(ActionOnEntitySetAction.Cfg::set),
               BiEntityAction.CODEC.fieldOf("bientity_action").forGetter(ActionOnEntitySetAction.Cfg::biEntityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnEntitySetAction.Cfg::biEntityCondition),
               Codec.INT.optionalFieldOf("limit", 0).forGetter(ActionOnEntitySetAction.Cfg::limit),
               Codec.BOOL.optionalFieldOf("reverse", false).forGetter(ActionOnEntitySetAction.Cfg::reverse),
               SetIteration.CODEC.optionalFieldOf("iterate", SetIteration.MEMBERS).forGetter(ActionOnEntitySetAction.Cfg::iterate),
               Codec.BOOL.optionalFieldOf("include_offline", false).forGetter(ActionOnEntitySetAction.Cfg::includeOffline)
            )
            .apply(i, ActionOnEntitySetAction.Cfg::new)
      );
   }

   public void run(ActionOnEntitySetAction.Cfg cfg, EntityCtx ctx) {
      class_1297 holder = ctx.entity();
      if (ctx.level() instanceof class_3218 serverLevel) {
         MinecraftServer var12 = serverLevel.method_8503();
         if (EntitySetPower.resolveCfg(cfg.set) != null) {
            if (cfg.iterate == SetIteration.OWNERS) {
               runOnOwners(cfg, holder, serverLevel, var12);
            } else {
               PowerContainer container = PowerContainer.of(holder);
               if (container != null && container.hasPower(cfg.set)) {
                  List<UUID> order = EntitySetPower.iterationOrder(holder, cfg.set, cfg.reverse);
                  int processed = 0;

                  for (int i = 0; i < order.size(); i++) {
                     class_1297 target = EntitySetPower.resolveEntity(var12, (UUID)order.get(i), cfg.includeOffline);
                     if (target != null) {
                        BiEntityCtx biCtx = new BiEntityCtx(holder, target, serverLevel);
                        if (!cfg.biEntityCondition.isPresent() || ((BiEntityCondition)cfg.biEntityCondition.get()).test(biCtx)) {
                           cfg.biEntityAction.run(biCtx);
                           if (cfg.limit > 0) {
                              if (++processed >= cfg.limit) {
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void runOnOwners(ActionOnEntitySetAction.Cfg cfg, class_1297 holder, class_3218 level, MinecraftServer server) {
      List<UUID> owners = EntitySetPower.ownersContaining(holder, cfg.set, cfg.reverse);
      int processed = 0;

      for (int i = 0; i < owners.size(); i++) {
         class_1297 owner = EntitySetPower.resolveEntity(server, (UUID)owners.get(i), cfg.includeOffline);
         if (owner != null) {
            BiEntityCtx biCtx = new BiEntityCtx(owner, holder, level);
            if (!cfg.biEntityCondition.isPresent() || ((BiEntityCondition)cfg.biEntityCondition.get()).test(biCtx)) {
               cfg.biEntityAction.run(biCtx);
               if (cfg.limit > 0) {
                  if (++processed >= cfg.limit) {
                     break;
                  }
               }
            }
         }
      }
   }

   public record Cfg(
      class_2960 set,
      BiEntityAction biEntityAction,
      Optional<BiEntityCondition> biEntityCondition,
      int limit,
      boolean reverse,
      SetIteration iterate,
      boolean includeOffline
   ) {
   }
}
