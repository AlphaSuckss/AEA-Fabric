package dev.overgrown.sync.factory.action.entity.revoke_all_powers;

import dev.overgrown.sync.Sync;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public class RevokeAllPowersAction {
   public static void action(Instance data, class_1297 entity) {
      PowerHolderComponent component = (PowerHolderComponent)PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
      if (component != null) {
         class_2960 source = data.get("source");
         if (component.removeAllPowersFromSource(source) > 0) {
            component.sync();
         }
      }
   }

   public static ActionFactory<class_1297> getFactory() {
      return new ActionFactory<>(
         Sync.identifier("revoke_all_powers"), new SerializableData().add("source", SerializableDataTypes.IDENTIFIER), RevokeAllPowersAction::action
      );
   }
}
