package io.github.apace100.originsclasses.util;

import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_5134;
import net.minecraft.class_1322.class_1323;

public final class EntityUtil {
   // AEA: fixed ids so the boost can't stack (it used a fresh random UUID on every setOwner/bondWithPlayer call)
   private static final java.util.UUID HEALTH_ID = java.util.UUID.fromString("5e3bd0a6-6d63-4a3e-9f27-0c7a1b4e2a11");
   private static final java.util.UUID DAMAGE_ID = java.util.UUID.fromString("9a1c2f4e-3b7d-4c55-8e0a-6f2d9b1c7e22");
   private static final String NAME = "Beastmaster boost";

   public static void addBeastmasterAttributes(class_1309 entity) {
      if (entity.method_6127().method_27306(class_5134.field_23716)) {
         add(entity.method_5996(class_5134.field_23716), HEALTH_ID, 0.3, class_1323.field_6331);
      }

      if (entity.method_6127().method_27306(class_5134.field_23721)) {
         add(entity.method_5996(class_5134.field_23721), DAMAGE_ID, 1.5, class_1323.field_6328);
      }
   }

   private static void add(@org.jetbrains.annotations.Nullable net.minecraft.class_1324 instance, java.util.UUID id, double amount, class_1323 operation) {
      if (instance == null || instance.method_6199(id) != null) {
         return;
      }
      for (class_1322 existing : instance.method_6195()) {
         if (NAME.equals(existing.method_6185())) {
            return; // boosted by an older version (random UUID): keep that one
         }
      }
      instance.method_26837(new class_1322(id, NAME, amount, operation));
   }
}
