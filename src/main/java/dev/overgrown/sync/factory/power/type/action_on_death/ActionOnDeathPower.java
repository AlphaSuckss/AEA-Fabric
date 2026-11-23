package dev.overgrown.sync.factory.power.type.action_on_death;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3545;

public class ActionOnDeathPower extends Power {
   private final ActionFactory<class_3545<class_1297, class_1297>>.Instance bientityAction;
   private final ConditionFactory<class_3545<class_1297, class_1297>>.Instance bientityCondition;
   private final ConditionFactory<class_3545<class_1282, Float>>.Instance damageCondition;

   public ActionOnDeathPower(
      PowerType<?> type,
      class_1309 entity,
      ActionFactory<class_3545<class_1297, class_1297>>.Instance bientityAction,
      ConditionFactory<class_3545<class_1297, class_1297>>.Instance bientityCondition,
      ConditionFactory<class_3545<class_1282, Float>>.Instance damageCondition
   ) {
      super(type, entity);
      this.bientityAction = bientityAction;
      this.bientityCondition = bientityCondition;
      this.damageCondition = damageCondition;
   }

   public void onDeath(class_1282 damageSource, float damageAmount) {
      if (this.isActive()) {
         class_1297 killer = damageSource.method_5529();
         if (killer == null) {
            // no attacker: bi-entity actions/conditions would get a null actor (this power has no plain entity action)
            return;
         }

         class_3545<class_1297, class_1297> entities = new class_3545<>(killer, this.entity);
         if (this.bientityCondition == null || this.bientityCondition.test(entities)) {
            if (this.damageCondition == null || this.damageCondition.test(new class_3545<>(damageSource, damageAmount))) {
               this.bientityAction.accept(entities);
            }
         }
      }
   }

   public static PowerFactory<ActionOnDeathPower> getFactory() {
      return new PowerFactory<ActionOnDeathPower>(
            Sync.identifier("action_on_death"),
            new SerializableData()
               .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
               .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
               .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
            data -> (type, entity) -> new ActionOnDeathPower(
               type, entity, data.get("bientity_action"), data.get("bientity_condition"), data.get("damage_condition")
            )
         )
         .allowCondition();
   }
}
