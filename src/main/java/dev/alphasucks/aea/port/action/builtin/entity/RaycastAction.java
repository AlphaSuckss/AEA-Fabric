package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LazyCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.ExprVector;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.FluidHandling;
import dev.alphasucks.aea.port.data.ParticleEffect;
import dev.alphasucks.aea.port.data.ShapeType;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.data.expr.ExprContext;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.dev.DevParticles.Ray;
import dev.alphasucks.aea.port.util.InteractionRange;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3542;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class RaycastAction implements ActionType<EntityCtx, RaycastAction.Cfg> {
   private static final int SLOT_DISTANCE = ExprContext.slot("distance");
   private static final int SLOT_HIT_X = ExprContext.slot("hit_x");
   private static final int SLOT_HIT_Y = ExprContext.slot("hit_y");
   private static final int SLOT_HIT_Z = ExprContext.slot("hit_z");
   private static final int SLOT_COUNT = ExprContext.slot("count");
   private static final int SLOT_INDEX = ExprContext.slot("index");
   private static final int MAX_CHAIN_DEPTH = 32;
   private static final int MAX_PIERCED_BLOCKS = 128;
   private static final int MAX_MARKED_BLOCKS = 16;
   private static final MapCodec<RaycastAction.Params> PARAMS = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Expression.FLOAT_OR_EXPR.optionalFieldOf("distance").forGetter(RaycastAction.Params::distance),
            Codec.BOOL.optionalFieldOf("block", true).forGetter(RaycastAction.Params::block),
            Codec.BOOL.optionalFieldOf("entity", true).forGetter(RaycastAction.Params::entity),
            ShapeType.CODEC.optionalFieldOf("shape_type", ShapeType.VISUAL).forGetter(RaycastAction.Params::shapeType),
            FluidHandling.CODEC.optionalFieldOf("fluid_handling", FluidHandling.ANY).forGetter(RaycastAction.Params::fluidHandling),
            Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(RaycastAction.Params::space),
            Vector.CODEC.optionalFieldOf("direction").forGetter(RaycastAction.Params::direction),
            ParticleEffect.CODEC.optionalFieldOf("particle").forGetter(RaycastAction.Params::particle),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("spacing", Expression.constant(0.5)).forGetter(RaycastAction.Params::spacing),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("entity_distance").forGetter(RaycastAction.Params::entityDistance),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("block_distance").forGetter(RaycastAction.Params::blockDistance),
            ExprVector.SCALAR_OR_VECTOR.optionalFieldOf("radius").forGetter(RaycastAction.Params::radius),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("cone_angle").forGetter(RaycastAction.Params::coneAngle),
            RaycastAction.ChainDirection.CODEC
               .optionalFieldOf("chain_direction", RaycastAction.ChainDirection.FORWARD)
               .forGetter(RaycastAction.Params::chainDirection)
         )
         .apply(i, RaycastAction.Params::new)
   );
   private static final MapCodec<RaycastAction.Aim> AIM = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.BOOL.optionalFieldOf("pierce", false).forGetter(RaycastAction.Aim::pierce),
            Codec.BOOL.optionalFieldOf("pierce_blocks").forGetter(RaycastAction.Aim::pierceBlocks),
            Codec.BOOL.optionalFieldOf("pierce_entities").forGetter(RaycastAction.Aim::pierceEntities),
            Codec.BOOL.optionalFieldOf("aim_at_target", true).forGetter(RaycastAction.Aim::aimAtTarget),
            Codec.BOOL.optionalFieldOf("stop_at_target", true).forGetter(RaycastAction.Aim::stopAtTarget)
         )
         .apply(i, RaycastAction.Aim::new)
   );
   private static final MapCodec<RaycastAction.Hooks> HOOKS = RecordCodecBuilder.mapCodec(
      i -> i.group(
            LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(RaycastAction.Hooks::bientityCondition),
            LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(RaycastAction.Hooks::blockCondition),
            LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(RaycastAction.Hooks::bientityAction),
            LoggedOptionalField.of("block_action", BlockAction.CODEC).forGetter(RaycastAction.Hooks::blockAction),
            LoggedOptionalField.of("before_action", EntityAction.CODEC).forGetter(RaycastAction.Hooks::beforeAction),
            LoggedOptionalField.of("hit_action", EntityAction.CODEC).forGetter(RaycastAction.Hooks::hitAction),
            LoggedOptionalField.of("miss_action", EntityAction.CODEC).forGetter(RaycastAction.Hooks::missAction),
            Codec.STRING.optionalFieldOf("command_at_hit").forGetter(RaycastAction.Hooks::commandAtHit),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("command_hit_offset").forGetter(RaycastAction.Hooks::commandHitOffset),
            Codec.STRING.optionalFieldOf("command_along_ray").forGetter(RaycastAction.Hooks::commandAlongRay),
            Expression.FLOAT_OR_EXPR.optionalFieldOf("command_step", Expression.constant(1.0)).forGetter(RaycastAction.Hooks::commandStep),
            Codec.BOOL.optionalFieldOf("command_along_ray_only_on_hit", false).forGetter(RaycastAction.Hooks::commandAlongRayOnlyOnHit)
         )
         .apply(i, RaycastAction.Hooks::new)
   );
   private static final Codec<RaycastAction.Cfg> CHAIN_CODEC = new LazyCodec<>(() -> buildCodec(chainCodec()).codec());
   public static final MapCodec<RaycastAction.Cfg> CONFIG_CODEC = buildCodec(CHAIN_CODEC);

   private static MapCodec<RaycastAction.Cfg> buildCodec(Codec<RaycastAction.Cfg> chainCodec) {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               PARAMS.forGetter(RaycastAction.Cfg::params),
               AIM.forGetter(RaycastAction.Cfg::aim),
               HOOKS.forGetter(RaycastAction.Cfg::hooks),
               chainCodec.optionalFieldOf("chain").forGetter(RaycastAction.Cfg::chain)
            )
            .apply(i, RaycastAction.Cfg::new)
      );
   }

   private static Codec<RaycastAction.Cfg> chainCodec() {
      return CHAIN_CODEC;
   }

   @Override
   public MapCodec<RaycastAction.Cfg> codec() {
      return CONFIG_CODEC;
   }

   public void run(RaycastAction.Cfg cfg, EntityCtx ctx) {
      cast(cfg, ctx, null, ctx.entity().method_33571(), null, null, 0);
   }

   public static void runTowards(RaycastAction.Cfg cfg, EntityCtx ctx, class_1297 target) {
      cast(cfg, ctx, target, ctx.entity().method_33571(), null, null, 0);
   }

   @Nullable
   public static class_243 entityHit(
      class_1297 candidate, class_243 origin, class_243 dir, class_243 end, double range, double rx, double ry, double rz, boolean cone, double coneCos
   ) {
      if (cone) {
         class_243 center = candidate.method_5829().method_1005();
         class_243 toEntity = center.method_1020(origin);
         double dist = toEntity.method_1033();
         if (dist > range) {
            return null;
         } else {
            return dist > 1.0E-4 && toEntity.method_1021(1.0 / dist).method_1026(dir) < coneCos ? null : center;
         }
      } else {
         class_238 targetBox = Math.max(rx, Math.max(ry, rz)) > 0.0 ? candidate.method_5829().method_1009(rx, ry, rz) : candidate.method_5829();
         return targetBox.method_1006(origin) ? origin : (class_243)targetBox.method_992(origin, end).orElse(null);
      }
   }

   private static float blockDistance(class_1297 source, RaycastAction.Params params) {
      if (params.blockDistance.isPresent()) {
         return (float)((Expression)params.blockDistance.get()).eval(source);
      } else {
         return params.distance.isPresent() ? (float)((Expression)params.distance.get()).eval(source) : (float)InteractionRange.block(source);
      }
   }

   private static float entityDistance(class_1297 source, RaycastAction.Params params) {
      if (params.entityDistance.isPresent()) {
         return (float)((Expression)params.entityDistance.get()).eval(source);
      } else {
         return params.distance.isPresent() ? (float)((Expression)params.distance.get()).eval(source) : (float)InteractionRange.entity(source);
      }
   }

   private static void cast(
      RaycastAction.Cfg cfg,
      EntityCtx ctx,
      @Nullable class_1297 aimTarget,
      class_243 origin,
      @Nullable class_243 incomingDir,
      @Nullable class_243 incomingNormal,
      int depth
   ) {
      if (depth <= 32) {
         class_1297 source = ctx.entity();
         class_1937 level = ctx.level();
         cfg.hooks.beforeAction.ifPresent(a -> a.run(ctx));
         boolean aiming = aimTarget != null && cfg.aim.aimAtTarget && incomingDir == null && cfg.params.direction.isEmpty();
         class_243 dir = aiming ? aimTarget.method_5829().method_1005().method_1020(origin) : resolveDirection(cfg, source, incomingDir, incomingNormal);
         if (dir.method_1027() < 1.0E-6) {
            dir = source.method_5828(1.0F);
         }

         double gap = aiming ? dir.method_1033() : 0.0;
         dir = dir.method_1029();
         float baseBlockDist = blockDistance(source, cfg.params);
         float baseEntityDist = entityDistance(source, cfg.params);
         boolean clampToTarget = aiming && cfg.aim.stopAtTarget;
         float blockDist = clampToTarget ? (float)gap : baseBlockDist;
         float entityDist = clampToTarget ? (float)gap : baseEntityDist;
         boolean pierceBlocks = cfg.aim.piercesBlocks();
         boolean pierceEntities = cfg.aim.piercesEntities();
         boolean drawing = DevMode.any() && level instanceof class_3218;
         List<class_2338> piercedBlocks = drawing && pierceBlocks ? new ArrayList(4) : null;
         List<class_1297> hitEntities = drawing ? new ArrayList(4) : null;
         class_3965 blockHit = null;
         if (cfg.params.block) {
            class_243 to = origin.method_1019(dir.method_1021(blockDist));
            if (pierceBlocks) {
               class_243 from = origin;

               for (int guard = 0; guard < 128; guard++) {
                  class_3965 hit = level.method_17742(new class_3959(from, to, cfg.params.shapeType.vanilla(), cfg.params.fluidHandling.vanilla(), source));
                  if (hit.method_17783() == class_240.field_1333) {
                     break;
                  }

                  if (blockHit == null) {
                     blockHit = hit;
                  }

                  class_2338 pos = hit.method_17777();
                  class_2680 state = level.method_8320(pos);
                  BlockCtx blockCtx = new BlockCtx(pos, state, level, source, hit.method_17784());
                  if (cfg.hooks.blockCondition.isEmpty() || ((BlockCondition)cfg.hooks.blockCondition.get()).test(blockCtx)) {
                     if (cfg.hooks.blockAction.isPresent()) {
                        try (RaycastAction.Scope scope = new RaycastAction.Scope(origin, hit.method_17784(), 1, guard)) {
                           ((BlockAction)cfg.hooks.blockAction.get()).run(blockCtx);
                        }
                     }

                     if (piercedBlocks != null && piercedBlocks.size() < 16) {
                        piercedBlocks.add(pos);
                     }
                  }

                  double exitT = cellExitT(origin, dir, pos, blockDist);
                  if (exitT >= blockDist) {
                     break;
                  }

                  from = origin.method_1019(dir.method_1021(exitT + 0.001));
               }
            } else {
               blockHit = level.method_17742(new class_3959(origin, to, cfg.params.shapeType.vanilla(), cfg.params.fluidHandling.vanilla(), source));
               if (blockHit.method_17783() == class_240.field_1333) {
                  blockHit = null;
               }
            }
         }

         double blockHitDistSq = blockHit != null ? origin.method_1025(blockHit.method_17784()) : Double.POSITIVE_INFINITY;
         double rx = 0.0;
         double ry = 0.0;
         double rz = 0.0;
         if (cfg.params.radius.isPresent()) {
            Vector radius = ((ExprVector)cfg.params.radius.get()).resolve(source);
            rx = radius.x();
            ry = radius.y();
            rz = radius.z();
         }

         double maxRadius = Math.max(rx, Math.max(ry, rz));
         boolean anyEntityHit = false;
         class_243 nearestEntityHit = null;
         double nearestEntityHitDistSq = Double.POSITIVE_INFINITY;
         boolean coneMode = cfg.params.coneAngle.isPresent();
         double coneAngle = coneMode ? ((Expression)cfg.params.coneAngle.get()).eval(source) : -1.0;
         double coneCos = coneMode ? Math.cos(Math.toRadians(coneAngle)) : -1.0;
         if (cfg.params.entity) {
            class_243 endE = origin.method_1019(dir.method_1021(entityDist));
            class_238 box = coneMode
               ? class_238.method_30048(origin, entityDist * 2.0, entityDist * 2.0, entityDist * 2.0)
               : new class_238(origin, endE).method_1014(1.0 + maxRadius);
            List<class_1297> candidates = level.method_8333(source, box, e -> e != source && e.method_5863());
            List<RaycastAction.EntityHit> hits = new ArrayList();

            for (class_1297 cand : candidates) {
               class_243 hitPos = entityHit(cand, origin, dir, endE, entityDist, rx, ry, rz, coneMode, coneCos);
               if (hitPos != null) {
                  double dSq = origin.method_1025(hitPos);
                  if (pierceBlocks || !(dSq > blockHitDistSq)) {
                     hits.add(new RaycastAction.EntityHit(cand, hitPos, dSq));
                  }
               }
            }

            hits.sort(Comparator.comparingDouble(RaycastAction.EntityHit::distSq));
            int hitIndex = 0;

            for (RaycastAction.EntityHit hit : hits) {
               if (!cfg.hooks.bientityCondition.isPresent()
                  || ((BiEntityCondition)cfg.hooks.bientityCondition.get()).test(new BiEntityCtx(source, hit.target(), level))) {
                  if (hitEntities != null) {
                     hitEntities.add(hit.target());
                  }

                  if (cfg.hooks.bientityAction.isPresent()) {
                     try (RaycastAction.Scope scope = new RaycastAction.Scope(origin, hit.pos(), hits.size(), hitIndex)) {
                        ((BiEntityAction)cfg.hooks.bientityAction.get()).run(new BiEntityCtx(source, hit.target(), level));
                     }
                  }

                  hitIndex++;
                  if (!anyEntityHit) {
                     anyEntityHit = true;
                     nearestEntityHit = hit.pos();
                     nearestEntityHitDistSq = hit.distSq();
                  }

                  if (!pierceEntities) {
                     break;
                  }
               }
            }
         }

         boolean entityStops = anyEntityHit && !pierceEntities && nearestEntityHitDistSq <= blockHitDistSq;
         class_243 rayEnd;
         if (entityStops) {
            rayEnd = nearestEntityHit;
         } else if (blockHit != null && !pierceBlocks) {
            rayEnd = blockHit.method_17784();
         } else {
            rayEnd = origin.method_1019(dir.method_1021(blockDist));
         }

         if (drawing) {
            outline(
               (class_3218)level, cfg, origin, dir, rayEnd, blockDist, entityDist, rx, ry, rz, coneAngle, blockHit, blockHitDistSq, piercedBlocks, hitEntities
            );
         }

         boolean anyHit = anyEntityHit || blockHit != null;
         if (blockHit != null && !entityStops && !pierceBlocks) {
            class_2338 pos = blockHit.method_17777();
            class_2680 state = level.method_8320(pos);
            BlockCtx blockCtx = new BlockCtx(pos, state, level, source, blockHit.method_17784());
            if ((cfg.hooks.blockCondition.isEmpty() || ((BlockCondition)cfg.hooks.blockCondition.get()).test(blockCtx)) && cfg.hooks.blockAction.isPresent()) {
               try (RaycastAction.Scope scope = new RaycastAction.Scope(origin, blockHit.method_17784(), 1, 0)) {
                  ((BlockAction)cfg.hooks.blockAction.get()).run(blockCtx);
               }
            }
         }

         if (cfg.hooks.hitAction.isPresent() || cfg.hooks.missAction.isPresent()) {
            try (RaycastAction.Scope scope = new RaycastAction.Scope(origin, rayEnd, anyEntityHit ? 1 : 0, 0)) {
               if (anyHit) {
                  cfg.hooks.hitAction.ifPresent(a -> a.run(ctx));
               } else {
                  cfg.hooks.missAction.ifPresent(a -> a.run(ctx));
               }
            }
         }

         if (level instanceof class_3218 serverLevel && cfg.params.particle.isPresent()) {
            class_2394 opts = ((ParticleEffect)cfg.params.particle.get()).resolve(level, ctx.raw());
            if (opts != null) {
               double total = origin.method_1022(rayEnd);
               double step = Math.max(0.1, cfg.params.spacing.eval(source));

               for (double t = 0.0; t < total; t += step) {
                  class_243 p = origin.method_1019(dir.method_1021(t));
                  serverLevel.method_14199(opts, p.field_1352, p.field_1351, p.field_1350, 1, 0.0, 0.0, 0.0, 0.0);
               }
            }
         }

         MinecraftServer server = level.method_8503();
         if (server != null) {
            class_2168 cmdSrc = source.method_5671().method_9206(4).method_9217();
            if (cfg.hooks.commandAtHit.isPresent() && anyHit) {
               boolean entityFirst = anyEntityHit && nearestEntityHitDistSq <= blockHitDistSq;
               class_243 hitPos = !entityFirst && blockHit != null ? blockHit.method_17784() : nearestEntityHit;
               float off = cfg.hooks.commandHitOffset.isPresent() ? (float)((Expression)cfg.hooks.commandHitOffset.get()).eval(source) : 0.0F;
               if (off != 0.0F) {
                  class_243 normal = !entityFirst && blockHit != null ? class_243.method_24954(blockHit.method_17780().method_10163()) : dir.method_1021(-1.0);
                  hitPos = hitPos.method_1019(normal.method_1021(off));
               }

               class_2168 atPos = cmdSrc.method_9208(hitPos);
               server.method_3734().method_44252(atPos, (String)cfg.hooks.commandAtHit.get());
            }

            if (cfg.hooks.commandAlongRay.isPresent() && (!cfg.hooks.commandAlongRayOnlyOnHit || anyHit)) {
               double total = origin.method_1022(rayEnd);
               double step = Math.max(0.1, cfg.hooks.commandStep.eval(source));

               for (double t = 0.0; t < total; t += step) {
                  class_243 p = origin.method_1019(dir.method_1021(t));
                  server.method_3734().method_44252(cmdSrc.method_9208(p), (String)cfg.hooks.commandAlongRay.get());
               }
            }
         }

         if (cfg.chain.isPresent()) {
            class_243 hitNormal = blockHit != null && !entityStops && !pierceBlocks ? class_243.method_24954(blockHit.method_17780().method_10163()) : null;
            class_243 chainOrigin = hitNormal != null ? rayEnd.method_1019(hitNormal.method_1021(0.01)) : rayEnd;
            cast((RaycastAction.Cfg)cfg.chain.get(), ctx, null, chainOrigin, dir, hitNormal, depth + 1);
         }
      }
   }

   private static void outline(
      class_3218 level,
      RaycastAction.Cfg cfg,
      class_243 origin,
      class_243 dir,
      class_243 rayEnd,
      float blockDist,
      float entityDist,
      double rx,
      double ry,
      double rz,
      double coneAngle,
      @Nullable class_3965 blockHit,
      double blockHitDistSq,
      @Nullable List<class_2338> piercedBlocks,
      List<class_1297> hitEntities
   ) {
      Ray kind = Ray.ACTION;
      double reach = Math.max(cfg.params.block ? blockDist : 0.0, cfg.params.entity ? entityDist : 0.0);
      DevParticles.ray(level, kind, origin, dir, origin.method_1022(rayEnd), reach);
      if (cfg.params.entity) {
         double length = cfg.aim.piercesBlocks() ? entityDist : Math.min(entityDist, Math.sqrt(blockHitDistSq));
         DevParticles.detection(level, kind, origin, dir, length, rx, ry, rz, coneAngle);
      }

      if (piercedBlocks != null) {
         for (int i = 0; i < piercedBlocks.size(); i++) {
            DevParticles.mark(level, kind, (class_2338)piercedBlocks.get(i), true);
         }
      } else if (blockHit != null) {
         DevParticles.mark(level, kind, blockHit.method_17777(), true);
      }

      for (int i = 0; i < hitEntities.size(); i++) {
         DevParticles.mark(level, kind, (class_1297)hitEntities.get(i), true);
      }
   }

   private static double cellExitT(class_243 origin, class_243 dir, class_2338 pos, double maxT) {
      double t = maxT;
      t = Math.min(t, axisExitT(origin.field_1352, dir.field_1352, pos.method_10263()));
      t = Math.min(t, axisExitT(origin.field_1351, dir.field_1351, pos.method_10264()));
      return Math.min(t, axisExitT(origin.field_1350, dir.field_1350, pos.method_10260()));
   }

   private static double axisExitT(double origin, double dir, int cell) {
      if (dir > 1.0E-9) {
         return (cell + 1 - origin) / dir;
      } else {
         return dir < -1.0E-9 ? (cell - origin) / dir : Double.POSITIVE_INFINITY;
      }
   }

   private static class_243 resolveDirection(RaycastAction.Cfg cfg, class_1297 source, @Nullable class_243 incomingDir, @Nullable class_243 incomingNormal) {
      if (incomingDir == null) {
         return customOrView(cfg, source, source.method_5828(1.0F));
      }

      return switch (cfg.params.chainDirection) {
         case FORWARD -> incomingDir;
         case REFLECT -> incomingNormal != null ? reflect(incomingDir, incomingNormal) : incomingDir;
         case CUSTOM -> customOrView(cfg, source, incomingDir);
      };
   }

   private static class_243 customOrView(RaycastAction.Cfg cfg, class_1297 source, class_243 fallback) {
      if (cfg.params.direction.isEmpty()) {
         return fallback;
      }

      Vector v = (Vector)cfg.params.direction.get();
      return cfg.params.space.toGlobal(source, new class_243(v.x(), v.y(), v.z()));
   }

   private static class_243 reflect(class_243 d, class_243 n) {
      class_243 unit = n.method_1029();
      return d.method_1020(unit.method_1021(2.0 * d.method_1026(unit)));
   }

   public record Aim(boolean pierce, Optional<Boolean> pierceBlocks, Optional<Boolean> pierceEntities, boolean aimAtTarget, boolean stopAtTarget) {
      public boolean piercesBlocks() {
         return (Boolean)this.pierceBlocks.orElse(this.pierce);
      }

      public boolean piercesEntities() {
         return (Boolean)this.pierceEntities.orElse(this.pierce);
      }
   }

   public record Cfg(RaycastAction.Params params, RaycastAction.Aim aim, RaycastAction.Hooks hooks, Optional<RaycastAction.Cfg> chain) {
   }

   public enum ChainDirection implements class_3542 {
      FORWARD("forward"),
      REFLECT("reflect"),
      CUSTOM("custom");

      public static final Codec<RaycastAction.ChainDirection> CODEC = class_3542.method_28140(RaycastAction.ChainDirection::values);
      private final String name;

      ChainDirection(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }

   private record EntityHit(class_1297 target, class_243 pos, double distSq) {
   }

   public record Hooks(
      Optional<BiEntityCondition> bientityCondition,
      Optional<BlockCondition> blockCondition,
      Optional<BiEntityAction> bientityAction,
      Optional<BlockAction> blockAction,
      Optional<EntityAction> beforeAction,
      Optional<EntityAction> hitAction,
      Optional<EntityAction> missAction,
      Optional<String> commandAtHit,
      Optional<Expression> commandHitOffset,
      Optional<String> commandAlongRay,
      Expression commandStep,
      boolean commandAlongRayOnlyOnHit
   ) {
   }

   public record Params(
      Optional<Expression> distance,
      boolean block,
      boolean entity,
      ShapeType shapeType,
      FluidHandling fluidHandling,
      Space space,
      Optional<Vector> direction,
      Optional<ParticleEffect> particle,
      Expression spacing,
      Optional<Expression> entityDistance,
      Optional<Expression> blockDistance,
      Optional<ExprVector> radius,
      Optional<Expression> coneAngle,
      RaycastAction.ChainDirection chainDirection
   ) {
   }

   private static final class Scope implements AutoCloseable {
      private final double distance;
      private final double hitX;
      private final double hitY;
      private final double hitZ;
      private final double count;
      private final double index;

      private Scope(class_243 origin, class_243 hit, int count, int index) {
         this.distance = ExprContext.push(RaycastAction.SLOT_DISTANCE, origin.method_1022(hit));
         this.hitX = ExprContext.push(RaycastAction.SLOT_HIT_X, hit.field_1352);
         this.hitY = ExprContext.push(RaycastAction.SLOT_HIT_Y, hit.field_1351);
         this.hitZ = ExprContext.push(RaycastAction.SLOT_HIT_Z, hit.field_1350);
         this.count = ExprContext.push(RaycastAction.SLOT_COUNT, count);
         this.index = ExprContext.push(RaycastAction.SLOT_INDEX, index);
      }

      public void close() {
         ExprContext.pop(RaycastAction.SLOT_DISTANCE, this.distance);
         ExprContext.pop(RaycastAction.SLOT_HIT_X, this.hitX);
         ExprContext.pop(RaycastAction.SLOT_HIT_Y, this.hitY);
         ExprContext.pop(RaycastAction.SLOT_HIT_Z, this.hitZ);
         ExprContext.pop(RaycastAction.SLOT_COUNT, this.count);
         ExprContext.pop(RaycastAction.SLOT_INDEX, this.index);
      }
   }
}
