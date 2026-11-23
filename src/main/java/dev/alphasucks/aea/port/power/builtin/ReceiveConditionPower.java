package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
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

public final class ReceiveConditionPower extends PowerType<ReceiveConditionPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("receive_condition");

   @Override
   public MapCodec<ReceiveConditionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ReceiveConditionPower.Config::bientityCondition),
               LoggedOptionalField.strict("entity_condition", EntityCondition.CODEC).forGetter(ReceiveConditionPower.Config::entityCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ReceiveConditionPower.Config::itemCondition)
            )
            .apply(i, ReceiveConditionPower.Config::new)
      );
   }

   @Nullable
   private static ReceiveConditionPower.Config resolve(@Nullable class_1297 holder, class_2960 receiver) {
      if (holder == null) {
         return null;
      }

      PowerContainer container = PowerContainer.of(holder);
      if (container != null && container.hasPower(receiver) && !container.isSuppressed(receiver)) {
         Power loaded = ApoliPowers.get(receiver);
         if (!(loaded != null && loaded.config() instanceof ReceiveConditionPower.Config cfg)) {
            return null;
         } else if (!(PowerTypeRegistry.get(loaded.typeId()) instanceof ReceiveConditionPower)) {
            return null;
         } else {
            return loaded.condition().isPresent() && !((EntityCondition)loaded.condition().get()).test(EntityCtx.of(holder, holder.method_37908()))
               ? null
               : cfg;
         }
      } else {
         return null;
      }
   }

   public static boolean testEntity(class_1297 holder, class_2960 receiver, EntityCtx ctx) {
      ReceiveConditionPower.Config cfg = resolve(holder, receiver);
      return cfg != null && (cfg.entityCondition.isEmpty() || ((EntityCondition)cfg.entityCondition.get()).test(ctx));
   }

   public static boolean testBiEntity(class_1297 holder, class_2960 receiver, BiEntityCtx ctx) {
      ReceiveConditionPower.Config cfg = resolve(holder, receiver);
      return cfg != null && (cfg.bientityCondition.isEmpty() || ((BiEntityCondition)cfg.bientityCondition.get()).test(ctx));
   }

   public static boolean testItem(class_1297 holder, class_2960 receiver, ItemCtx ctx) {
      ReceiveConditionPower.Config cfg = resolve(holder, receiver);
      return cfg != null && (cfg.itemCondition.isEmpty() || ((ItemCondition)cfg.itemCondition.get()).test(ctx));
   }

   public record Config(Optional<BiEntityCondition> bientityCondition, Optional<EntityCondition> entityCondition, Optional<ItemCondition> itemCondition) {
   }
}
