package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ReceiveActionPower extends PowerType<ReceiveActionPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("receive_action");

   @Override
   public MapCodec<ReceiveActionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("action", EntityAction.CODEC).forGetter(ReceiveActionPower.Config::action),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ReceiveActionPower.Config::bientityAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ReceiveActionPower.Config::bientityCondition),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ReceiveActionPower.Config::entityAction),
               LoggedOptionalField.strict("entity_condition", EntityCondition.CODEC).forGetter(ReceiveActionPower.Config::entityCondition),
               LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(ReceiveActionPower.Config::itemAction),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ReceiveActionPower.Config::itemCondition)
            )
            .apply(i, ReceiveActionPower.Config::new)
      );
   }

   @Nullable
   public static ReceiveActionPower.Config resolve(@Nullable class_1297 receiverHolder, class_2960 receiver) {
      if (receiverHolder == null) {
         return null;
      }

      PowerContainer container = PowerContainer.of(receiverHolder);
      if (container != null && container.hasPower(receiver) && !container.isSuppressed(receiver)) {
         Power loaded = ApoliPowers.get(receiver);
         if (!(loaded != null && loaded.config() instanceof ReceiveActionPower.Config cfg)) {
            return null;
         } else if (!(PowerTypeRegistry.get(loaded.typeId()) instanceof ReceiveActionPower)) {
            return null;
         } else {
            return loaded.condition().isPresent()
                  && !((EntityCondition)loaded.condition().get()).test(EntityCtx.of(receiverHolder, receiverHolder.method_37908()))
               ? null
               : cfg;
         }
      } else {
         return null;
      }
   }

   public static void receiveEntity(class_1297 holder, class_2960 receiver, EntityCtx ctx) {
      ReceiveActionPower.Config cfg = resolve(holder, receiver);
      if (cfg != null) {
         if (!cfg.entityCondition.isPresent() || ((EntityCondition)cfg.entityCondition.get()).test(ctx)) {
            cfg.entityAction.ifPresent(a -> a.run(ctx));
            cfg.action.ifPresent(a -> a.run(EntityCtx.of(holder, holder.method_37908())));
         }
      }
   }

   public static void receiveBiEntity(class_1297 holder, class_2960 receiver, BiEntityCtx ctx) {
      ReceiveActionPower.Config cfg = resolve(holder, receiver);
      if (cfg != null) {
         if (!cfg.bientityCondition.isPresent() || ((BiEntityCondition)cfg.bientityCondition.get()).test(ctx)) {
            cfg.bientityAction.ifPresent(a -> a.run(ctx));
            cfg.action.ifPresent(a -> a.run(EntityCtx.of(holder, holder.method_37908())));
         }
      }
   }

   public static void receiveItem(class_1297 holder, class_2960 receiver, ItemCtx ctx) {
      ReceiveActionPower.Config cfg = resolve(holder, receiver);
      if (cfg != null) {
         if (!cfg.itemCondition.isPresent() || ((ItemCondition)cfg.itemCondition.get()).test(ctx)) {
            cfg.itemAction.ifPresent(a -> a.run(ctx));
            cfg.action.ifPresent(a -> a.run(EntityCtx.of(holder, holder.method_37908())));
         }
      }
   }

   public record Config(
      Optional<EntityAction> action,
      Optional<BiEntityAction> bientityAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<EntityAction> entityAction,
      Optional<EntityCondition> entityCondition,
      Optional<ItemAction> itemAction,
      Optional<ItemCondition> itemCondition
   ) {
   }
}
