package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionLiveness;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.action.DelayedActionQueue;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.data.Nbt;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.TextureRef;
import dev.alphasucks.aea.port.data.TextureRef.Kind;
import dev.alphasucks.aea.port.entity.ApoliEntities;
import dev.alphasucks.aea.port.entity.CustomProjectileEntity;
import dev.alphasucks.aea.port.entity.ProjectileHitActions;
import dev.alphasucks.aea.port.entity.ProjectileTickManager;
import dev.alphasucks.aea.port.network.payload.PowerActivatedS2C;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.sound.Sounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1676;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3419;
import net.minecraft.class_7923;

public final class FireProjectilePower extends PowerType<FireProjectilePower.Config> {
   private static final MapCodec<Optional<FireProjectilePower.Reflect>> REFLECTIVE = RecordCodecBuilder.<FireProjectilePower.ReflectFields>mapCodec(
         i -> i.group(
               LoggedOptionalField.of("reflective", Codec.either(Codec.BOOL, FireProjectilePower.Reflect.CODEC))
                  .forGetter(FireProjectilePower.ReflectFields::reflective),
               LoggedOptionalField.of("max_bounces", Expression.INT_OR_EXPR).forGetter(FireProjectilePower.ReflectFields::maxBounces),
               LoggedOptionalField.of("bounce_speed", Expression.FLOAT_OR_EXPR).forGetter(FireProjectilePower.ReflectFields::bounceSpeed)
            )
            .apply(i, FireProjectilePower.ReflectFields::new)
      )
      .xmap(
         FireProjectilePower::reflectFrom,
         reflect -> new FireProjectilePower.ReflectFields(reflect.map(r -> Either.right(r)), Optional.empty(), Optional.empty())
      );
   private static final Expression NO_OFFSET = Expression.constant(0.0);
   private static final Expression NO_LIFETIME = Expression.constant(0.0);
   private static final Expression NO_PIERCE = Expression.constant(0.0);
   private static final MapCodec<FireProjectilePower.Params> PARAMS_BODY = RecordCodecBuilder.mapCodec(
      i -> i.group(
            IdCodecs.ID.optionalFieldOf("entity_type").forGetter(FireProjectilePower.Params::entityType),
            TextureRef.ID_CODEC.optionalFieldOf("texture_location").forGetter(FireProjectilePower.Params::textureLocation),
            Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(1.0)).forGetter(FireProjectilePower.Params::cooldown),
            HudRender.CODEC.optionalFieldOf("hud_render").forGetter(FireProjectilePower.Params::hudRender),
            Expression.INT_OR_EXPR.optionalFieldOf("count", Expression.constant(1.0)).forGetter(FireProjectilePower.Params::count),
            Codec.INT.optionalFieldOf("interval", 0).forGetter(FireProjectilePower.Params::interval),
            Codec.INT.optionalFieldOf("start_delay", 0).forGetter(FireProjectilePower.Params::startDelay),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("speed", Expression.constant(1.5)).forGetter(FireProjectilePower.Params::speed),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("divergence", Expression.constant(1.0)).forGetter(FireProjectilePower.Params::divergence),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("max_distance", Expression.constant(0.0)).forGetter(FireProjectilePower.Params::maxDistance),
            IdCodecs.ID.optionalFieldOf("sound").forGetter(FireProjectilePower.Params::sound),
            Nbt.CODEC.optionalFieldOf("tag").forGetter(FireProjectilePower.Params::tag),
            Codec.BOOL.optionalFieldOf("allow_conditional_cancelling", false).forGetter(FireProjectilePower.Params::allowConditionalCancelling),
            Codec.BOOL.optionalFieldOf("block_action_cancels_miss_action", false).forGetter(FireProjectilePower.Params::blockActionCancelsMissAction),
            Key.CODEC.optionalFieldOf("key").forGetter(FireProjectilePower.Params::key)
         )
         .apply(
            i,
            (entityType, textureLocation, cooldown, hudRender, count, interval, startDelay, speed, divergence, maxDistance, sound, tag, allowConditionalCancelling, blockActionCancelsMissAction, key) -> new FireProjectilePower.Params(
               entityType,
               textureLocation,
               cooldown,
               hudRender,
               count,
               interval,
               startDelay,
               speed,
               divergence,
               maxDistance,
               sound,
               tag,
               allowConditionalCancelling,
               blockActionCancelsMissAction,
               key,
               NO_OFFSET,
               NO_OFFSET,
               NO_OFFSET,
               Space.WORLD
            )
         )
   );
   private static final MapCodec<FireProjectilePower.Spawn> PARAMS_SPAWN = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_x", NO_OFFSET).forGetter(FireProjectilePower.Spawn::offsetX),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_y", NO_OFFSET).forGetter(FireProjectilePower.Spawn::offsetY),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_z", NO_OFFSET).forGetter(FireProjectilePower.Spawn::offsetZ),
            Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(FireProjectilePower.Spawn::space)
         )
         .apply(i, FireProjectilePower.Spawn::new)
   );
   private static final MapCodec<FireProjectilePower.Params> PARAMS = Codec.mapPair(PARAMS_BODY, PARAMS_SPAWN)
      .xmap(
         pair -> ((FireProjectilePower.Params)pair.getFirst()).withSpawn((FireProjectilePower.Spawn)pair.getSecond()),
         params -> Pair.of(params, new FireProjectilePower.Spawn(params.offsetX(), params.offsetY(), params.offsetZ(), params.space()))
      );
   private static final MapCodec<FireProjectilePower.Hooks> HOOKS = RecordCodecBuilder.mapCodec(
      i -> i.group(
            LoggedOptionalField.of("entity_action_before_firing", EntityAction.CODEC).forGetter(FireProjectilePower.Hooks::entityActionBeforeFiring),
            LoggedOptionalField.of("bientity_action_after_firing", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::bientityActionAfterFiring),
            LoggedOptionalField.of("block_action_on_hit", BlockAction.CODEC).forGetter(FireProjectilePower.Hooks::blockActionOnHit),
            LoggedOptionalField.of("bientity_action_on_miss", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::bientityActionOnMiss),
            LoggedOptionalField.of("bientity_action_on_hit", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::bientityActionOnHit),
            LoggedOptionalField.of("owner_target_bientity_action_on_hit", BiEntityAction.CODEC)
               .forGetter(FireProjectilePower.Hooks::ownerTargetBientityActionOnHit),
            LoggedOptionalField.of("tick_bientity_action", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::tickBientityAction),
            LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(FireProjectilePower.Hooks::blockCondition),
            LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(FireProjectilePower.Hooks::bientityCondition),
            LoggedOptionalField.strict("owner_bientity_condition", BiEntityCondition.CODEC).forGetter(FireProjectilePower.Hooks::ownerBientityCondition),
            LoggedOptionalField.of("projectile_action", EntityAction.CODEC).forGetter(FireProjectilePower.Hooks::projectileAction),
            LoggedOptionalField.of("shooter_action", EntityAction.CODEC).forGetter(FireProjectilePower.Hooks::shooterAction),
            LoggedOptionalField.of("bientity_action_on_bounce", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::bientityActionOnBounce),
            LoggedOptionalField.of("bientity_action_on_expire", BiEntityAction.CODEC).forGetter(FireProjectilePower.Hooks::bientityActionOnExpire)
         )
         .apply(i, FireProjectilePower.Hooks::new)
   );
   public static final MapCodec<FireProjectilePower.Config> CONFIG_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            PARAMS.forGetter(FireProjectilePower.Config::params),
            HOOKS.forGetter(FireProjectilePower.Config::hooks),
            LoggedOptionalField.of("return", FireProjectilePower.Return.CODEC).forGetter(FireProjectilePower.Config::returning),
            LoggedOptionalField.of("homing", FireProjectilePower.Homing.CODEC).forGetter(FireProjectilePower.Config::homing),
            REFLECTIVE.forGetter(FireProjectilePower.Config::reflective),
            Expression.INT_OR_EXPR.optionalFieldOf("lifetime", NO_LIFETIME).forGetter(FireProjectilePower.Config::lifetime),
            Expression.INT_OR_EXPR.optionalFieldOf("pierce", NO_PIERCE).forGetter(FireProjectilePower.Config::pierce)
         )
         .apply(i, FireProjectilePower.Config::new)
   );
   private final Map<FireProjectilePower.StateKey, FireProjectilePower.FireState> states = new HashMap();

   private static Optional<FireProjectilePower.Reflect> reflectFrom(FireProjectilePower.ReflectFields fields) {
      return fields.reflective().isEmpty()
         ? Optional.empty()
         : ((Either<Boolean, FireProjectilePower.Reflect>)fields.reflective().get())
            .<Optional<FireProjectilePower.Reflect>>map(
               enabled -> enabled
                  ? Optional.of(
                     new FireProjectilePower.Reflect(
                        (Expression)fields.maxBounces().orElse(FireProjectilePower.Reflect.DEFAULT_MAX_BOUNCES),
                        (Expression)fields.bounceSpeed().orElse(FireProjectilePower.Reflect.DEFAULT_SPEED),
                        false
                     )
                  )
                  : Optional.empty(),
               Optional::of
            );
   }

   @Override
   public MapCodec<FireProjectilePower.Config> configCodec() {
      return CONFIG_CODEC;
   }

   public static void fireBurst(class_1297 owner, class_3218 level, FireProjectilePower.Config cfg) {
      playSound(owner, level, cfg.params());
      int count = Math.max(1, cfg.params().count().evalInt(owner));

      for (int n = 0; n < count; n++) {
         fireOne(owner, level, cfg);
      }
   }

   public static void fireVolley(class_1297 owner, class_3218 level, FireProjectilePower.Config cfg) {
      FireProjectilePower.Params p = cfg.params();
      int startDelay = Math.max(0, p.startDelay());
      int interval = Math.max(0, p.interval());
      if (startDelay <= 0 && interval <= 0) {
         fireBurst(owner, level, cfg);
      } else {
         int count = Math.max(1, p.count().evalInt(owner));
         int shots = interval <= 0 ? 1 : count;

         for (int n = 0; n < shots; n++) {
            int delay = startDelay + n * interval;
            int remaining = interval <= 0 ? count : 1;
            if (delay <= 0) {
               volleyStep(owner, level, cfg, remaining);
            } else {
               DelayedActionQueue.schedule(delay, () -> ActionLiveness.alive(owner, level), () -> volleyStep(owner, level, cfg, remaining));
            }
         }
      }
   }

   private static void volleyStep(class_1297 owner, class_3218 level, FireProjectilePower.Config cfg, int shots) {
      playSound(owner, level, cfg.params());

      for (int n = 0; n < shots; n++) {
         fireOne(owner, level, cfg);
      }
   }

   public boolean tryActivate(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder) {
      class_1309 owner = holder.owner();
      if (owner == null) {
         return false;
      } else if (!(owner.method_37908() instanceof class_3218 level)) {
         return false;
      } else {
         FireProjectilePower.StateKey var9 = new FireProjectilePower.StateKey(owner.method_5667(), powerId);
         FireProjectilePower.FireState st = (FireProjectilePower.FireState)this.states.computeIfAbsent(var9, x -> new FireProjectilePower.FireState());
         if (st.cooldown <= 0 && !st.firing) {
            FireProjectilePower.Params p = cfg.params();
            st.cooldown = Math.max(1, PowerResources.cooldownTicks(p.cooldown(), holder));
            if (p.startDelay() <= 0 && p.interval() <= 0) {
               fireBurst(owner, level, cfg);
            } else {
               st.firing = true;
               st.shotProjectiles = 0;
               st.ticksSinceUse = 0;
               st.finishedStartDelay = p.startDelay() <= 0;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public HudRender hudRender(FireProjectilePower.Config cfg) {
      return (HudRender)cfg.params().hudRender().orElse(null);
   }

   public OptionalInt readResource(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.of(PowerResources.clientCooldown(holder, powerId));
      }

      FireProjectilePower.FireState st = (FireProjectilePower.FireState)this.states.get(new FireProjectilePower.StateKey(owner.method_5667(), powerId));
      return OptionalInt.of(st == null ? 0 : Math.max(0, st.cooldown));
   }

   public OptionalInt writeResource(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder, int value) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908().method_8608()) {
         return OptionalInt.empty();
      }

      int clamped = Math.max(0, Math.min(value, Math.max(PowerResources.cooldownTicks(cfg.params().cooldown(), holder), 0)));
      ((FireProjectilePower.FireState)this.states
            .computeIfAbsent(new FireProjectilePower.StateKey(owner.method_5667(), powerId), x -> new FireProjectilePower.FireState()))
         .cooldown = clamped;
      if (owner instanceof class_3222 player) {
         ApoliNetwork.sendActivated(player, new PowerActivatedS2C(powerId, clamped));
      }

      return OptionalInt.of(clamped);
   }

   public OptionalInt resourceBound(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.params().cooldown(), holder), 0) : 0);
   }

   public void tickStored(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder) {
      this.tick(powerId, cfg, holder);
   }

   public void tick(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder) {
      FireProjectilePower.StateKey k = new FireProjectilePower.StateKey(holder.rawOwner().method_5667(), powerId);
      FireProjectilePower.FireState st = (FireProjectilePower.FireState)this.states.get(k);
      if (st != null) {
         if (st.cooldown > 0) {
            st.cooldown--;
         }

         if (st.firing) {
            class_1309 owner = holder.owner();
            if (owner != null) {
               if (owner.method_37908() instanceof class_3218 level) {
                  FireProjectilePower.Params p = cfg.params();
                  if (p.allowConditionalCancelling() && !conditionMet(powerId, owner, level)) {
                     reset(st);
                  } else {
                     st.ticksSinceUse++;
                     if (!st.finishedStartDelay) {
                        if (p.startDelay() > 0 && st.ticksSinceUse % p.startDelay() != 0) {
                           return;
                        }

                        st.finishedStartDelay = true;
                     }

                     if (p.interval() <= 0) {
                        playSound(owner, level, p);

                        for (int total = Math.max(1, p.count().evalInt(owner)); st.shotProjectiles < total; st.shotProjectiles++) {
                           fireOne(owner, level, cfg);
                        }

                        reset(st);
                     } else if (st.ticksSinceUse % p.interval() == 0) {
                        playSound(owner, level, p);
                        fireOne(owner, level, cfg);
                        st.shotProjectiles++;
                        if (st.shotProjectiles >= Math.max(1, p.count().evalInt(owner))) {
                           reset(st);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void onRemoved(class_2960 powerId, FireProjectilePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.allPowers().contains(powerId)) {
         this.states.remove(new FireProjectilePower.StateKey(holder.rawOwner().method_5667(), powerId));
      }
   }

   private static boolean conditionMet(class_2960 powerId, class_1309 owner, class_3218 level) {
      Power power = ApoliPowers.get(powerId);
      return power != null && !power.condition().isEmpty() ? ((EntityCondition)power.condition().get()).test(new EntityCtx(owner, level)) : true;
   }

   private static void reset(FireProjectilePower.FireState st) {
      st.firing = false;
      st.shotProjectiles = 0;
      st.ticksSinceUse = 0;
      st.finishedStartDelay = false;
   }

   private static void playSound(class_1297 owner, class_3218 level, FireProjectilePower.Params p) {
      if (!p.sound().isEmpty()) {
         level.method_43128(
            null,
            owner.method_23317(),
            owner.method_23318(),
            owner.method_23321(),
            Sounds.event((class_2960)p.sound().get()),
            class_3419.field_15254,
            0.5F,
            0.4F / (level.field_9229.method_43057() * 0.4F + 0.8F)
         );
      }
   }

   private static void fireOne(class_1297 owner, class_3218 level, FireProjectilePower.Config cfg) {
      FireProjectilePower.Params p = cfg.params();
      cfg.hooks().entityActionBeforeFiring().ifPresent(a -> a.run(new EntityCtx(owner, level)));
      class_1297 projectile;
      if (p.textureLocation().isPresent()) {
         CustomProjectileEntity custom = new CustomProjectileEntity(ApoliEntities.customProjectile(), owner, level);
         class_2960 declared = (class_2960)p.textureLocation().get();
         Kind kind = TextureRef.kindOf(declared);
         if (kind != null && kind.isItem() && owner instanceof class_1309 holder) {
            custom.setItem(kind == Kind.HELD_ITEM ? holder.method_6047() : holder.method_6079());
         } else {
            custom.setTexture(declared);
         }

         cfg.homing().ifPresent(homing -> custom.startHoming(homing, owner));
         projectile = custom;
      } else {
         if (!p.entityType().isPresent()) {
            return;
         }

         class_1299<?> type = (class_1299<?>)class_7923.field_41177.method_17966((class_2960)p.entityType().get()).orElse(null);
         if (type == null) {
            return;
         }

         projectile = type.method_5883(level);
         if (projectile == null) {
            return;
         }
      }

      if (projectile instanceof ProjectileHitActions hooks) {
         hooks.apoli$setFireConfig(cfg);
         hooks.apoli$setMaxRange(p.maxDistance().eval(owner));
         hooks.apoli$setLifetime(cfg.lifetime().evalInt(owner));
         hooks.apoli$setFireCause(PowerCause.holder(), PowerCause.powerId());
      }

      float yaw = owner.method_36454();
      float pitch = owner.method_36455();
      class_243 spawn = new class_243(owner.method_23317(), owner.method_23320(), owner.method_23321())
         .method_1019(p.space().toGlobal(owner, new class_243(p.offsetX().eval(owner), p.offsetY().eval(owner), p.offsetZ().eval(owner))));
      projectile.method_5808(spawn.field_1352, spawn.field_1351, spawn.field_1350, yaw, pitch);
      float speed = (float)p.speed().eval(owner);
      float divergence = (float)p.divergence().eval(owner);
      if (projectile instanceof class_1676 proj) {
         proj.method_7432(owner);
         proj.method_24919(owner, pitch, yaw, 0.0F, speed, divergence);
      } else {
         float f = (float) (Math.PI / 180.0);
         double g = 0.0075;
         double dx = -Math.sin(yaw * f) * Math.cos(pitch * f);
         double dy = -Math.sin(pitch * f);
         double dz = Math.cos(yaw * f) * Math.cos(pitch * f);
         class_243 v = new class_243(dx, dy, dz)
            .method_1029()
            .method_1031(
               level.field_9229.method_43059() * g * divergence,
               level.field_9229.method_43059() * g * divergence,
               level.field_9229.method_43059() * g * divergence
            )
            .method_1021(speed);
         projectile.method_18799(v);
      }

      p.tag().ifPresent(nbt -> {
         class_2487 merged = projectile.method_5647(new class_2487());
         merged.method_10543(nbt.tag());
         projectile.method_5651(merged);
      });
      level.method_8649(projectile);
      cfg.hooks().projectileAction().ifPresent(a -> a.run(new EntityCtx(projectile, level)));
      if (projectile instanceof CustomProjectileEntity custom) {
         List<class_2960> modelPowers = CustomModelRenderPower.geometryPowerIds(projectile);
         if (!modelPowers.isEmpty()) {
            custom.setModelPowers(modelPowers);
         }
      }

      cfg.hooks().bientityActionAfterFiring().ifPresent(a -> a.run(BiEntityCtx.of(owner, projectile, level)));
      cfg.hooks().tickBientityAction().ifPresent(a -> ProjectileTickManager.track(projectile, owner, a));
      cfg.hooks().shooterAction().ifPresent(a -> a.run(new EntityCtx(owner, level)));
   }

   public record Config(
      FireProjectilePower.Params params,
      FireProjectilePower.Hooks hooks,
      Optional<FireProjectilePower.Return> returning,
      Optional<FireProjectilePower.Homing> homing,
      Optional<FireProjectilePower.Reflect> reflective,
      Expression lifetime,
      Expression pierce
   ) {
   }

   private static final class FireState {
      int cooldown;
      boolean firing;
      int shotProjectiles;
      int ticksSinceUse;
      boolean finishedStartDelay;
   }

   public record Homing(
      Expression delay, Expression duration, Expression range, Expression angle, Expression turnRate, Optional<BiEntityCondition> bientityCondition
   ) {
      public static final Codec<FireProjectilePower.Homing> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Expression.INT_OR_EXPR.optionalFieldOf("delay", Expression.constant(0.0)).forGetter(FireProjectilePower.Homing::delay),
               Expression.INT_OR_EXPR.optionalFieldOf("duration", Expression.constant(0.0)).forGetter(FireProjectilePower.Homing::duration),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("range", Expression.constant(8.0)).forGetter(FireProjectilePower.Homing::range),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("angle", Expression.constant(60.0)).forGetter(FireProjectilePower.Homing::angle),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("turn_rate", Expression.constant(8.0)).forGetter(FireProjectilePower.Homing::turnRate),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(FireProjectilePower.Homing::bientityCondition)
            )
            .apply(i, FireProjectilePower.Homing::new)
      );
   }

   public record Hooks(
      Optional<EntityAction> entityActionBeforeFiring,
      Optional<BiEntityAction> bientityActionAfterFiring,
      Optional<BlockAction> blockActionOnHit,
      Optional<BiEntityAction> bientityActionOnMiss,
      Optional<BiEntityAction> bientityActionOnHit,
      Optional<BiEntityAction> ownerTargetBientityActionOnHit,
      Optional<BiEntityAction> tickBientityAction,
      Optional<BlockCondition> blockCondition,
      Optional<BiEntityCondition> bientityCondition,
      Optional<BiEntityCondition> ownerBientityCondition,
      Optional<EntityAction> projectileAction,
      Optional<EntityAction> shooterAction,
      Optional<BiEntityAction> bientityActionOnBounce,
      Optional<BiEntityAction> bientityActionOnExpire
   ) {
   }

   public record Params(
      Optional<class_2960> entityType,
      Optional<class_2960> textureLocation,
      Expression cooldown,
      Optional<HudRender> hudRender,
      Expression count,
      int interval,
      int startDelay,
      Expression speed,
      Expression divergence,
      Expression maxDistance,
      Optional<class_2960> sound,
      Optional<Nbt> tag,
      boolean allowConditionalCancelling,
      boolean blockActionCancelsMissAction,
      Optional<Key> key,
      Expression offsetX,
      Expression offsetY,
      Expression offsetZ,
      Space space
   ) {
      FireProjectilePower.Params withSpawn(FireProjectilePower.Spawn spawn) {
         return new FireProjectilePower.Params(
            this.entityType,
            this.textureLocation,
            this.cooldown,
            this.hudRender,
            this.count,
            this.interval,
            this.startDelay,
            this.speed,
            this.divergence,
            this.maxDistance,
            this.sound,
            this.tag,
            this.allowConditionalCancelling,
            this.blockActionCancelsMissAction,
            this.key,
            spawn.offsetX(),
            spawn.offsetY(),
            spawn.offsetZ(),
            spawn.space()
         );
      }
   }

   public record Reflect(Expression maxBounces, Expression speed, boolean entities) {
      private static final Expression DEFAULT_MAX_BOUNCES = Expression.constant(4.0);
      private static final Expression DEFAULT_SPEED = Expression.constant(1.0);
      public static final Codec<FireProjectilePower.Reflect> CODEC = AliasingMapCodec.wrap(
            RecordCodecBuilder.<FireProjectilePower.Reflect>mapCodec(
               i -> i.group(
                     Expression.INT_OR_EXPR.optionalFieldOf("max_bounces", DEFAULT_MAX_BOUNCES).forGetter(FireProjectilePower.Reflect::maxBounces),
                     Expression.FLOAT_OR_EXPR.optionalFieldOf("speed", DEFAULT_SPEED).forGetter(FireProjectilePower.Reflect::speed),
                     Codec.BOOL.optionalFieldOf("entities", false).forGetter(FireProjectilePower.Reflect::entities)
                  )
                  .apply(i, FireProjectilePower.Reflect::new)
            ),
            Map.of("bounce_speed", "speed")
         )
         .codec();
   }

   private record ReflectFields(
      Optional<Either<Boolean, FireProjectilePower.Reflect>> reflective, Optional<Expression> maxBounces, Optional<Expression> bounceSpeed
   ) {
   }

   public record Return(
      boolean onHitEntity,
      boolean onHitBlock,
      int after,
      Expression speed,
      boolean hitWhileReturning,
      Optional<BiEntityAction> bientityActionOnReturn,
      Optional<BiEntityAction> bientityActionOnCatch,
      Optional<BiEntityCondition> condition
   ) {
      public static final Codec<FireProjectilePower.Return> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("on_hit_entity", true).forGetter(FireProjectilePower.Return::onHitEntity),
               Codec.BOOL.optionalFieldOf("on_hit_block", true).forGetter(FireProjectilePower.Return::onHitBlock),
               Codec.INT.optionalFieldOf("after", 0).forGetter(FireProjectilePower.Return::after),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("speed", Expression.constant(2.0)).forGetter(FireProjectilePower.Return::speed),
               Codec.BOOL.optionalFieldOf("hit_while_returning", false).forGetter(FireProjectilePower.Return::hitWhileReturning),
               LoggedOptionalField.of("bientity_action_on_return", BiEntityAction.CODEC).forGetter(FireProjectilePower.Return::bientityActionOnReturn),
               LoggedOptionalField.of("bientity_action_on_catch", BiEntityAction.CODEC).forGetter(FireProjectilePower.Return::bientityActionOnCatch),
               LoggedOptionalField.strict("condition", BiEntityCondition.CODEC).forGetter(FireProjectilePower.Return::condition)
            )
            .apply(i, FireProjectilePower.Return::new)
      );
   }

   private record Spawn(Expression offsetX, Expression offsetY, Expression offsetZ, Space space) {
   }

   private record StateKey(UUID entity, class_2960 power) {
   }
}
