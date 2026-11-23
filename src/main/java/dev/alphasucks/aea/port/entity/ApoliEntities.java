package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.class_1299;
import net.minecraft.class_1311;
import net.minecraft.class_2378;
import net.minecraft.class_7923;
import net.minecraft.class_1299.class_1300;

public final class ApoliEntities {
   public static final class_1299<CustomProjectileEntity> CUSTOM_PROJECTILE = class_1300.<CustomProjectileEntity>method_5903(CustomProjectileEntity::new, class_1311.field_17715)
      .method_17687(0.25F, 0.25F)
      .method_27299(4)
      .method_27300(10)
      .method_5905("custom_projectile");
   public static final class_1299<MinionEntity> MINION = class_1300.method_5903(MinionEntity::new, class_1311.field_17715)
      .method_17687(0.6F, 0.9F)
      .method_27299(10)
      .method_5905("minion");
   public static final class_1299<CloneEntity> CLONE = class_1300.method_5903(CloneEntity::new, class_1311.field_17715)
      .method_17687(0.6F, 1.8F)
      .method_27299(10)
      .method_5905("clone");

   private ApoliEntities() {
   }

   public static class_1299<CustomProjectileEntity> customProjectile() {
      return CUSTOM_PROJECTILE;
   }

   public static class_1299<MinionEntity> minionType() {
      return MINION;
   }

   public static class_1299<CloneEntity> cloneType() {
      return CLONE;
   }

   public static void register() {
      class_2378.method_10230(class_7923.field_41177, Apoli.id("custom_projectile"), CUSTOM_PROJECTILE);
      class_2378.method_10230(class_7923.field_41177, Apoli.id("minion"), MINION);
      class_2378.method_10230(class_7923.field_41177, Apoli.id("clone"), CLONE);
      FabricDefaultAttributeRegistry.register(MINION, MinionEntity.createAttributes());
      FabricDefaultAttributeRegistry.register(CLONE, CloneEntity.createAttributes());
   }
}
