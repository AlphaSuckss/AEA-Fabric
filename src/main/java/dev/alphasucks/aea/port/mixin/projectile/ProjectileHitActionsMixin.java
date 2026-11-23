package dev.alphasucks.aea.port.mixin.projectile;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.entity.CustomProjectileEntity;
import dev.alphasucks.aea.port.entity.ProjectileHitActions;
import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDamageHandler;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Config;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Hooks;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Reflect;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Return;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1676.class)
public abstract class ProjectileHitActionsMixin implements ProjectileHitActions {
   @Unique
   private static final double APOLI$ENTITY_HIT_MARGIN = 0.3;
   @Unique
   private static final double APOLI$FACE_EPSILON = 1.0E-4;
   @Unique
   private static final double APOLI$STAND_OFF = 0.05;
   @Unique
   private static final class_243 APOLI$WEST = new class_243(-1.0, 0.0, 0.0);
   @Unique
   private static final class_243 APOLI$EAST = new class_243(1.0, 0.0, 0.0);
   @Unique
   private static final class_243 APOLI$NORTH = new class_243(0.0, 0.0, -1.0);
   @Unique
   private static final class_243 APOLI$SOUTH = new class_243(0.0, 0.0, 1.0);
   @Unique
   private static final class_243 APOLI$DOWN = new class_243(0.0, -1.0, 0.0);
   @Unique
   private static final class_243 APOLI$UP = new class_243(0.0, 1.0, 0.0);
   @Unique
   @Nullable
   private Config apoli$fireConfig;
   @Unique
   private boolean apoli$missFired;
   @Unique
   private double apoli$maxRange;
   @Unique
   private double apoli$travelled;
   @Unique
   private int apoli$lifetime;
   @Unique
   private int apoli$age;
   @Unique
   private boolean apoli$lastSet;
   @Unique
   private double apoli$lastX;
   @Unique
   private double apoli$lastY;
   @Unique
   private double apoli$lastZ;
   @Unique
   @Nullable
   private class_1297 apoli$causeHolder;
   @Unique
   @Nullable
   private class_2960 apoli$causePower;
   @Unique
   private int apoli$bounces;
   @Unique
   private boolean apoli$keepsFlying;
   @Unique
   private int apoli$flightTicks;
   @Unique
   private int apoli$lastHit = -1;
   @Unique
   private int apoli$pierced;
   @Unique
   @Nullable
   private IntArrayList apoli$passed;
   @Unique
   private int apoli$bouncedOff = -1;

   @Override
   public void apoli$setFireConfig(Config config) {
      this.apoli$fireConfig = config;
   }

   @Override
   public void apoli$setMaxRange(double blocks) {
      this.apoli$maxRange = blocks > 0.0 ? blocks : 0.0;
   }

   @Override
   public void apoli$setLifetime(int ticks) {
      this.apoli$lifetime = Math.max(0, ticks);
   }

   @Override
   public void apoli$setFireCause(@Nullable class_1297 holder, @Nullable class_2960 powerId) {
      this.apoli$causeHolder = holder;
      this.apoli$causePower = powerId;
   }

   @Override
   public boolean apoli$keepsFlying() {
      return this.apoli$keepsFlying;
   }

   @Override
   public void apoli$caught() {
      Config config = this.apoli$fireConfig;
      if (config != null) {
         Return ret = (Return)config.returning().orElse(null);
         if (ret != null) {
            this.apoli$runOwnerAction((class_1676)(Object)this, ret.bientityActionOnCatch());
         }
      }
   }

   @Inject(method = "method_5773()V", at = @At("HEAD"))
   private void apoli$tickFired(CallbackInfo ci) {
      Config config = this.apoli$fireConfig;
      if (config != null) {
         class_1676 self = (class_1676)(Object)this;
         if (!self.method_37908().method_8608() && !self.method_31481()) {
            if (this.apoli$lifetime > 0 && ++this.apoli$age > this.apoli$lifetime) {
               this.apoli$runOwnerAction(self, config.hooks().bientityActionOnExpire());
               self.method_31472();
            } else {
               CustomProjectileEntity custom = self instanceof CustomProjectileEntity projectile ? projectile : null;
               if (custom == null || !custom.isReturning()) {
                  Return ret = custom == null ? null : (Return)config.returning().orElse(null);
                  if (ret == null
                     || ret.after() <= 0
                     || ++this.apoli$flightTicks < ret.after()
                     || !apoli$mayTurn(self, ret)
                     || !this.apoli$startReturn(self, custom, ret, null, false)) {
                     if (!(this.apoli$maxRange <= 0.0)) {
                        if (!this.apoli$lastSet) {
                           this.apoli$lastSet = true;
                           this.apoli$lastX = self.method_23317();
                           this.apoli$lastY = self.method_23318();
                           this.apoli$lastZ = self.method_23321();
                        } else {
                           double dx = self.method_23317() - this.apoli$lastX;
                           double dy = self.method_23318() - this.apoli$lastY;
                           double dz = self.method_23321() - this.apoli$lastZ;
                           this.apoli$lastX = self.method_23317();
                           this.apoli$lastY = self.method_23318();
                           this.apoli$lastZ = self.method_23321();
                           this.apoli$travelled = this.apoli$travelled + Math.sqrt(dx * dx + dy * dy + dz * dz);
                           if (!(this.apoli$travelled <= this.apoli$maxRange)) {
                              if (ret != null) {
                                 if (!apoli$mayTurn(self, ret)) {
                                    return;
                                 }

                                 if (this.apoli$startReturn(self, custom, ret, null, false)) {
                                    return;
                                 }
                              }

                              this.apoli$runOwnerAction(self, config.hooks().bientityActionOnExpire());
                              self.method_31472();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Unique
   private static boolean apoli$mayTurn(class_1676 self, Return ret) {
      if (ret.condition().isEmpty()) {
         return true;
      }

      class_1297 owner = self.method_24921();
      return owner == null || ((BiEntityCondition)ret.condition().get()).test(BiEntityCtx.of(owner, self, self.method_37908()));
   }

   @ModifyReturnValue(method = "method_26958(Lnet/minecraft/class_1297;)Z", at = @At("RETURN"))
   private boolean apoli$filterHitTargets(boolean original, class_1297 target) {
      Config config = this.apoli$fireConfig;
      if (original && config != null) {
         class_1676 self = (class_1676)(Object)this;
         class_1937 level = self.method_37908();
         if (level.method_8608()) {
            return true;
         }

         int targetId = target.method_5628();
         if (targetId != this.apoli$bouncedOff && (this.apoli$passed == null || !this.apoli$passed.contains(targetId))) {
            if (self instanceof CustomProjectileEntity custom && custom.isReturning()) {
               Return ret = (Return)config.returning().orElse(null);
               if (ret == null || !ret.hitWhileReturning() || target == self.method_24921() || target.method_5628() == this.apoli$lastHit) {
                  return false;
               }
            }

            Hooks hooks = config.hooks();
            return hooks.bientityCondition().isPresent() && !((BiEntityCondition)hooks.bientityCondition().get()).test(BiEntityCtx.of(self, target, level))
               ? false
               : hooks.ownerBientityCondition().isEmpty()
                  || ((BiEntityCondition)hooks.ownerBientityCondition().get()).test(BiEntityCtx.of(self.method_24921(), target, level));
         } else {
            return false;
         }
      } else {
         return original;
      }
   }

   @Inject(method = "method_7488(Lnet/minecraft/class_239;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$runHitHooks(class_239 result, CallbackInfo ci) {
      this.apoli$keepsFlying = false;
      Config config = this.apoli$fireConfig;
      if (config != null) {
         class_1676 self = (class_1676)(Object)this;
         class_1937 level = self.method_37908();
         if (!level.method_8608()) {
            Hooks hooks = config.hooks();
            boolean entityHit = result.method_17783() == class_240.field_1331;
            ModifyProjectileDamageHandler.beginProjectileContext(self);
            boolean attributed = PowerCause.push(this.apoli$causeHolder, this.apoli$causePower);

            try {
               if (entityHit) {
                  class_1297 target = ((class_3966)result).method_17782();
                  if (target != null) {
                     this.apoli$lastHit = target.method_5628();
                     hooks.bientityActionOnHit().ifPresent(a -> a.run(BiEntityCtx.of(self, target, level)));
                     hooks.ownerTargetBientityActionOnHit().ifPresent(a -> a.run(BiEntityCtx.of(self.method_24921(), target, level)));
                     if (!self.method_31481()
                        && !apoli$returning(self)
                        && (this.apoli$bounceOffEntity(self, level, config, target) || this.apoli$pierce(self, config, target))) {
                        this.apoli$keepsFlying = true;
                     }
                  }
               } else if (this.apoli$tryBounce(self, level, config, result)) {
                  this.apoli$keepsFlying = true;
               } else {
                  this.apoli$runMissHooks(self, level, hooks, config.params().blockActionCancelsMissAction(), result);
               }
            } finally {
               if (attributed) {
                  PowerCause.pop();
               }

               ModifyProjectileDamageHandler.endProjectileContext();
            }

            if (this.apoli$keepsFlying) {
               ci.cancel();
            } else {
               if (self instanceof CustomProjectileEntity custom && !custom.isReturning()) {
                  Return ret = (Return)config.returning().orElse(null);
                  if (ret == null) {
                     return;
                  }

                  if (entityHit ? ret.onHitEntity() : ret.onHitBlock() && result instanceof class_3965) {
                     this.apoli$startReturn(self, custom, ret, entityHit ? null : (class_3965)result, entityHit);
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean apoli$startReturn(class_1676 self, CustomProjectileEntity custom, Return ret, @Nullable class_3965 blockHit, boolean afterEntityHit) {
      class_1297 owner = self.method_24921();
      if (owner == null) {
         return false;
      }

      float pull = (float)ret.speed().eval(owner);
      if (!(pull > 0.0F)) {
         return false;
      }

      if (blockHit != null) {
         apoli$standOff(self, blockHit);
         self.method_18799(class_243.field_1353);
      }

      custom.startReturning(pull);
      self.field_6007 = true;
      this.apoli$newLeg();
      if (!afterEntityHit) {
         this.apoli$lastHit = -1;
      }

      this.apoli$runOwnerAction(self, ret.bientityActionOnReturn());
      return true;
   }

   @Unique
   private void apoli$newLeg() {
      if (this.apoli$passed != null) {
         this.apoli$passed.clear();
      }

      this.apoli$bouncedOff = -1;
   }

   @Unique
   private static boolean apoli$returning(class_1676 self) {
      return self instanceof CustomProjectileEntity custom && custom.isReturning();
   }

   @Unique
   private boolean apoli$pierce(class_1676 self, Config config, class_1297 target) {
      int limit = config.pierce().evalInt(self.method_24921());
      if (limit != 0 && (limit <= 0 || this.apoli$pierced < limit)) {
         this.apoli$pierced++;
         if (this.apoli$passed == null) {
            this.apoli$passed = new IntArrayList(4);
         }

         this.apoli$passed.add(target.method_5628());
         return true;
      } else {
         return false;
      }
   }

   @Unique
   private boolean apoli$bounceOffEntity(class_1676 self, class_1937 level, Config config, class_1297 target) {
      Reflect reflect = (Reflect)config.reflective().orElse(null);
      if (reflect != null && reflect.entities()) {
         class_1297 owner = self.method_24921();
         int limit = reflect.maxBounces().evalInt(owner);
         if (limit >= 0 && this.apoli$bounces >= limit) {
            return false;
         }

         class_243 velocity = self.method_18798();
         class_243 start = self.method_19538();
         class_238 box = target.method_5829().method_1014(0.3);
         class_243 entry = (class_243)box.method_992(start, start.method_1019(velocity)).orElse(null);
         class_243 normal = entry == null ? null : apoli$faceNormal(box, entry);
         if (normal == null) {
            class_243 center = box.method_1005();
            normal = new class_243(start.field_1352 - center.field_1352, 0.0, start.field_1350 - center.field_1350);
            normal = normal.method_1027() < 1.0E-8 ? velocity.method_1021(-1.0) : normal;
            if (normal.method_1027() < 1.0E-12) {
               return false;
            }

            normal = normal.method_1029();
         }

         double dot = velocity.method_1026(normal);
         class_243 reflected = (dot < 0.0 ? velocity.method_1020(normal.method_1021(2.0 * dot)) : velocity).method_1021(reflect.speed().eval(owner));
         if (reflected.method_1027() < 1.0E-6) {
            return false;
         }

         this.apoli$bounces++;
         this.apoli$bouncedOff = target.method_5628();
         class_243 from = entry == null ? start : entry;
         self.method_5814(from.field_1352 + normal.field_1352 * 0.05, from.field_1351 + normal.field_1351 * 0.05, from.field_1350 + normal.field_1350 * 0.05);
         apoli$launch(self, reflected);
         this.apoli$missFired = false;
         config.hooks().bientityActionOnBounce().ifPresent(a -> a.run(BiEntityCtx.of(owner, self, level)));
         return true;
      } else {
         return false;
      }
   }

   @Unique
   @Nullable
   private static class_243 apoli$faceNormal(class_238 box, class_243 point) {
      if (Math.abs(point.field_1352 - box.field_1323) < 1.0E-4) {
         return APOLI$WEST;
      } else if (Math.abs(point.field_1352 - box.field_1320) < 1.0E-4) {
         return APOLI$EAST;
      } else if (Math.abs(point.field_1350 - box.field_1321) < 1.0E-4) {
         return APOLI$NORTH;
      } else if (Math.abs(point.field_1350 - box.field_1324) < 1.0E-4) {
         return APOLI$SOUTH;
      } else if (Math.abs(point.field_1351 - box.field_1322) < 1.0E-4) {
         return APOLI$DOWN;
      } else {
         return Math.abs(point.field_1351 - box.field_1325) < 1.0E-4 ? APOLI$UP : null;
      }
   }

   @Unique
   private static void apoli$launch(class_1676 self, class_243 velocity) {
      self.method_18799(velocity);
      double horizontal = velocity.method_37267();
      self.method_36456((float)(class_3532.method_15349(velocity.field_1352, velocity.field_1350) * (180.0 / Math.PI)));
      self.method_36457((float)(class_3532.method_15349(velocity.field_1351, horizontal) * (180.0 / Math.PI)));
      self.field_5982 = self.method_36454();
      self.field_6004 = self.method_36455();
      self.field_6007 = true;
   }

   @Unique
   private void apoli$runOwnerAction(class_1676 self, Optional<BiEntityAction> action) {
      if (!action.isEmpty()) {
         boolean attributed = PowerCause.push(this.apoli$causeHolder, this.apoli$causePower);

         try {
            ((BiEntityAction)action.get()).run(BiEntityCtx.of(self.method_24921(), self, self.method_37908()));
         } finally {
            if (attributed) {
               PowerCause.pop();
            }
         }
      }
   }

   @Unique
   private static void apoli$standOff(class_1676 self, class_3965 blockHit) {
      class_2350 face = blockHit.method_17780();
      class_243 landing = blockHit.method_17784();
      double clearance = Math.max(self.method_17681(), self.method_17682()) * 0.5 + 0.01;
      self.method_5814(
         landing.field_1352 + face.method_10148() * clearance,
         landing.field_1351 + face.method_10164() * clearance,
         landing.field_1350 + face.method_10165() * clearance
      );
   }

   @Unique
   private boolean apoli$tryBounce(class_1676 self, class_1937 level, Config config, class_239 result) {
      Reflect reflect = (Reflect)config.reflective().orElse(null);
      if (reflect == null) {
         return false;
      }

      if (result instanceof class_3965 blockHit) {
         class_1297 owner = self.method_24921();
         int limit = reflect.maxBounces().evalInt(owner);
         if (limit >= 0 && this.apoli$bounces >= limit) {
            return false;
         }

         Hooks hooks = config.hooks();
         if (hooks.blockActionOnHit().isPresent()) {
            class_2338 pos = blockHit.method_17777();
            BlockCtx blockCtx = new BlockCtx(
               pos.method_10062(), level.method_8320(pos), level, self.method_24921() != null ? self.method_24921() : self, blockHit.method_17784()
            );
            if (hooks.blockCondition().isEmpty() || ((BlockCondition)hooks.blockCondition().get()).test(blockCtx)) {
               ((BlockAction)hooks.blockActionOnHit().get()).run(blockCtx);
            }
         }

         class_2350 face = blockHit.method_17780();
         class_243 velocity = self.method_18798();
         double dot = velocity.field_1352 * face.method_10148() + velocity.field_1351 * face.method_10164() + velocity.field_1350 * face.method_10165();
         class_243 reflected = new class_243(
               velocity.field_1352 - 2.0 * dot * face.method_10148(),
               velocity.field_1351 - 2.0 * dot * face.method_10164(),
               velocity.field_1350 - 2.0 * dot * face.method_10165()
            )
            .method_1021(reflect.speed().eval(owner));
         if (reflected.method_1027() < 1.0E-6) {
            return false;
         }

         this.apoli$bounces++;
         apoli$standOff(self, blockHit);
         apoli$launch(self, reflected);
         this.apoli$newLeg();
         this.apoli$missFired = false;
         hooks.bientityActionOnBounce().ifPresent(a -> a.run(BiEntityCtx.of(self.method_24921(), self, level)));
         return true;
      } else {
         return false;
      }
   }

   @Unique
   private void apoli$runMissHooks(class_1676 self, class_1937 level, Hooks hooks, boolean blockActionCancelsMiss, class_239 result) {
      boolean blockActionRan = false;
      if (hooks.blockActionOnHit().isPresent() && result instanceof class_3965 blockHit) {
         class_2338 pos = blockHit.method_17777();
         BlockCtx blockCtx = new BlockCtx(
            pos.method_10062(), level.method_8320(pos), level, self.method_24921() != null ? self.method_24921() : self, blockHit.method_17784()
         );
         if (hooks.blockCondition().isEmpty() || ((BlockCondition)hooks.blockCondition().get()).test(blockCtx)) {
            ((BlockAction)hooks.blockActionOnHit().get()).run(blockCtx);
            blockActionRan = true;
         }
      }

      if (!this.apoli$missFired && (!blockActionRan || !blockActionCancelsMiss)) {
         this.apoli$missFired = true;
         hooks.bientityActionOnMiss().ifPresent(a -> a.run(BiEntityCtx.of(self.method_24921(), self, level)));
      }
   }
}
