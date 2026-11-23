package dev.overgrown.sync.factory.action.entity.summons.entities.clone;

import dev.overgrown.sync.factory.action.entity.summons.utils.ExtraTameable;
import dev.overgrown.sync.factory.action.entity.summons.utils.Temporary;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1266;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import net.minecraft.class_1315;
import net.minecraft.class_1347;
import net.minecraft.class_1352;
import net.minecraft.class_1361;
import net.minecraft.class_1366;
import net.minecraft.class_1376;
import net.minecraft.class_1380;
import net.minecraft.class_1383;
import net.minecraft.class_1399;
import net.minecraft.class_1405;
import net.minecraft.class_1429;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1675;
import net.minecraft.class_1676;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1924;
import net.minecraft.class_1937;
import net.minecraft.class_2398;
import net.minecraft.class_2487;
import net.minecraft.class_268;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3321;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3730;
import net.minecraft.class_3745;
import net.minecraft.class_5134;
import net.minecraft.class_5425;
import net.minecraft.class_6025;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_1665.class_1666;
import net.minecraft.class_5132.class_5133;
import org.jetbrains.annotations.Nullable;

public class CloneEntity extends class_1588 implements ExtraTameable, class_3745, Temporary {
   private static final double COMBAT_SPEED = 1.35;
   private static final float SHOOTING_RANGE = 32.0F;
   private boolean canSit;
   private boolean followOwner;
   private boolean canAttack;
   private int maxTicks;
   private final class_1366 MELEE_ATTACK = new class_1366(this, 1.35, false);
   private final class_1380<CloneEntity> RANGED_ATTACK = new class_1380<>(this, 1.35, 20, 32.0F);
   private final class_1383<CloneEntity> CROSSBOW_ATTACK = new class_1383<>(this, 1.35, 32.0F);
   private static final class_2940<Optional<UUID>> OWNER_UUID = class_2945.method_12791(CloneEntity.class, class_2943.field_13313);
   private static final class_2940<Boolean> SITTING = class_2945.method_12791(CloneEntity.class, class_2943.field_13323);
   private static final class_2940<Boolean> CHARGING = class_2945.method_12791(CloneEntity.class, class_2943.field_13323);
   private static final class_2940<String> CUSTOM_WIDE_TEXTURE_NAMESPACE = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);
   private static final class_2940<String> CUSTOM_WIDE_TEXTURE_PATH = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);
   private static final class_2940<String> CUSTOM_SLIM_TEXTURE_NAMESPACE = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);
   private static final class_2940<String> CUSTOM_SLIM_TEXTURE_PATH = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);

   public CloneEntity(class_1299<? extends CloneEntity> entityType, class_1937 world) {
      super(entityType, world);
      this.updateWeaponGoals();
      this.field_6194 = 0;
      this.maxTicks = 1200;
   }

   @Override
   public class_1315 method_5943(class_5425 world, class_1266 difficulty, class_3730 spawnReason, class_1315 entityData, class_2487 entityNbt) {
      class_1315 data = super.method_5943(world, difficulty, spawnReason, entityData, entityNbt);
      this.updateWeaponGoals();
      this.method_5952(false);
      if (this.method_37908() instanceof class_3218 serverWorld
         && this.method_5781() == null
         && this.getOwner() != null
         && this.getOwner().method_5781() != null
         && this.getOwner().method_5781() instanceof class_268 implementedTeam) {
         serverWorld.method_14170().method_1172(this.method_5845(), implementedTeam);
      }

      return data;
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(OWNER_UUID, Optional.empty());
      this.field_6011.method_12784(SITTING, false);
      this.field_6011.method_12784(CHARGING, false);
      this.field_6011.method_12784(CUSTOM_WIDE_TEXTURE_NAMESPACE, "");
      this.field_6011.method_12784(CUSTOM_WIDE_TEXTURE_PATH, "");
      this.field_6011.method_12784(CUSTOM_SLIM_TEXTURE_NAMESPACE, "");
      this.field_6011.method_12784(CUSTOM_SLIM_TEXTURE_PATH, "");
   }

   @Override
   protected void method_5959() {
      super.method_5959();
      this.field_6201.method_6277(0, new class_1347(this));
      this.field_6201.method_6277(2, new CloneEntity.FollowOwnerGoal(this));
      this.field_6201.method_6277(4, new class_1361(this, class_1657.class, 16.0F));
      this.field_6201.method_6277(5, new class_1361(this, class_1429.class, 16.0F));
      this.field_6201.method_6277(6, new class_1376(this));
      this.field_6185.method_6277(0, new CloneEntity.DefendOwnerGoal(this));
      this.field_6185.method_6277(1, new CloneEntity.FightForOwnerGoal(this));
      this.field_6185.method_6277(2, new class_1399(this));
   }

   public static class_5133 createCloneAttributes() {
      class_5133 attributes = class_1588.method_26828();
      attributes.method_26867(class_5134.field_23721);
      attributes.method_26868(class_5134.field_23716, 20.0);
      attributes.method_26868(class_5134.field_23719, 0.35F);
      attributes.method_26868(class_5134.field_23717, 64.0);
      return attributes;
   }

   @Override
   protected boolean method_23734() {
      return false;
   }

   @Override
   public class_3419 method_5634() {
      return class_3419.field_15248;
   }

   @Override
   public boolean method_5939(class_1799 stack) {
      return false;
   }

   @Override
   public boolean method_5733() {
      return true;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      // server only: the client neither knows maxTicks (not synced) nor always sees the owner
      if (this.method_37908() instanceof class_3218 serverWorld) {
         if (this.getOwner() != null
            && this.getOwner().method_37908().method_27983() == this.method_37908().method_27983()
            && (this.field_6012 <= this.maxTicks || this.maxTicks <= 0)) {
            if (this.field_6012 == this.maxTicks - 1) {
               for (int i = 0; i < 20; i++) {
                  double velX = this.field_5974.method_43059() * 0.02;
                  double velY = this.field_5974.method_43059() * 0.02;
                  double velZ = this.field_5974.method_43059() * 0.02;
                  // count 0: the deltas are used as the particle's velocity
                  serverWorld.method_14199(class_2398.field_11203, this.method_23322(1.0), this.method_23319(), this.method_23325(1.0), 0, velX, velY, velZ, 1.0);
               }
            }
         } else {
            this.method_31472();
         }
      }
   }

   @Override
   public boolean isOwned() {
      return this.field_6011.method_12789(OWNER_UUID).isPresent();
   }

   @Override
   public void setOwnerUUID(@Nullable UUID uuid) {
      if (uuid == null) {
         this.field_6011.method_12778(OWNER_UUID, Optional.empty());
      } else {
         this.field_6011.method_12778(OWNER_UUID, Optional.of(uuid));
      }
   }

   @Override
   public void setOwner(@Nullable class_1309 owner) {
      if (owner instanceof class_1657) {
         ExtraTameable.super.setOwner(owner);
      }
   }

   @Nullable
   @Override
   public UUID method_6139() {
      Optional<UUID> uuid = this.field_6011.method_12789(OWNER_UUID);
      return (UUID)uuid.orElse(null);
   }

   @Nullable
   public class_1657 getOwner() {
      UUID uuid = this.method_6139();
      return uuid == null ? null : this.method_37908().method_18470(uuid);
   }

   @Nullable
   @Override
   public class_1309 method_35057() {
      return this.getOwner();
   }

   public void method_7105(class_1309 target, float pullProgress) {
      if (this.method_24518(class_1802.field_8399)) {
         this.method_24654(this, 1.6F);
      } else {
         class_1799 arrowType = this.method_18808(this.method_5998(class_1675.method_18812(this, class_1802.field_8102)));
         class_1665 persistentProjectileEntity = class_1675.method_18813(this, arrowType, pullProgress);
         persistentProjectileEntity.field_7572 = class_1666.field_7592;
         double xDirection = target.method_23317() - this.method_23317();
         double yDirection = target.method_23323(0.3333333333333333) - persistentProjectileEntity.method_23318();
         double zDirection = target.method_23321() - this.method_23321();
         double g = Math.sqrt(xDirection * xDirection + zDirection * zDirection);
         persistentProjectileEntity.method_7485(xDirection, yDirection + g * 0.2F, zDirection, 1.6F, 14 - this.method_37908().method_8407().method_5461() * 4);
         this.method_37908()
            .method_43128(
               null,
               this.method_23317(),
               this.method_23318(),
               this.method_23321(),
               class_3417.field_14600,
               class_3419.field_15248,
               1.0F,
               1.0F / (this.method_37908().method_8409().method_43057() * 0.4F + 1.2F) + pullProgress * 0.5F
            );
         this.method_37908().method_8649(persistentProjectileEntity);
      }
   }

   public boolean isCharging() {
      return this.field_6011.method_12789(CHARGING);
   }

   @Override
   public void method_7110(boolean isCharging) {
      this.field_6011.method_12778(CHARGING, isCharging);
   }

   @Override
   public void method_18811(class_1309 target, class_1799 crossbow, class_1676 projectile, float multiShotSpray) {
      projectile.method_7432(this.getOwner());
      if (projectile instanceof class_1665 persistent) {
         persistent.field_7572 = class_1666.field_7592;
      }

      this.method_24652(this, target, projectile, multiShotSpray, 1.6F);
   }

   @Override
   public void method_24651() {
   }

   public void updateWeaponGoals() {
      int WEAPON_GOAL_PRIORITY = 1;
      if (this.method_37908() != null && !this.method_37908().method_8608()) {
         this.field_6201.method_6280(this.MELEE_ATTACK);
         this.field_6201.method_6280(this.RANGED_ATTACK);
         this.field_6201.method_6280(this.CROSSBOW_ATTACK);
         if (this.method_24518(class_1802.field_8102)) {
            this.field_6201.method_6277(1, this.RANGED_ATTACK);
         } else if (this.method_24518(class_1802.field_8399)) {
            this.field_6201.method_6277(1, this.CROSSBOW_ATTACK);
         } else {
            this.field_6201.method_6277(1, this.MELEE_ATTACK);
         }
      }
   }

   public void setCustomWideTexture(@Nullable class_2960 texture) {
      this.field_6011.method_12778(CUSTOM_WIDE_TEXTURE_NAMESPACE, texture != null ? texture.method_12836() : "");
      this.field_6011.method_12778(CUSTOM_WIDE_TEXTURE_PATH, texture != null ? texture.method_12832() : "");
   }

   public void setCustomSlimTexture(@Nullable class_2960 texture) {
      this.field_6011.method_12778(CUSTOM_SLIM_TEXTURE_NAMESPACE, texture != null ? texture.method_12836() : "");
      this.field_6011.method_12778(CUSTOM_SLIM_TEXTURE_PATH, texture != null ? texture.method_12832() : "");
   }

   public boolean hasCustomWideTexture() {
      return !this.field_6011.method_12789(CUSTOM_WIDE_TEXTURE_PATH).isEmpty();
   }

   public boolean hasCustomSlimTexture() {
      return !this.field_6011.method_12789(CUSTOM_SLIM_TEXTURE_PATH).isEmpty();
   }

   public boolean hasAnyCustomTexture() {
      return this.hasCustomWideTexture() || this.hasCustomSlimTexture();
   }

   public class_2960 getCustomWideTexture() {
      return new class_2960(this.field_6011.method_12789(CUSTOM_WIDE_TEXTURE_NAMESPACE), this.field_6011.method_12789(CUSTOM_WIDE_TEXTURE_PATH));
   }

   public class_2960 getCustomSlimTexture() {
      return new class_2960(this.field_6011.method_12789(CUSTOM_SLIM_TEXTURE_NAMESPACE), this.field_6011.method_12789(CUSTOM_SLIM_TEXTURE_PATH));
   }

   public class_2960 getCustomTexture(boolean slim) {
      if (slim) {
         if (this.hasCustomSlimTexture()) {
            return this.getCustomSlimTexture();
         }

         if (this.hasCustomWideTexture()) {
            return this.getCustomWideTexture();
         }
      } else {
         if (this.hasCustomWideTexture()) {
            return this.getCustomWideTexture();
         }

         if (this.hasCustomSlimTexture()) {
            return this.getCustomSlimTexture();
         }
      }

      return null;
   }

   @Override
   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      this.updateWeaponGoals();
      if (nbt.method_10545("CustomWideTexture")) {
         class_2487 tex = nbt.method_10562("CustomWideTexture");
         String ns = tex.method_10558("Namespace");
         String path = tex.method_10558("Path");
         if (!ns.isEmpty() && !path.isEmpty()) {
            class_2960 id = class_2960.method_12829(ns + ":" + path);
            if (id != null) {
               this.setCustomWideTexture(id);
            }
         }
      }

      if (nbt.method_10545("CustomSlimTexture")) {
         class_2487 tex = nbt.method_10562("CustomSlimTexture");
         String ns = tex.method_10558("Namespace");
         String path = tex.method_10558("Path");
         if (!ns.isEmpty() && !path.isEmpty()) {
            class_2960 id = class_2960.method_12829(ns + ":" + path);
            if (id != null) {
               this.setCustomSlimTexture(id);
            }
         }
      }

      UUID uuid;
      if (nbt.method_25928("Owner")) {
         uuid = nbt.method_25926("Owner");
      } else {
         String ownerName = nbt.method_10558("Owner");
         uuid = class_3321.method_14546(this.method_5682(), ownerName);
      }

      if (uuid != null) {
         this.setOwnerUUID(uuid);
      }

      if (nbt.method_10545("CanSit")) {
         this.canSit = nbt.method_10577("CanSit");
      }

      if (nbt.method_10545("FollowOwner")) {
         this.followOwner = nbt.method_10577("FollowOwner");
      }

      if (nbt.method_10545("CanAttack")) {
         this.canAttack = nbt.method_10577("CanAttack");
      }

      if (nbt.method_10545("MaxLifetime")) {
         this.maxTicks = nbt.method_10550("MaxLifetime");
      }
   }

   @Override
   public void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      if (this.hasCustomWideTexture()) {
         class_2487 tex = new class_2487();
         tex.method_10582("Namespace", this.getCustomWideTexture().method_12836());
         tex.method_10582("Path", this.getCustomWideTexture().method_12832());
         nbt.method_10566("CustomWideTexture", tex);
      }

      if (this.hasCustomSlimTexture()) {
         class_2487 tex = new class_2487();
         tex.method_10582("Namespace", this.getCustomSlimTexture().method_12836());
         tex.method_10582("Path", this.getCustomSlimTexture().method_12832());
         nbt.method_10566("CustomSlimTexture", tex);
      }

      if (this.isOwned()) {
         nbt.method_25927("Owner", this.method_6139());
      }

      nbt.method_10556("CanSit", this.canSit);
      nbt.method_10556("FollowOwner", this.followOwner);
      nbt.method_10556("CanAttack", this.canAttack);
      nbt.method_10569("MaxLifetime", this.maxTicks);
   }

   @Override
   public class_1269 method_5992(class_1657 player, class_1268 hand) {
      if (player == this.getOwner()) {
         if (this.method_37908().method_8608()) {
            return class_1269.field_5812;
         }

         if (this.canSit) {
            this.toggleSitting();
            return class_1269.method_29236(false);
         }
      }

      return class_1269.field_5811;
   }

   protected class_1665 createArrowProjectile(class_1799 arrow, float damageModifier) {
      class_1665 projectile = class_1675.method_18813(this, arrow, damageModifier);
      projectile.field_7572 = class_1666.field_7592;
      return projectile;
   }

   @Override
   public class_1310 method_6046() {
      return this.getOwner() != null ? this.getOwner().method_6046() : super.method_6046();
   }

   @Override
   public boolean method_18395(class_1309 target) {
      if (this.getOwner() != null
         && target.method_33190()
         && target != this.getOwner()
         && !this.isSitting()
         && this.canAttack
         && (this.method_5781() == null || target.method_5781() == null || !this.method_5781().method_1206(target.method_5781()))) {
         return target instanceof class_6025 tameable ? tameable.method_35057() != this.getOwner() : true;
      } else {
         return false;
      }
   }

   public void setCanSit(boolean value) {
      this.canSit = value;
   }

   public void setFollowOwner(boolean value) {
      this.followOwner = value;
   }

   public void setCanAttack(boolean value) {
      this.canAttack = value;
   }

   public boolean isSitting() {
      return this.field_6011.method_12789(SITTING);
   }

   public void toggleSitting() {
      this.field_6011.method_12778(SITTING, !this.isSitting());
   }

   public void setSitting(boolean isSat) {
      this.field_6011.method_12778(SITTING, isSat);
   }

   @Override
   public class_1924 method_48926() {
      return this.method_37908();
   }

   @Override
   public void setMaxLifetime(int ticks) {
      this.maxTicks = ticks;
   }

   @Override
   public boolean method_5822() {
      return false;
   }

   protected abstract static class AssistOwnerGoal extends class_1405 {
      protected final CloneEntity clone;
      protected class_1657 owner;
      protected int timer = 0;

      protected AssistOwnerGoal(CloneEntity clone) {
         super(clone, false);
         this.clone = clone;
         this.method_6265(EnumSet.of(class_4134.field_18408));
      }

      @Override
      public boolean method_6264() {
         if (this.owner == null) {
            this.owner = this.clone.getOwner();
         }

         return this.owner != null && !this.clone.isSitting() && this.clone.canAttack;
      }
   }

   protected abstract static class CloneGoal extends class_1352 {
      protected final CloneEntity clone;
      protected class_1657 owner;

      protected CloneGoal(CloneEntity clone) {
         this.clone = clone;
      }

      @Override
      public boolean method_6264() {
         if (this.owner == null) {
            this.owner = this.clone.getOwner();
         }

         return !this.clone.isSitting() && this.owner != null;
      }
   }

   protected static class DefendOwnerGoal extends CloneEntity.AssistOwnerGoal {
      public DefendOwnerGoal(CloneEntity clone) {
         super(clone);
      }

      @Override
      public boolean method_6264() {
         if (super.method_6264()
            && this.owner.method_6065() != null
            && this.clone.method_18395(this.owner.method_6065())
            && this.timer != this.owner.method_6117()) {
            this.field_6664 = this.owner.method_6065();
            this.timer = this.owner.method_6117();
            return true;
         } else {
            return false;
         }
      }
   }

   protected static class FightForOwnerGoal extends CloneEntity.AssistOwnerGoal {
      public FightForOwnerGoal(CloneEntity clone) {
         super(clone);
      }

      @Override
      public boolean method_6264() {
         if (super.method_6264()
            && this.owner.method_6052() != null
            && this.clone.method_18395(this.owner.method_6052())
            && this.timer != this.owner.method_6083()) {
            this.field_6664 = this.owner.method_6052();
            this.timer = this.owner.method_6083();
            return true;
         } else {
            return false;
         }
      }
   }

   protected static class FollowOwnerGoal extends CloneEntity.CloneGoal {
      private static final double MAX_DISTANCE = 32.0;
      private static final double MIN_DISTANCE = 6.0;
      private final double speed;
      private int tickTimer = 0;

      public FollowOwnerGoal(CloneEntity clone, double speed) {
         super(clone);
         this.speed = speed;
         this.method_6265(EnumSet.of(class_4134.field_18405, class_4134.field_18406));
      }

      public FollowOwnerGoal(CloneEntity clone) {
         this(clone, 1.0);
      }

      @Override
      public boolean method_6264() {
         return super.method_6264() && this.clone.followOwner && this.clone.method_5739(this.owner) >= 32.0;
      }

      @Override
      public void method_6269() {
         this.clone.method_5942().method_6335(this.owner, this.speed);
      }

      @Override
      public void method_6270() {
         this.clone.method_5942().method_6340();
      }

      @Override
      public boolean method_6266() {
         return super.method_6264() && this.clone.method_5739(this.owner) >= 6.0;
      }

      @Override
      public void method_6268() {
         this.clone.method_5988().method_6226(this.owner, 10.0F, this.clone.method_5978());
         if (!this.clone.method_5934() && !this.clone.method_5765() && --this.tickTimer <= 0) {
            this.tickTimer = this.method_38847(10);
            this.clone.method_5942().method_6335(this.owner, this.speed);
         }
      }
   }
}
