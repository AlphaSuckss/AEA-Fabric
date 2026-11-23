package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Homing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1682;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_239.class_240;
import org.jetbrains.annotations.Nullable;

public class CustomProjectileEntity extends class_1682 {
   private static final class_2940<String> TEXTURE = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13326);
   private static final class_2940<String> MODEL_POWER = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13326);
   private static final class_2940<class_1799> ITEM = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13322);
   private static final class_2940<Float> RETURN_PULL = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13320);
   private static final class_2940<Integer> HOMING_TARGET = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13327);
   private static final class_2940<Float> HOMING_TURN = class_2945.method_12791(CustomProjectileEntity.class, class_2943.field_13320);
   @Nullable
   private ProjectileHoming homing;
   private String modelPowersRaw = "";
   private List<class_2960> modelPowers = List.of();

   public CustomProjectileEntity(class_1299<? extends CustomProjectileEntity> type, class_1937 level) {
      super(type, level);
   }

   public CustomProjectileEntity(class_1299<? extends CustomProjectileEntity> type, class_1297 owner, class_1937 level) {
      super(type, level);
      this.method_7432(owner);
      this.method_5814(owner.method_23317(), owner.method_23320() - 0.1, owner.method_23321());
   }

   protected void method_5693() {
      this.field_6011.method_12784(TEXTURE, "");
      this.field_6011.method_12784(MODEL_POWER, "");
      this.field_6011.method_12784(ITEM, class_1799.field_8037);
      this.field_6011.method_12784(RETURN_PULL, 0.0F);
      this.field_6011.method_12784(HOMING_TARGET, -1);
      this.field_6011.method_12784(HOMING_TURN, 0.0F);
   }

   public void setTexture(class_2960 texture) {
      this.field_6011.method_12778(TEXTURE, texture == null ? "" : texture.toString());
   }

   public class_2960 getTexture() {
      String s = this.field_6011.method_12789(TEXTURE);
      return s.isEmpty() ? null : class_2960.method_12829(s);
   }

   public void setItem(class_1799 stack) {
      this.field_6011.method_12778(ITEM, stack == null ? class_1799.field_8037 : stack.method_7972());
   }

   public class_1799 getItem() {
      return this.field_6011.method_12789(ITEM);
   }

   public void setModelPowers(List<class_2960> powerIds) {
      StringBuilder joined = new StringBuilder();

      for (int i = 0; i < powerIds.size(); i++) {
         if (i > 0) {
            joined.append(',');
         }

         joined.append(powerIds.get(i));
      }

      this.field_6011.method_12778(MODEL_POWER, joined.toString());
   }

   public List<class_2960> getModelPowers() {
      String raw = this.field_6011.method_12789(MODEL_POWER);
      if (!raw.equals(this.modelPowersRaw)) {
         this.modelPowersRaw = raw;
         this.modelPowers = parseModelPowers(raw);
      }

      return this.modelPowers;
   }

   private static List<class_2960> parseModelPowers(String raw) {
      if (raw.isEmpty()) {
         return List.of();
      }

      String[] parts = raw.split(",");
      List<class_2960> ids = new ArrayList(parts.length);

      for (String part : parts) {
         class_2960 id = class_2960.method_12829(part);
         if (id != null) {
            ids.add(id);
         }
      }

      return List.copyOf(ids);
   }

   public boolean isReturning() {
      return this.field_6011.method_12789(RETURN_PULL) > 0.0F;
   }

   public void startReturning(float pull) {
      this.field_6011.method_12778(RETURN_PULL, pull);
      this.method_5875(true);
      this.homing = null;
      this.field_6011.method_12778(HOMING_TARGET, -1);
   }

   public void startHoming(Homing config, class_1297 shooter) {
      this.homing = new ProjectileHoming(config, shooter);
      this.field_6011.method_12778(HOMING_TURN, this.homing.turnRate());
   }

   public boolean canTarget(class_1297 entity) {
      return this.method_26958(entity);
   }

   @Override
   public void method_5773() {
      float pull = this.field_6011.method_12789(RETURN_PULL);
      if (pull > 0.0F) {
         if (!this.steerHome(pull)) {
            return;
         }
      } else {
         this.seek();
      }

      super.method_5773();
   }

   private void seek() {
      int targetId = this.field_6011.method_12789(HOMING_TARGET);
      if (this.homing != null && !this.method_37908().field_9236) {
         int next = this.homing.update(this, targetId);
         if (next == -2) {
            this.homing = null;
            next = -1;
         }

         if (next != targetId) {
            this.field_6011.method_12778(HOMING_TARGET, next);
            this.field_6007 = true;
            targetId = next;
         }
      }

      if (targetId >= 0) {
         class_1297 target = this.method_37908().method_8469(targetId);
         if (target != null) {
            ProjectileHoming.steer(this, target, this.field_6011.method_12789(HOMING_TURN));
         }
      }
   }

   private boolean steerHome(float pull) {
      class_1297 owner = this.method_24921();
      boolean server = !this.method_37908().field_9236;
      if (owner != null && owner.method_5805() && !owner.method_7325() && owner.method_37908() == this.method_37908()) {
         class_243 toOwner = owner.method_33571().method_1020(this.method_19538());
         if (server && toOwner.method_1027() <= Math.max(1.0, this.method_18798().method_1027())) {
            ((ProjectileHitActions)this).apoli$caught();
            this.method_31472();
            return false;
         }

         this.method_23327(this.method_23317(), this.method_23318() + toOwner.field_1351 * 0.015 * pull, this.method_23321());
         if (!server) {
            this.field_5971 = this.method_23318();
         }

         this.method_18799(this.method_18798().method_1021(0.95).method_1019(toOwner.method_1029().method_1021(0.05 * pull)));
         return true;
      } else {
         if (!server) {
            return true;
         }

         this.method_31472();
         return false;
      }
   }

   protected void method_5852() {
      if (!this.isReturning()) {
         super.method_5852();
      }
   }

   protected void method_7488(class_239 result) {
      if (!this.isReturning() || result.method_17783() != class_240.field_1332) {
         super.method_7488(result);
         if (!this.method_37908().field_9236 && !this.isReturning() && !((ProjectileHitActions)this).apoli$keepsFlying()) {
            this.method_31472();
         }
      }
   }

   protected void method_5652(class_2487 tag) {
      super.method_5652(tag);
      tag.method_10582("Texture", this.field_6011.method_12789(TEXTURE));
      tag.method_10582("ModelPower", this.field_6011.method_12789(MODEL_POWER));
   }

   protected void method_5749(class_2487 tag) {
      super.method_5749(tag);
      this.field_6011.method_12778(TEXTURE, tag.method_10558("Texture"));
      this.field_6011.method_12778(MODEL_POWER, tag.method_10558("ModelPower"));
   }
}
