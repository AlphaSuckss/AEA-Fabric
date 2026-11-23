package dev.overgrown.sync.factory.action.entity.summons.entities.minion;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.utils.ExtraTameable;
import dev.overgrown.sync.factory.action.entity.summons.utils.Temporary;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1266;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1315;
import net.minecraft.class_1588;
import net.minecraft.class_1924;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_268;
import net.minecraft.class_2940;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3321;
import net.minecraft.class_3730;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import net.minecraft.class_5134;
import net.minecraft.class_5425;
import net.minecraft.class_5132.class_5133;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MinionEntity extends class_1308 implements ExtraTameable, Temporary {
   public static final class_2960 TEMPLATE_TEXTURE = Sync.identifier("textures/entity/minion_template.png");
   private static final class_2940<Optional<UUID>> OWNER_UUID = class_2945.method_12791(MinionEntity.class, class_2943.field_13313);
   private static final class_2940<String> TEXTURE_NAMESPACE = class_2945.method_12791(MinionEntity.class, class_2943.field_13326);
   private static final class_2940<String> TEXTURE_PATH = class_2945.method_12791(MinionEntity.class, class_2943.field_13326);
   private static final class_2940<Boolean> FOLLOW_OWNER = class_2945.method_12791(MinionEntity.class, class_2943.field_13323);
   private static final class_2940<Float> FOLLOW_OWNER_OFFSET_X = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> FOLLOW_OWNER_OFFSET_Y = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> FOLLOW_OWNER_OFFSET_Z = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   private static final class_2940<Float> SCALE = class_2945.method_12791(MinionEntity.class, class_2943.field_13320);
   protected int maxLifeTime;

   public MinionEntity(class_1299<? extends class_1308> entityType, class_1937 world) {
      super(entityType, world);
      this.method_5952(false);
      this.method_5971();
      this.method_5880(false);
      this.maxLifeTime = 1200;
   }

   @Override
   public class_1924 method_48926() {
      return this.method_37908();
   }

   @Override
   protected void method_5693() {
      super.method_5693();
      this.field_6011.method_12784(OWNER_UUID, Optional.empty());
      this.field_6011.method_12784(TEXTURE_NAMESPACE, TEMPLATE_TEXTURE.method_12836());
      this.field_6011.method_12784(TEXTURE_PATH, TEMPLATE_TEXTURE.method_12832());
      this.field_6011.method_12784(FOLLOW_OWNER, false);
      this.field_6011.method_12784(FOLLOW_OWNER_OFFSET_X, 0.0F);
      this.field_6011.method_12784(FOLLOW_OWNER_OFFSET_Y, 0.0F);
      this.field_6011.method_12784(FOLLOW_OWNER_OFFSET_Z, 0.0F);
      this.field_6011.method_12784(SCALE, 1.0F);
   }

   @Override
   public class_1315 method_5943(class_5425 world, class_1266 difficulty, class_3730 spawnReason, class_1315 entityData, class_2487 entityNbt) {
      class_1315 data = super.method_5943(world, difficulty, spawnReason, entityData, entityNbt);
      if (this.method_37908() instanceof class_3218 serverWorld
         && this.method_5781() == null
         && this.method_35057() != null
         && this.method_35057().method_5781() != null
         && this.method_35057().method_5781() instanceof class_268 implementedTeam) {
         serverWorld.method_14170().method_1172(this.method_5845(), implementedTeam);
      }

      return data;
   }

   public static class_5133 createMinionAttributes() {
      class_5133 attributes = class_1588.method_26828();
      attributes.method_26867(class_5134.field_23721);
      attributes.method_26868(class_5134.field_23716, 20.0);
      attributes.method_26868(class_5134.field_23719, 0.0);
      attributes.method_26868(class_5134.field_23717, 0.0);
      return attributes;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.field_6012 > this.maxLifeTime && this.maxLifeTime > 0) {
         this.method_5768();
      } else if (this.shouldFollowOwner()) {
         class_1309 owner = this.method_35057();
         if (owner == null || owner.method_37908().method_27983() != this.method_37908().method_27983()) {
            this.method_5768();
            return;
         }

         this.method_36456(owner.method_5791());
         this.method_5847(owner.method_5791());
         this.method_36457(owner.method_36455());
         Vector3f offsets = this.getFollowOwnerOffset();
         offsets.rotateY(-owner.method_5791() / (180.0F / (float)Math.PI));
         this.method_23327(owner.method_23317() + offsets.x, owner.method_23318() + offsets.y, owner.method_23321() + offsets.z);
      }
   }

   @Override
   public void setOwnerUUID(@Nullable UUID uuid) {
      if (uuid == null) {
         this.field_6011.method_12778(OWNER_UUID, Optional.empty());
      } else {
         this.field_6011.method_12778(OWNER_UUID, Optional.of(uuid));
      }
   }

   @Nullable
   @Override
   public UUID method_6139() {
      Optional<UUID> uuid = this.field_6011.method_12789(OWNER_UUID);
      return (UUID)uuid.orElse(null);
   }

   // Tameable's default only looks up players, but summon_minion accepts any living owner
   @Nullable
   @Override
   public class_1309 method_35057() {
      UUID uuid = this.method_6139();
      if (uuid == null) {
         return null;
      } else if (this.method_37908() instanceof class_3218 serverWorld) {
         return serverWorld.method_14190(uuid) instanceof class_1309 living ? living : null;
      } else {
         return this.method_37908().method_18470(uuid);
      }
   }

   @Override
   public boolean isOwned() {
      return this.field_6011.method_12789(OWNER_UUID).isPresent();
   }

   public void setTexture(class_2960 resourceLocation) {
      this.field_6011.method_12778(TEXTURE_NAMESPACE, resourceLocation.method_12836());
      this.field_6011.method_12778(TEXTURE_PATH, resourceLocation.method_12832());
   }

   public void setTexture(String resourceLocation) {
      try {
         class_2960 identifier = class_2960.method_12829(resourceLocation);
         if (identifier == null) {
            Sync.LOGGER.error("Invalid texture identifier: {}", resourceLocation);
            return;
         }

         this.setTexture(identifier);
      } catch (Exception e) {
         Sync.LOGGER.error("Failed to apply texture {} to MinionEntity.", resourceLocation, e);
      }
   }

   public class_2960 getTexture() {
      return class_2960.method_43902(this.field_6011.method_12789(TEXTURE_NAMESPACE), this.field_6011.method_12789(TEXTURE_PATH));
   }

   public void setScale(float amount) {
      this.field_6011.method_12778(SCALE, amount);
   }

   public float getScale() {
      return this.field_6011.method_12789(SCALE);
   }

   public void setFollowOwner(boolean shouldFollow) {
      this.field_6011.method_12778(FOLLOW_OWNER, shouldFollow);
      this.method_5875(shouldFollow);
   }

   public boolean shouldFollowOwner() {
      return this.field_6011.method_12789(FOLLOW_OWNER);
   }

   public void setFollowOwnerOffset(class_243 offset) {
      this.setFollowOwnerOffset((float)offset.field_1352, (float)offset.field_1351, (float)offset.field_1350);
   }

   public void setFollowOwnerOffset(float x, float y, float z) {
      this.field_6011.method_12778(FOLLOW_OWNER_OFFSET_X, x);
      this.field_6011.method_12778(FOLLOW_OWNER_OFFSET_Y, y);
      this.field_6011.method_12778(FOLLOW_OWNER_OFFSET_Z, z);
   }

   public Vector3f getFollowOwnerOffset() {
      return new Vector3f(
         this.field_6011.method_12789(FOLLOW_OWNER_OFFSET_X),
         this.field_6011.method_12789(FOLLOW_OWNER_OFFSET_Y),
         this.field_6011.method_12789(FOLLOW_OWNER_OFFSET_Z)
      );
   }

   @Override
   public void method_5674(class_2940<?> data) {
      if (SCALE.equals(data)) {
         this.method_18382();
      }

      super.method_5674(data);
   }

   @Override
   public class_4048 method_18377(class_4050 pose) {
      return super.method_18377(pose).method_18383(this.getScale());
   }

   @Override
   protected float method_18394(class_4050 pose, class_4048 dimensions) {
      return 0.5F * dimensions.field_18068;
   }

   @Override
   public void method_5749(class_2487 nbt) {
      super.method_5749(nbt);
      this.method_5971();
      this.method_5952(false);
      this.method_5880(false);
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

      if (nbt.method_10545("FollowOwner")) {
         this.setFollowOwner(nbt.method_10577("FollowOwner"));
         float xOffset = 0.0F;
         float yOffset = 0.0F;
         float zOffset = 0.0F;
         if (nbt.method_10545("FollowOffset")) {
            class_2487 offset = nbt.method_10562("FollowOffset");
            xOffset = offset.method_10583("X");
            yOffset = offset.method_10583("Y");
            zOffset = offset.method_10583("Z");
         }

         this.setFollowOwnerOffset(xOffset, yOffset, zOffset);
      } else {
         this.setFollowOwner(false);
      }

      if (nbt.method_10545("Texture")) {
         class_2487 compound = nbt.method_10562("Texture");
         String namespace = compound.method_10558("Namespace");
         String path = compound.method_10558("Path");
         if (!namespace.isEmpty() && !path.isEmpty()) {
            class_2960 identifier = class_2960.method_12829(namespace + ":" + path);
            if (identifier == null) {
               Sync.LOGGER.error("Invalid texture identifier from NBT: {}/{}", namespace, path);
               this.setTexture(TEMPLATE_TEXTURE);
            } else {
               this.setTexture(identifier);
            }
         }
      }

      if (nbt.method_10545("Scale")) {
         this.setScale(nbt.method_10583("Scale"));
      }

      if (nbt.method_10545("MaxLifetime")) {
         this.setMaxLifetime(nbt.method_10550("MaxLifetime"));
      }
   }

   @Override
   public void method_5652(class_2487 nbt) {
      super.method_5652(nbt);
      if (this.isOwned()) {
         nbt.method_25927("Owner", this.method_6139());
      }

      nbt.method_10556("FollowOwner", this.shouldFollowOwner());
      if (this.shouldFollowOwner()) {
         class_2487 offset = new class_2487();
         Vector3f offsetVector = this.getFollowOwnerOffset();
         offset.method_10548("X", offsetVector.x);
         offset.method_10548("Y", offsetVector.y);
         offset.method_10548("Z", offsetVector.z);
         nbt.method_10566("FollowOffset", offset);
      }

      nbt.method_10548("Scale", this.getScale());
      nbt.method_10569("MaxLifetime", this.maxLifeTime);
      class_2487 texture = new class_2487();
      texture.method_10582("Namespace", this.getTexture().method_12836());
      texture.method_10582("Path", this.getTexture().method_12832());
      nbt.method_10566("Texture", texture);
   }

   @Override
   public boolean method_5810() {
      return false;
   }

   @Override
   public void method_6005(double strength, double x, double z) {
   }

   @Override
   public void setMaxLifetime(int ticks) {
      this.maxLifeTime = ticks;
   }

   @Override
   public boolean method_5822() {
      return false;
   }

   @Override
   public boolean method_5863() {
      return this.shouldFollowOwner() ? false : super.method_5863();
   }
}
