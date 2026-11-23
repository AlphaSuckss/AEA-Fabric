package dev.alphasucks.aea.port.entity.summon;

import dev.alphasucks.aea.port.Apoli;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1282;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1924;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5134;
import net.minecraft.class_6025;
import net.minecraft.class_8103;
import net.minecraft.class_1297.class_5529;
import net.minecraft.class_5132.class_5133;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MinionEntity extends class_1308 implements class_6025, Temporary {
   public static final class_2960 TEMPLATE_TEXTURE = Apoli.id("textures/entity/minion_template.png");
   private static final class_2940<Optional<UUID>> OWNER = class_2945.method_12791(MinionEntity.class, class_2943.field_13313);
   private static final class_2940<String> TEXTURE = class_2945.method_12791(MinionEntity.class, class_2943.field_13326);
   private static final class_2940<Boolean> FOLLOW = class_2945.method_12791(MinionEntity.class, class_2943.field_13323);
   private static final class_2940<Float> OFFSET_X = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> OFFSET_Y = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> OFFSET_Z = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> SCALE = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private int maxLifeTime = 1200;
   private int lifeTicks = 1200;
   @Nullable
   private class_2960 summonId;

   // class_6025.method_48926 and class_1297.method_37908 share a name in the original mappings; the
   // original class carried this javac-generated bridge
   @Override
   public class_1924 method_48926() {
      return super.method_37908();
   }

   public MinionEntity(class_1299<? extends MinionEntity> type, class_1937 level) {
      super(type, level);
      this.method_5971();
      this.method_5952(false);
      this.method_5880(false);
   }

   public static class_5133 createAttributes() {
      return class_1308.method_26828()
         .method_26868(class_5134.field_23716, 20.0)
         .method_26868(class_5134.field_23719, 0.0)
         .method_26868(class_5134.field_23717, 0.0);
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(OWNER, Optional.empty());
      this.field_6011.method_12784(TEXTURE, TEMPLATE_TEXTURE.toString());
      this.field_6011.method_12784(FOLLOW, false);
      this.field_6011.method_12784(OFFSET_X, 0.0F);
      this.field_6011.method_12784(OFFSET_Y, 0.0F);
      this.field_6011.method_12784(OFFSET_Z, 0.0F);
      this.field_6011.method_12784(SCALE, 1.0F);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (!this.method_29504()) {
            if (this.maxLifeTime > 0 && --this.lifeTicks <= 0) {
               Summons.expire(this);
            } else {
               if (this.isFollowingOwner()) {
                  class_1309 owner = this.method_35057();
                  if (owner == null || owner.method_37908() != this.method_37908()) {
                     Summons.expire(this);
                     return;
                  }

                  this.aimAt(owner.method_5791(), owner.method_36455());
                  Vector3f offset = this.getFollowOffset();
                  offset.rotateY(-owner.method_5791() * (float) (Math.PI / 180.0));
                  this.method_5814(owner.method_23317() + offset.x, owner.method_23318() + offset.y, owner.method_23321() + offset.z);
               }
            }
         }
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
      if (uuid == null) {
         return null;
      } else if (this.method_37908() instanceof class_3218 server) {
         return server.method_14190(uuid) instanceof class_1309 living ? living : null;
      } else {
         return this.method_37908().method_18470(uuid);
      }
   }

   public void setOwner(@Nullable class_1309 owner) {
      this.setOwnerUUID(owner == null ? null : owner.method_5667());
   }

   public void setTexture(class_2960 texture) {
      this.field_6011.method_12778(TEXTURE, texture.toString());
   }

   public class_2960 getTexture() {
      String raw = this.field_6011.method_12789(TEXTURE);
      class_2960 parsed = raw.isEmpty() ? null : class_2960.method_12829(raw);
      return parsed == null ? TEMPLATE_TEXTURE : parsed;
   }

   public void setScale(float scale) {
      this.field_6011.method_12778(SCALE, scale);
   }

   public float getMinionScale() {
      return this.field_6011.method_12789(SCALE);
   }

   public void setFollowingOwner(boolean follow) {
      this.field_6011.method_12778(FOLLOW, follow);
      this.method_5875(follow);
   }

   public boolean isFollowingOwner() {
      return this.field_6011.method_12789(FOLLOW);
   }

   public void setFollowOffset(double x, double y, double z) {
      this.field_6011.method_12778(OFFSET_X, (float)x);
      this.field_6011.method_12778(OFFSET_Y, (float)y);
      this.field_6011.method_12778(OFFSET_Z, (float)z);
   }

   public Vector3f getFollowOffset() {
      return new Vector3f(this.field_6011.method_12789(OFFSET_X), this.field_6011.method_12789(OFFSET_Y), this.field_6011.method_12789(OFFSET_Z));
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
   public void method_5674(class_2940<?> key) {
      if (SCALE.equals(key)) {
         this.method_18382();
      }

      super.method_5674(key);
   }

   @Override
   public boolean method_5810() {
      return false;
   }

   @Override
   public boolean method_5822() {
      return false;
   }

   public void aimAt(float yaw, float pitch) {
      this.method_36456(yaw);
      this.method_5847(yaw);
      this.method_36457(pitch);
      this.field_6283 = yaw;
   }

   @Override
   protected float method_6031(float movementYaw, float animStep) {
      this.field_6283 = this.field_6241;
      this.method_36456(this.field_6241);
      return animStep;
   }

   @Override
   public int method_5986() {
      return 180;
   }

   @Override
   public int method_5978() {
      return 90;
   }

   @Override
   public boolean method_5679(class_1282 source) {
      return this.method_5655() && !source.method_48789(class_8103.field_42242) ? true : super.method_5679(source);
   }

   @Override
   public void method_5650(class_5529 reason) {
      super.method_5650(reason);
      Summons.onRemoved(this);
   }

   @Override
   public void method_5652(class_2487 tag) {
      super.method_5652(tag);
      UUID owner = this.method_6139();
      if (owner != null) {
         tag.method_25927("Owner", owner);
      }

      tag.method_10582("Texture", this.field_6011.method_12789(TEXTURE));
      tag.method_10556("Follow", this.isFollowingOwner());
      Vector3f offset = this.getFollowOffset();
      tag.method_10548("OffsetX", offset.x);
      tag.method_10548("OffsetY", offset.y);
      tag.method_10548("OffsetZ", offset.z);
      tag.method_10548("MinionScale", this.getMinionScale());
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
      this.method_5880(false);
      if (tag.method_25928("Owner")) {
         this.setOwnerUUID(tag.method_25926("Owner"));
      }

      if (tag.method_10545("Texture")) {
         class_2960 parsed = class_2960.method_12829(tag.method_10558("Texture"));
         if (parsed != null) {
            this.setTexture(parsed);
         }
      }

      this.setFollowOffset(tag.method_10583("OffsetX"), tag.method_10583("OffsetY"), tag.method_10583("OffsetZ"));
      this.setFollowingOwner(tag.method_10577("Follow"));
      if (tag.method_10545("MinionScale")) {
         this.setScale(tag.method_10583("MinionScale"));
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
   }
}
