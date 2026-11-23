package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.ActionResult;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class ActionOnUsePower extends PowerType<ActionOnUsePower.Config> {
   @Override
   public MapCodec<ActionOnUsePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnUsePower.Config::bientityAction),
               LoggedOptionalField.of("held_item_action", ItemAction.CODEC).forGetter(ActionOnUsePower.Config::heldItemAction),
               LoggedOptionalField.of("result_item_action", ItemAction.CODEC).forGetter(ActionOnUsePower.Config::resultItemAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnUsePower.Config::bientityCondition),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ActionOnUsePower.Config::itemCondition),
               Hand.LIST_CODEC.optionalFieldOf("hands", Hand.BOTH).forGetter(ActionOnUsePower.Config::hands),
               ItemStackData.CODEC.optionalFieldOf("result_stack").forGetter(ActionOnUsePower.Config::resultStack),
               ActionResult.CODEC.optionalFieldOf("action_result", ActionResult.SUCCESS).forGetter(ActionOnUsePower.Config::actionResult),
               Codec.BOOL.optionalFieldOf("target_used", false).forGetter(ActionOnUsePower.Config::targetUsed)
            )
            .apply(i, ActionOnUsePower.Config::new)
      );
   }

   public class_1269 tryFire(ActionOnUsePower.Config cfg, class_1657 actor, class_1297 target, class_1268 hand) {
      if (!handAllowed(cfg, hand)) {
         return class_1269.field_5811;
      }

      class_1799 stack = actor.method_5998(hand);
      if (cfg.itemCondition.isPresent() && !((ItemCondition)cfg.itemCondition.get()).test(new ItemCtx(stack, actor.method_37908(), actor))) {
         return class_1269.field_5811;
      }

      BiEntityCtx biCtx = new BiEntityCtx(actor, target, actor.method_37908());
      if (cfg.bientityCondition.isPresent() && !((BiEntityCondition)cfg.bientityCondition.get()).test(biCtx)) {
         return class_1269.field_5811;
      }

      cfg.bientityAction.ifPresent(a -> a.run(biCtx));
      BlockInteractionHelper.applyItemResult(actor, hand, actor.method_5998(hand), cfg.heldItemAction, cfg.resultStack, cfg.resultItemAction);
      return cfg.actionResult.vanilla();
   }

   public boolean isActive(class_2960 id, ActionOnUsePower.Config cfg, EntityCtx ctx) {
      return true;
   }

   private static boolean handAllowed(ActionOnUsePower.Config cfg, class_1268 hand) {
      Hand needed = Hand.of(hand);

      for (Hand h : cfg.hands) {
         if (h == needed) {
            return true;
         }
      }

      return false;
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<ItemAction> heldItemAction,
      Optional<ItemAction> resultItemAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<ItemCondition> itemCondition,
      List<Hand> hands,
      Optional<ItemStackData> resultStack,
      ActionResult actionResult,
      boolean targetUsed
   ) {
   }
}
