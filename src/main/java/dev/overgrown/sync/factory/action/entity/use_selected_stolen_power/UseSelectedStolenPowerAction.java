package dev.overgrown.sync.factory.action.entity.use_selected_stolen_power;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.bientity.transfer.StolenPowerSlotManager;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public class UseSelectedStolenPowerAction {
   public static void action(Instance data, class_1297 entity) {
      if (!entity.method_37908().method_8608()) {
         PowerHolderComponent component = (PowerHolderComponent)PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
         if (component != null) {
            List<PowerType<?>> powers = StolenPowerSlotManager.getSelectedPackagePowers(entity);
            if (!powers.isEmpty()) {
               for (PowerType<?> pt : powers) {
                  use(component, pt);
                  // packages hold top-level powers only; the active parts of an apoli:multiple are its sub-powers
                  if (pt instanceof MultiplePowerType<?> multiple && multiple.getSubPowers() != null) {
                     for (class_2960 subId : multiple.getSubPowers()) {
                        if (PowerTypeRegistry.contains(subId)) {
                           use(component, PowerTypeRegistry.get(subId));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void use(PowerHolderComponent component, PowerType<?> pt) {
      if (getPowerUnchecked(component, pt) instanceof ActiveCooldownPower acp) {
         acp.onUse();
      }
   }

   private static <T extends Power> T getPowerUnchecked(PowerHolderComponent component, PowerType<?> pt) {
      return component.getPower((PowerType<T>)pt);
   }

   public static ActionFactory<class_1297> getFactory() {
      return new ActionFactory<>(Sync.identifier("use_selected_stolen_power"), new SerializableData(), UseSelectedStolenPowerAction::action);
   }
}
