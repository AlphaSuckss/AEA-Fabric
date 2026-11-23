package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.power.builtin.GameEventListenerPower.State;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public interface EntityGameEventListenerAccess {
   @Nullable
   Map<class_2960, State> apoli$getGameEventStates();

   void apoli$setGameEventStates(@Nullable Map<class_2960, State> var1);

   @Nullable
   static Map<class_2960, State> getMap(class_1297 entity) {
      return entity instanceof EntityGameEventListenerAccess access ? access.apoli$getGameEventStates() : null;
   }

   static void setMap(class_1297 entity, @Nullable Map<class_2960, State> map) {
      if (entity instanceof EntityGameEventListenerAccess access) {
         access.apoli$setGameEventStates(map);
      }
   }
}
