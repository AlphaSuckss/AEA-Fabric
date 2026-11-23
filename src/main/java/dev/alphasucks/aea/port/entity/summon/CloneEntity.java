package dev.alphasucks.aea.port.entity.summon;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1347;
import net.minecraft.class_1352;
import net.minecraft.class_1361;
import net.minecraft.class_1366;
import net.minecraft.class_1376;
import net.minecraft.class_1380;
import net.minecraft.class_1383;
import net.minecraft.class_1399;
import net.minecraft.class_1405;
import net.minecraft.class_1588;
import net.minecraft.class_1657;
import net.minecraft.class_1665;
import net.minecraft.class_1675;
import net.minecraft.class_1676;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1924;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_3417;
import net.minecraft.class_3419;
import net.minecraft.class_3745;
import net.minecraft.class_5134;
import net.minecraft.class_6025;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_1665.class_1666;
import net.minecraft.class_5132.class_5133;
import org.jetbrains.annotations.Nullable;

public class CloneEntity extends class_1588 implements class_6025, class_3745, Temporary {
   private static final double COMBAT_SPEED = 1.35;
   private static final float SHOOTING_RANGE = 32.0F;
   private static final class_2940<Optional<UUID>> OWNER = class_2945.method_12791(CloneEntity.class, class_2943.field_13313);
   private static final class_2940<Boolean> SLIM = class_2945.method_12791(CloneEntity.class, class_2943.field_13323);
   private static final class_2940<Boolean> SITTING = class_2945.method_12791(CloneEntity.class, class_2943.field_13323);
   private static final class_2940<Boolean> CHARGING = class_2945.method_12791(CloneEntity.class, class_2943.field_13323);
   private static final class_2940<String> WIDE_TEXTURE = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);
   private static final class_2940<String> SLIM_TEXTURE = class_2945.method_12791(CloneEntity.class, class_2943.field_13326);
   private boolean canAttack = true;
   private boolean canSit = true;
   private boolean followOwner = true;
   private int maxLifeTime = 1200;
   private int lifeTicks = 1200;
   @Nullable
   private class_2960 summonId;
   private class_1366 meleeGoal;
   private class_1380<CloneEntity> bowGoal;
   private class_1383<CloneEntity> crossbowGoal;

   // class_6025.method_48926 and class_1297.method_37908 share a name in the original mappings; the
   // original class carried this javac-generated bridge
   @Override
   public class_1924 method_48926() {
      return super.method_37908();
   }

   public CloneEntity(class_1299<? extends CloneEntity> type, class_1937 level) {
      super(type, level);
      this.method_5971();
      this.method_5952(false);
      this.field_6194 = 0;
   }

   public static class_5133 createAttributes() {
      return class_1588.method_26918()
         .method_26868(class_5134.field_23716, 20.0)
         .method_26868(class_5134.field_23719, 0.35)
         .method_26868(class_5134.field_23717, 64.0)
         .method_26868(class_5134.field_23721, 2.0);
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(OWNER, Optional.empty());
      this.field_6011.method_12784(SLIM, false);
      this.field_6011.method_12784(SITTING, false);
      this.field_6011.method_12784(CHARGING, false);
      this.field_6011.method_12784(WIDE_TEXTURE, "");
      this.field_6011.method_12784(SLIM_TEXTURE, "");
   }

   @Override
   protected void method_5959() {
      this.meleeGoal = new class_1366(this, 1.35, false);
      this.bowGoal = new class_1380<>(this, 1.35, 20, 32.0F);
      this.crossbowGoal = new class_1383<>(this, 1.35, 32.0F);
      this.field_6201.method_6277(0, new class_1347(this));
      this.field_6201.method_6277(2, new CloneEntity.FollowOwnerGoal(this));
      this.field_6201.method_6277(4, new class_1361(this, class_1657.class, 16.0F));
      this.field_6201.method_6277(6, new class_1376(this));
      this.field_6185.method_6277(0, new CloneEntity.DefendOwnerGoal(this));
      this.field_6185.method_6277(1, new CloneEntity.FightForOwnerGoal(this));
      this.field_6185.method_6277(2, new class_1399(this));
      this.updateWeaponGoals();
   }

   public void updateWeaponGoals() {
      if (this.method_37908() != null && !this.method_37908().field_9236 && this.meleeGoal != null) {
         this.field_6201.method_6280(this.meleeGoal);
         this.field_6201.method_6280(this.bowGoal);
         this.field_6201.method_6280(this.crossbowGoal);
         if (this.method_24518(class_1802.field_8102)) {
            this.field_6201.method_6277(1, this.bowGoal);
         } else if (this.method_24520(is -> is.method_7909() instanceof class_1764)) {
            this.field_6201.method_6277(1, this.crossbowGoal);
         } else {
            this.field_6201.method_6277(1, this.meleeGoal);
         }
      }
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (!this.method_29504()) {
            class_1309 owner = this.method_35057();
            boolean expired = this.maxLifeTime > 0 && --this.lifeTicks <= 0;
            if (owner == null || owner.method_37908() != this.method_37908() || expired) {
               Summons.expire(this);
            }
         }
      }
   }

   @Override
   protected boolean method_6062() {
      return super.method_6062() || this.isSitting();
   }

   @Override
   protected class_1269 method_5992(class_1657 player, class_1268 hand) {
      if (player == this.method_35057() && this.canSit) {
         if (!this.method_37908().field_9236) {
            this.toggleSitting();
         }

         return class_1269.method_29236(this.method_37908().field_9236);
      } else {
         return super.method_5992(player, hand);
      }
   }

   public void method_7105(class_1309 target, float pullProgress) {
      if (this.method_24520(is -> is.method_7909() instanceof class_1764)) {
         this.method_24654(this, 1.6F);
      } else {
         class_1799 bow = this.method_5998(class_1675.method_18812(this, class_1802.field_8102));
         class_1799 arrowStack = this.method_18808(bow);
         class_1665 arrow = class_1675.method_18813(this, arrowStack, pullProgress);
         arrow.field_7572 = class_1666.field_7592;
         double dx = target.method_23317() - this.method_23317();
         double dy = target.method_23323(0.3333333333333333) - arrow.method_23318();
         double dz = target.method_23321() - this.method_23321();
         double horizontal = Math.sqrt(dx * dx + dz * dz);
         arrow.method_7485(dx, dy + horizontal * 0.2, dz, 1.6F, 14 - this.method_37908().method_8407().method_5461() * 4);
         this.method_5783(class_3417.field_14600, 1.0F, 1.0F / (this.method_6051().method_43057() * 0.4F + 1.2F) + pullProgress * 0.5F);
         this.method_37908().method_8649(arrow);
      }
   }

   @Override
   public void method_7110(boolean charging) {
      this.field_6011.method_12778(CHARGING, charging);
   }

   public boolean isCharging() {
      return this.field_6011.method_12789(CHARGING);
   }

   @Override
   public void method_18811(class_1309 target, class_1799 crossbow, class_1676 projectile, float spray) {
      this.method_24652(this, target, projectile, spray, 1.6F);
   }

   @Override
   public void method_24651() {
      this.field_6278 = 0;
   }

   public boolean canTargetEntity(@Nullable class_1309 target) {
      class_1309 owner = this.method_35057();
      if (owner == null || target == null || !this.canAttack || this.isSitting() || target == owner || target == this) {
         return false;
      } else {
         return !target.method_33190() ? false : !(target instanceof class_6025 ownable && ownable.method_35057() == owner);
      }
   }

   @Nullable
   @Override
   public UUID method_6139() {
      return (UUID)this.field_6011.method_12789(OWNER).orElse(null);
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.field_6011.method_12778(OWNER, Optional.ofNullable(uuid));
   }

   @Nullable
   @Override
   public class_1309 method_35057() {
      UUID uuid = this.method_6139();
      return uuid == null ? null : this.method_37908().method_18470(uuid);
   }

   public void setOwner(@Nullable class_1657 owner) {
      this.setOwnerUUID(owner == null ? null : owner.method_5667());
   }

   public boolean isOwned() {
      return this.field_6011.method_12789(OWNER).isPresent();
   }

   public void setSlim(boolean slim) {
      this.field_6011.method_12778(SLIM, slim);
   }

   public boolean isSlim() {
      return this.field_6011.method_12789(SLIM);
   }

   public boolean isSitting() {
      return this.field_6011.method_12789(SITTING);
   }

   public void setSitting(boolean sitting) {
      this.field_6011.method_12778(SITTING, sitting);
   }

   public void toggleSitting() {
      this.setSitting(!this.isSitting());
      if (this.isSitting()) {
         this.method_5980(null);
         this.method_5942().method_6340();
      }
   }

   public void setWideTexture(@Nullable class_2960 texture) {
      this.field_6011.method_12778(WIDE_TEXTURE, texture == null ? "" : texture.toString());
   }

   public void setSlimTexture(@Nullable class_2960 texture) {
      this.field_6011.method_12778(SLIM_TEXTURE, texture == null ? "" : texture.toString());
   }

   @Nullable
   public class_2960 getCustomTexture(boolean slim) {
      class_2960 wide = parseTexture(this.field_6011.method_12789(WIDE_TEXTURE));
      class_2960 slimTex = parseTexture(this.field_6011.method_12789(SLIM_TEXTURE));
      if (slim) {
         return slimTex != null ? slimTex : wide;
      } else {
         return wide != null ? wide : slimTex;
      }
   }

   @Nullable
   private static class_2960 parseTexture(String raw) {
      return raw.isEmpty() ? null : class_2960.method_12829(raw);
   }

   public void setCanAttack(boolean canAttack) {
      this.canAttack = canAttack;
   }

   public void setCanSit(boolean canSit) {
      this.canSit = canSit;
   }

   public void setFollowOwner(boolean followOwner) {
      this.followOwner = followOwner;
   }

   public void setSummonId(@Nullable class_2960 id) {
      this.summonId = id;
   }

   @Nullable
   @Override
   public class_2960 getSummonId() {
      return this.summonId;
   }

   @Override
   public void setMaxLifeTime(int ticks) {
      this.maxLifeTime = ticks;
      this.lifeTicks = ticks;
   }

   @Override
   public int getMaxLifeTime() {
      return this.maxLifeTime;
   }

   @Override
   public int getRemainingLifeTime() {
      return this.maxLifeTime > 0 ? this.lifeTicks : -1;
   }

   @Override
   protected boolean method_23734() {
      return false;
   }

   @Override
   public boolean method_5822() {
      return false;
   }

   @Override
   public void method_5650(class_5529 reason) {
      super.method_5650(reason);
      Summons.onRemoved(this);
   }

   @Override
   public class_3419 method_5634() {
      return class_3419.field_15248;
   }

   @Override
   public void method_5652(class_2487 tag) {
      super.method_5652(tag);
      UUID owner = this.method_6139();
      if (owner != null) {
         tag.method_25927("Owner", owner);
      }

      tag.method_10556("Slim", this.isSlim());
      tag.method_10556("Sitting", this.isSitting());
      tag.method_10582("WideTexture", this.field_6011.method_12789(WIDE_TEXTURE));
      tag.method_10582("SlimTexture", this.field_6011.method_12789(SLIM_TEXTURE));
      tag.method_10556("CanAttack", this.canAttack);
      tag.method_10556("CanSit", this.canSit);
      tag.method_10556("FollowOwner", this.followOwner);
      tag.method_10569("MaxLifeTime", this.maxLifeTime);
      tag.method_10569("LifeTicks", this.lifeTicks);
      if (this.summonId != null) {
         tag.method_10582("SummonId", this.summonId.toString());
      }
   }

   @Override
   public void method_5749(class_2487 tag) {
      super.method_5749(tag);
      this.method_5971();
      this.method_5952(false);
      if (tag.method_25928("Owner")) {
         this.setOwnerUUID(tag.method_25926("Owner"));
      }

      this.setSlim(tag.method_10577("Slim"));
      this.setSitting(tag.method_10577("Sitting"));
      this.field_6011.method_12778(WIDE_TEXTURE, tag.method_10558("WideTexture"));
      this.field_6011.method_12778(SLIM_TEXTURE, tag.method_10558("SlimTexture"));
      if (tag.method_10545("CanAttack")) {
         this.canAttack = tag.method_10577("CanAttack");
      }

      if (tag.method_10545("CanSit")) {
         this.canSit = tag.method_10577("CanSit");
      }

      if (tag.method_10545("FollowOwner")) {
         this.followOwner = tag.method_10577("FollowOwner");
      }

      if (tag.method_10545("MaxLifeTime")) {
         this.setMaxLifeTime(tag.method_10550("MaxLifeTime"));
      }

      if (tag.method_10545("LifeTicks")) {
         this.lifeTicks = tag.method_10550("LifeTicks");
      }

      if (tag.method_10545("SummonId")) {
         this.summonId = class_2960.method_12829(tag.method_10558("SummonId"));
      }

      this.updateWeaponGoals();
   }

   static class DefendOwnerGoal extends CloneEntity.OwnerTargetGoal {
      DefendOwnerGoal(CloneEntity clone) {
         super(clone);
      }

      @Override
      public boolean method_6264() {
         class_1309 owner = this.clone.method_35057();
         if (owner == null) {
            return false;
         } else {
            class_1309 attacker = owner.method_6065();
            int time = owner.method_6117();
            if (attacker != null && time != this.timestamp && this.clone.canTargetEntity(attacker)) {
               this.candidate = attacker;
               this.timestamp = time;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   static class FightForOwnerGoal extends CloneEntity.OwnerTargetGoal {
      FightForOwnerGoal(CloneEntity clone) {
         super(clone);
      }

      @Override
      public boolean method_6264() {
         class_1309 owner = this.clone.method_35057();
         if (owner == null) {
            return false;
         } else {
            class_1309 victim = owner.method_6052();
            int time = owner.method_6083();
            if (victim != null && time != this.timestamp && this.clone.canTargetEntity(victim)) {
               this.candidate = victim;
               this.timestamp = time;
               return true;
            } else {
               return false;
            }
         }
      }
   }

   static class FollowOwnerGoal extends class_1352 {
      private static final double MAX_DISTANCE = 32.0;
      private static final double MIN_DISTANCE = 6.0;
      private final CloneEntity clone;
      private class_1309 owner;
      private int repathCooldown;

      FollowOwnerGoal(CloneEntity clone) {
         this.clone = clone;
         this.method_6265(EnumSet.of(class_4134.field_18405, class_4134.field_18406));
      }

      @Override
      public boolean method_6264() {
         this.owner = this.clone.method_35057();
         return !this.clone.isSitting() && this.clone.followOwner && this.owner != null && this.clone.method_5739(this.owner) >= 32.0;
      }

      @Override
      public boolean method_6266() {
         return !this.clone.isSitting() && this.owner != null && this.clone.followOwner && this.clone.method_5739(this.owner) >= 6.0;
      }

      @Override
      public void method_6269() {
         this.repathCooldown = 0;
      }

      @Override
      public void method_6270() {
         this.owner = null;
         this.clone.method_5942().method_6340();
      }

      @Override
      public void method_6268() {
         if (this.owner != null) {
            this.clone.method_5988().method_6226(this.owner, 10.0F, this.clone.method_5978());
            if (--this.repathCooldown <= 0) {
               this.repathCooldown = 10;
               this.clone.method_5942().method_6335(this.owner, 1.35);
            }
         }
      }
   }

   abstract static class OwnerTargetGoal extends class_1405 {
      protected final CloneEntity clone;
      @Nullable
      protected class_1309 candidate;
      protected int timestamp;

      OwnerTargetGoal(CloneEntity clone) {
         super(clone, false);
         this.clone = clone;
         this.method_6265(EnumSet.of(class_4134.field_18408));
      }

      @Override
      public void method_6269() {
         this.clone.method_5980(this.candidate);
         super.method_6269();
      }
   }
}
