package dev.alphasucks.aea.port.action.builtin.meta;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionLiveness;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.action.TypedActionRegistry;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import net.minecraft.class_2960;

public final class MetaActions {
   private static final class_2960 AND = Apoli.id("and");
   private static final class_2960 RANDOM_CHANCE = Apoli.id("random_chance");
   private static final class_2960 CHOICE = Apoli.id("choice");
   private static final class_2960 DELAY = Apoli.id("delay");
   private static final class_2960 LOOP = Apoli.id("loop");
   private static final class_2960 IF_ELSE = Apoli.id("if_else");
   private static final class_2960 IF_ELSE_LIST = Apoli.id("if_else_list");
   private static final class_2960 IF_CASE = Apoli.id("if_case");
   private static final class_2960 NOTHING = Apoli.id("nothing");

   private MetaActions() {
   }

   private static AliasingOptions chanceAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("chance")).build();
   }

   public static void registerEntity() {
      TypedActionRegistry<EntityCtx> reg = ActionTypes.ENTITY;
      reg.register(AND, new AndMetaAction<EntityCtx, EntityAction>(EntityAction.CODEC, EntityAction::run));
      reg.register(RANDOM_CHANCE, new RandomChanceMetaAction<EntityCtx, EntityAction>(EntityAction.CODEC, EntityAction::run, EntityCtx::entity, EntityCtx::level), chanceAliases());
      reg.register(CHOICE, new ChoiceMetaAction<EntityCtx, EntityAction>(EntityAction.CODEC, EntityAction::run));
      reg.register(DELAY, new DelayMetaAction<EntityCtx, EntityAction>(EntityAction.CODEC, EntityAction::run, ActionLiveness.entity(), EntityCtx::entity));
      reg.register(LOOP, new LoopMetaAction<EntityCtx, EntityAction>(EntityAction.CODEC, EntityAction::run, ActionLiveness.entity(), EntityCtx::entity));
      reg.register(IF_ELSE, new IfElseMetaAction<EntityCtx, EntityCondition, EntityAction>(EntityCondition.CODEC, EntityAction.CODEC, EntityCondition::test, EntityAction::run));
      reg.register(IF_ELSE_LIST, new IfElseListMetaAction<EntityCtx, EntityCondition, EntityAction>(EntityCondition.CODEC, EntityAction.CODEC, EntityCondition::test, EntityAction::run));
      reg.register(IF_CASE, new IfCaseMetaAction<EntityCtx, EntityCondition, EntityAction>(EntityCondition.CODEC, EntityAction.CODEC, EntityCondition::test, EntityAction::run));
      reg.register(NOTHING, new NothingMetaAction<>());
   }

   public static void registerBiEntity() {
      TypedActionRegistry<BiEntityCtx> reg = ActionTypes.BI_ENTITY;
      reg.register(AND, new AndMetaAction<BiEntityCtx, BiEntityAction>(BiEntityAction.CODEC, BiEntityAction::run));
      reg.register(
         RANDOM_CHANCE, new RandomChanceMetaAction<BiEntityCtx, BiEntityAction>(BiEntityAction.CODEC, BiEntityAction::run, BiEntityCtx::actor, BiEntityCtx::level), chanceAliases()
      );
      reg.register(CHOICE, new ChoiceMetaAction<BiEntityCtx, BiEntityAction>(BiEntityAction.CODEC, BiEntityAction::run));
      reg.register(DELAY, new DelayMetaAction<BiEntityCtx, BiEntityAction>(BiEntityAction.CODEC, BiEntityAction::run, ActionLiveness.biEntity(), BiEntityCtx::actor));
      reg.register(LOOP, new LoopMetaAction<BiEntityCtx, BiEntityAction>(BiEntityAction.CODEC, BiEntityAction::run, ActionLiveness.biEntity(), BiEntityCtx::actor));
      reg.register(IF_ELSE, new IfElseMetaAction<BiEntityCtx, BiEntityCondition, BiEntityAction>(BiEntityCondition.CODEC, BiEntityAction.CODEC, BiEntityCondition::test, BiEntityAction::run));
      reg.register(IF_ELSE_LIST, new IfElseListMetaAction<BiEntityCtx, BiEntityCondition, BiEntityAction>(BiEntityCondition.CODEC, BiEntityAction.CODEC, BiEntityCondition::test, BiEntityAction::run));
      reg.register(IF_CASE, new IfCaseMetaAction<BiEntityCtx, BiEntityCondition, BiEntityAction>(BiEntityCondition.CODEC, BiEntityAction.CODEC, BiEntityCondition::test, BiEntityAction::run));
      reg.register(NOTHING, new NothingMetaAction<>());
   }

   public static void registerBlock() {
      TypedActionRegistry<BlockCtx> reg = ActionTypes.BLOCK;
      reg.register(AND, new AndMetaAction<BlockCtx, BlockAction>(BlockAction.CODEC, BlockAction::run));
      reg.register(RANDOM_CHANCE, new RandomChanceMetaAction<BlockCtx, BlockAction>(BlockAction.CODEC, BlockAction::run, ctx -> null, BlockCtx::level), chanceAliases());
      reg.register(CHOICE, new ChoiceMetaAction<BlockCtx, BlockAction>(BlockAction.CODEC, BlockAction::run));
      reg.register(DELAY, new DelayMetaAction<BlockCtx, BlockAction>(BlockAction.CODEC, BlockAction::run, ActionLiveness.always(), BlockCtx::actor));
      reg.register(LOOP, new LoopMetaAction<BlockCtx, BlockAction>(BlockAction.CODEC, BlockAction::run, ActionLiveness.always(), BlockCtx::actor));
      reg.register(IF_ELSE, new IfElseMetaAction<BlockCtx, BlockCondition, BlockAction>(BlockCondition.CODEC, BlockAction.CODEC, BlockCondition::test, BlockAction::run));
      reg.register(IF_ELSE_LIST, new IfElseListMetaAction<BlockCtx, BlockCondition, BlockAction>(BlockCondition.CODEC, BlockAction.CODEC, BlockCondition::test, BlockAction::run));
      reg.register(IF_CASE, new IfCaseMetaAction<BlockCtx, BlockCondition, BlockAction>(BlockCondition.CODEC, BlockAction.CODEC, BlockCondition::test, BlockAction::run));
      reg.register(NOTHING, new NothingMetaAction<>());
   }

   public static void registerItem() {
      TypedActionRegistry<ItemCtx> reg = ActionTypes.ITEM;
      reg.register(AND, new AndMetaAction<ItemCtx, ItemAction>(ItemAction.CODEC, ItemAction::run));
      reg.register(RANDOM_CHANCE, new RandomChanceMetaAction<ItemCtx, ItemAction>(ItemAction.CODEC, ItemAction::run, ItemCtx::holder, ItemCtx::level), chanceAliases());
      reg.register(CHOICE, new ChoiceMetaAction<ItemCtx, ItemAction>(ItemAction.CODEC, ItemAction::run));
      reg.register(DELAY, new DelayMetaAction<ItemCtx, ItemAction>(ItemAction.CODEC, ItemAction::run, ActionLiveness.item(), ItemCtx::holder));
      reg.register(LOOP, new LoopMetaAction<ItemCtx, ItemAction>(ItemAction.CODEC, ItemAction::run, ActionLiveness.item(), ItemCtx::holder));
      reg.register(IF_ELSE, new IfElseMetaAction<ItemCtx, ItemCondition, ItemAction>(ItemCondition.CODEC, ItemAction.CODEC, ItemCondition::test, ItemAction::run));
      reg.register(IF_ELSE_LIST, new IfElseListMetaAction<ItemCtx, ItemCondition, ItemAction>(ItemCondition.CODEC, ItemAction.CODEC, ItemCondition::test, ItemAction::run));
      reg.register(IF_CASE, new IfCaseMetaAction<ItemCtx, ItemCondition, ItemAction>(ItemCondition.CODEC, ItemAction.CODEC, ItemCondition::test, ItemAction::run));
      reg.register(NOTHING, new NothingMetaAction<>());
   }
}
