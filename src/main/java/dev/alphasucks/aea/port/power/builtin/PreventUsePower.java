package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.alias.AliasDefault;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;

public final class PreventUsePower extends PowerType<PreventUsePower.Config> {
   @Override
   public MapCodec<PreventUsePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(PreventUsePower.Config::bientityAction),
               LoggedOptionalField.of("held_item_action", ItemAction.CODEC).forGetter(PreventUsePower.Config::heldItemAction),
               LoggedOptionalField.of("result_item_action", ItemAction.CODEC).forGetter(PreventUsePower.Config::resultItemAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(PreventUsePower.Config::bientityCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(PreventUsePower.Config::itemCondition),
               Hand.LIST_CODEC.optionalFieldOf("hands", Hand.BOTH).forGetter(PreventUsePower.Config::hands),
               ItemStackData.CODEC.optionalFieldOf("result_stack").forGetter(PreventUsePower.Config::resultStack),
               Codec.BOOL.optionalFieldOf("target_used", false).forGetter(PreventUsePower.Config::targetUsed)
            )
            .apply(i, PreventUsePower.Config::new)
      );
   }

   public boolean tryPrevent(PreventUsePower.Config cfg, class_1657 actor, class_1297 target, class_1268 hand, boolean runActions) {
      if (!BlockInteractionHelper.handMatches(cfg.hands, hand)) {
         return false;
      }

      class_1799 stack = actor.method_5998(hand);
      if (cfg.itemCondition.isPresent() && !((ItemCondition)cfg.itemCondition.get()).test(new ItemCtx(stack, actor.method_37908(), actor))) {
         return false;
      }

      BiEntityCtx biCtx = new BiEntityCtx(actor, target, actor.method_37908());
      if (cfg.bientityCondition.isPresent() && !((BiEntityCondition)cfg.bientityCondition.get()).test(biCtx)) {
         return false;
      }

      if (runActions) {
         cfg.bientityAction.ifPresent(action -> action.run(biCtx));
         BlockInteractionHelper.applyItemResult(actor, hand, actor.method_5998(hand), cfg.heldItemAction, cfg.resultStack, cfg.resultItemAction);
      }

      return true;
   }

   public static AliasDefault<Boolean> targetUsed(boolean value) {
      return AliasDefault.of("target_used", Codec.BOOL, value);
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<ItemAction> heldItemAction,
      Optional<ItemAction> resultItemAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<ItemCondition> itemCondition,
      List<Hand> hands,
      Optional<ItemStackData> resultStack,
      boolean targetUsed
   ) {
   }
}
