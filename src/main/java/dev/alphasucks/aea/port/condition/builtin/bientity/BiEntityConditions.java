package dev.alphasucks.aea.port.condition.builtin.bientity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.builtin.meta.SendConditionMeta.BiEntity;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class BiEntityConditions {
   private BiEntityConditions() {
   }

   public static void register() {
      ConditionTypes.BI_ENTITY.register(Apoli.id("projectile_owner"), new ProjectileOwnerCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("same_team"), new SameTeamCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("allied")).build());
      ConditionTypes.BI_ENTITY
         .register(
            Apoli.id("same_equipped_item"),
            new SameEquippedItemCondition(),
            AliasingOptions.builder().renameField("compare_components", "compare_tag").renameField("compare_nbt", "compare_tag").build()
         );
      ConditionTypes.BI_ENTITY.register(Apoli.id("damage_would_kill"), new DamageWouldKillBiEntityCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("equal"), new EqualCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("grabbed"), new GrabbedBiEntityCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("raycast"), new RaycastBiEntityCondition());
      ConditionTypes.BI_ENTITY
         .register(Apoli.id("in_entity_set"), new InEntitySetCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("in_set")).build());
      ConditionTypes.BI_ENTITY.register(Apoli.id("disguised"), new DisguisedBiCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("roped_together"), new RopedTogetherCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("script"), new ScriptBiEntityCondition());
      ConditionTypes.BI_ENTITY.register(Apoli.id("send_condition"), new BiEntity(), AliasingOptions.builder().addTypeAlias("shappoli:send_condition").build());
   }
}
