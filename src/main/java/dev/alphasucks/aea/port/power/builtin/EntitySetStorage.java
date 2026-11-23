package dev.alphasucks.aea.port.power.builtin;

import net.minecraft.class_18;
import net.minecraft.class_2487;
import net.minecraft.server.MinecraftServer;

public final class EntitySetStorage extends class_18 {
   private static final String NAME = "apoli_entity_sets";

   private EntitySetStorage() {
   }

   public static void attach(MinecraftServer server) {
      EntitySetStorage storage = server.method_30002().method_17983().method_17924(EntitySetStorage::load, EntitySetStorage::new, "apoli_entity_sets");
      EntitySetPower.attachStorage(storage::method_80);
   }

   private static EntitySetStorage load(class_2487 tag) {
      EntitySetPower.loadPersistent(tag);
      return new EntitySetStorage();
   }

   @Override
   public class_2487 method_75(class_2487 tag) {
      return EntitySetPower.savePersistent(tag);
   }
}
