package dev.alphasucks.aea.port.attribute;

import dev.alphasucks.aea.port.Apoli;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1324;
import net.minecraft.class_1329;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_7923;

public final class ApoliAttributes {
   public static final double DEFAULT_BLOCK_INTERACTION_RANGE = 4.5;
   public static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0;
   public static final class_2960 BLOCK_INTERACTION_RANGE_ID = Apoli.id("player.block_interaction_range");
   public static final class_2960 ENTITY_INTERACTION_RANGE_ID = Apoli.id("player.entity_interaction_range");
   public static final class_1320 BLOCK_INTERACTION_RANGE = new class_1329("attribute.name.player.block_interaction_range", 4.5, 0.0, 64.0).method_26829(true);
   public static final class_1320 ENTITY_INTERACTION_RANGE = new class_1329("attribute.name.player.entity_interaction_range", 3.0, 0.0, 64.0)
      .method_26829(true);
   private static final Map<class_2960, class_2960> ALIASES = new HashMap();

   private ApoliAttributes() {
   }

   private static void alias(String namespace, String path, class_2960 target) {
      ALIASES.put(new class_2960(namespace, path), target);
   }

   public static void register() {
      class_2378.method_10230(class_7923.field_41190, BLOCK_INTERACTION_RANGE_ID, BLOCK_INTERACTION_RANGE);
      class_2378.method_10230(class_7923.field_41190, ENTITY_INTERACTION_RANGE_ID, ENTITY_INTERACTION_RANGE);
   }

   public static class_2960 resolve(class_2960 id) {
      class_2960 aliased = (class_2960)ALIASES.get(id);
      return aliased == null ? id : aliased;
   }

   public static class_1320 get(class_2960 id) {
      return class_7923.field_41190.method_10223(resolve(id));
   }

   public static double blockInteractionRange(class_1309 entity) {
      return valueOf(entity, BLOCK_INTERACTION_RANGE, 4.5);
   }

   public static double entityInteractionRange(class_1309 entity) {
      return valueOf(entity, ENTITY_INTERACTION_RANGE, 3.0);
   }

   public static double blockInteractionBonus(class_1309 entity) {
      return blockInteractionRange(entity) - 4.5;
   }

   public static double entityInteractionBonus(class_1309 entity) {
      return entityInteractionRange(entity) - 3.0;
   }

   public static double extendSquared(double squaredDistance, double bonus) {
      return bonus == 0.0 ? squaredDistance : class_3532.method_33723(Math.max(0.0, Math.sqrt(squaredDistance) + bonus));
   }

   private static double valueOf(class_1309 entity, class_1320 attribute, double fallback) {
      if (entity == null) {
         return fallback;
      }

      class_1324 instance = entity.method_5996(attribute);
      return instance == null ? fallback : instance.method_6194();
   }

   static {
      alias("minecraft", "player.block_interaction_range", BLOCK_INTERACTION_RANGE_ID);
      alias("minecraft", "player.entity_interaction_range", ENTITY_INTERACTION_RANGE_ID);
      alias("minecraft", "block_interaction_range", BLOCK_INTERACTION_RANGE_ID);
      alias("minecraft", "entity_interaction_range", ENTITY_INTERACTION_RANGE_ID);
      alias("origins", "player.block_interaction_range", BLOCK_INTERACTION_RANGE_ID);
      alias("origins", "player.entity_interaction_range", ENTITY_INTERACTION_RANGE_ID);
      alias("reach-entity-attributes", "reach", BLOCK_INTERACTION_RANGE_ID);
      alias("reach-entity-attributes", "attack_range", ENTITY_INTERACTION_RANGE_ID);
   }
}
