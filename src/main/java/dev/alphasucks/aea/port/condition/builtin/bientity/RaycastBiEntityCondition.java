package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.builtin.entity.RaycastAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.ExprVector;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.FluidHandling;
import dev.alphasucks.aea.port.data.ShapeType;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.dev.DevParticles.Ray;
import dev.alphasucks.aea.port.util.InteractionRange;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import org.jetbrains.annotations.Nullable;

public final class RaycastBiEntityCondition implements ConditionType<BiEntityCtx, RaycastBiEntityCondition.Cfg> {
   private static final MapCodec<RaycastBiEntityCondition.Cfg> CODEC = AliasingMapCodec.wrap(
      RecordCodecBuilder.mapCodec(
         i -> i.group(
               Expression.FLOAT_OR_EXPR.optionalFieldOf("distance").forGetter(RaycastBiEntityCondition.Cfg::distance),
               Codec.BOOL.optionalFieldOf("block", true).forGetter(RaycastBiEntityCondition.Cfg::block),
               ShapeType.CODEC.optionalFieldOf("shape_type", ShapeType.VISUAL).forGetter(RaycastBiEntityCondition.Cfg::shapeType),
               FluidHandling.CODEC.optionalFieldOf("fluid_handling", FluidHandling.ANY).forGetter(RaycastBiEntityCondition.Cfg::fluidHandling),
               Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(RaycastBiEntityCondition.Cfg::space),
               Vector.CODEC.optionalFieldOf("direction").forGetter(RaycastBiEntityCondition.Cfg::direction),
               ExprVector.SCALAR_OR_VECTOR.optionalFieldOf("radius").forGetter(RaycastBiEntityCondition.Cfg::radius),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("cone_angle").forGetter(RaycastBiEntityCondition.Cfg::coneAngle)
            )
            .apply(i, RaycastBiEntityCondition.Cfg::new)
      ),
      Map.of("entity_distance", "distance")
   );

   @Override
   public MapCodec<RaycastBiEntityCondition.Cfg> codec() {
      return CODEC;
   }

   public boolean test(RaycastBiEntityCondition.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 target = ctx.target();
      if (actor != null && target != null && actor != target && actor.method_37908() == target.method_37908()) {
         class_243 origin = actor.method_33571();
         class_243 dir = cfg.direction.isPresent()
            ? cfg.space.toGlobal(actor, new class_243(((Vector)cfg.direction.get()).x(), ((Vector)cfg.direction.get()).y(), ((Vector)cfg.direction.get()).z()))
            : actor.method_5828(1.0F);
         if (dir.method_1027() < 1.0E-6) {
            dir = actor.method_5828(1.0F);
         }

         dir = dir.method_1029();
         double range = cfg.distance.isPresent() ? ((Expression)cfg.distance.get()).eval(actor) : InteractionRange.entity(actor);
         double rx = 0.0;
         double ry = 0.0;
         double rz = 0.0;
         if (cfg.radius.isPresent()) {
            Vector radius = ((ExprVector)cfg.radius.get()).resolve(actor);
            rx = radius.x();
            ry = radius.y();
            rz = radius.z();
         }

         boolean cone = cfg.coneAngle.isPresent();
         double coneAngle = cone ? ((Expression)cfg.coneAngle.get()).eval(actor) : -1.0;
         double coneCos = cone ? Math.cos(Math.toRadians(coneAngle)) : -1.0;
         class_243 hit = RaycastAction.entityHit(target, origin, dir, origin.method_1019(dir.method_1021(range)), range, rx, ry, rz, cone, coneCos);
         class_3965 blocker = null;
         if (hit != null && cfg.block && origin.method_1025(hit) >= 1.0E-8) {
            class_3965 clip = actor.method_37908().method_17742(new class_3959(origin, hit, cfg.shapeType.vanilla(), cfg.fluidHandling.vanilla(), actor));
            if (clip.method_17783() != class_240.field_1333) {
               blocker = clip;
            }
         }

         boolean passed = hit != null && blocker == null;
         if (DevMode.any()) {
            outline(actor.method_37908(), cfg, actor, target, origin, dir, range, rx, ry, rz, coneAngle, hit, blocker, passed);
         }

         return passed;
      } else {
         return false;
      }
   }

   private static void outline(
      class_1937 world,
      RaycastBiEntityCondition.Cfg cfg,
      class_1297 actor,
      class_1297 target,
      class_243 origin,
      class_243 dir,
      double range,
      double rx,
      double ry,
      double rz,
      double coneAngle,
      @Nullable class_243 hit,
      @Nullable class_3965 blocker,
      boolean passed
   ) {
      if (world instanceof class_3218 level) {
         Ray kind = Ray.CONDITION;
         if (DevParticles.due(level, actor, cfg)) {
            DevParticles.ray(level, kind, origin, dir, 0.0, range);
            DevParticles.detection(level, kind, origin, dir, range, rx, ry, rz, coneAngle);
         }

         if (hit != null && DevParticles.due(level, actor, target, cfg)) {
            DevParticles.segment(level, kind, origin, hit, blocker == null ? Double.POSITIVE_INFINITY : origin.method_1022(blocker.method_17784()));
            if (blocker != null) {
               DevParticles.mark(level, kind, blocker.method_17777(), true);
            }

            DevParticles.mark(level, kind, target, passed);
         }
      }
   }

   public record Cfg(
      Optional<Expression> distance,
      boolean block,
      ShapeType shapeType,
      FluidHandling fluidHandling,
      Space space,
      Optional<Vector> direction,
      Optional<ExprVector> radius,
      Optional<Expression> coneAngle
   ) {
   }
}
