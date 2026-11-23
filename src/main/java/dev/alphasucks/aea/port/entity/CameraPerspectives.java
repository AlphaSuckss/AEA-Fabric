package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.data.CameraPerspective;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;

public final class CameraPerspectives {
   private static final Map<UUID, CameraPerspective> KNOWN = new ConcurrentHashMap();

   private CameraPerspectives() {
   }

   public static void set(UUID player, boolean firstPerson) {
      CameraPerspective previous = (CameraPerspective)KNOWN.get(player);
      if (firstPerson) {
         KNOWN.put(player, CameraPerspective.FIRST_PERSON);
      } else if (previous == null || previous == CameraPerspective.FIRST_PERSON) {
         KNOWN.put(player, CameraPerspective.THIRD_PERSON_BACK);
      }
   }

   public static void setType(UUID player, int cameraType) {
      KNOWN.put(player, CameraPerspective.byId(cameraType));
   }

   public static void remove(UUID player) {
      KNOWN.remove(player);
   }

   public static void clear() {
      KNOWN.clear();
   }

   public static boolean isFirstPerson(class_1297 entity) {
      return typeOf(entity) == CameraPerspective.FIRST_PERSON;
   }

   public static CameraPerspective typeOf(class_1297 entity) {
      CameraPerspective known = (CameraPerspective)KNOWN.get(entity.method_5667());
      return known == null ? CameraPerspective.FIRST_PERSON : known;
   }
}
