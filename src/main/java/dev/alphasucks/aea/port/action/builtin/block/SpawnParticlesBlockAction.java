package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.ExprVector;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.ParticleBroadcast;
import dev.alphasucks.aea.port.data.ParticleEffect;
import dev.alphasucks.aea.port.data.ParticlePlacement;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.Vector;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_2394;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3542;

public final class SpawnParticlesBlockAction implements ActionType<BlockCtx, SpawnParticlesBlockAction.Cfg> {
   private static final ExprVector DEFAULT_SPREAD = ExprVector.of(0.25F, 0.25F, 0.25F);
   private static final Expression ZERO = Expression.constant(0.0);
   private static final Expression ONE = Expression.constant(1.0);
   private static final Optional<Space> WORLD = Optional.of(Space.WORLD);

   @Override
   public MapCodec<SpawnParticlesBlockAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ParticleEffect.CODEC.fieldOf("particle").forGetter(SpawnParticlesBlockAction.Cfg::particle),
               Expression.INT_OR_EXPR.optionalFieldOf("count", ONE).forGetter(SpawnParticlesBlockAction.Cfg::count),
               ParticlePlacement.SPEED_CODEC.optionalFieldOf("speed", ParticlePlacement.NO_SPEED).forGetter(SpawnParticlesBlockAction.Cfg::speed),
               Codec.BOOL.optionalFieldOf("force", false).forGetter(SpawnParticlesBlockAction.Cfg::force),
               ExprVector.SCALAR_OR_VECTOR.optionalFieldOf("spread", DEFAULT_SPREAD).forGetter(SpawnParticlesBlockAction.Cfg::spread),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_x", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::offsetX),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_y", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::offsetY),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("offset_z", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::offsetZ),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("velocity_x", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::velocityX),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("velocity_y", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::velocityY),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("velocity_z", ZERO).forGetter(SpawnParticlesBlockAction.Cfg::velocityZ),
               SpawnParticlesBlockAction.Anchor.CODEC
                  .optionalFieldOf("anchor", SpawnParticlesBlockAction.Anchor.HIT)
                  .forGetter(SpawnParticlesBlockAction.Cfg::anchor)
            )
            .apply(i, SpawnParticlesBlockAction.Cfg::new)
      );
   }

   public void run(SpawnParticlesBlockAction.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 level) {
         class_1297 var16 = ctx.actor();
         class_2394 opts = cfg.particle.resolve(level, var16);
         if (opts != null) {
            class_243 origin = anchor(cfg.anchor, ctx).method_1031(cfg.offsetX.eval(var16), cfg.offsetY.eval(var16), cfg.offsetZ.eval(var16));
            class_243 velocity = new class_243(cfg.velocityX.eval(var16), cfg.velocityY.eval(var16), cfg.velocityZ.eval(var16));
            float scalarSpeed = ParticlePlacement.scalarSpeed(var16, cfg.speed);
            if (velocity.method_1027() < 1.0E-9) {
               Vector vector = ParticlePlacement.speedVector(var16, cfg.speed);
               if (vector != null) {
                  velocity = new class_243(vector.x(), vector.y(), vector.z());
               }
            }

            int count = Math.max(0, cfg.count.evalInt(var16));
            Vector spread = cfg.spread.resolve(var16);
            List<class_2596<?>> packets = null;
            List<class_3222> players = level.method_18456();

            for (int i = 0; i < players.size(); i++) {
               class_3222 player = (class_3222)players.get(i);
               if (ParticleBroadcast.inRange(player, cfg.force, origin.field_1352, origin.field_1351, origin.field_1350)) {
                  if (packets == null) {
                     packets = ParticlePlacement.packets(opts, cfg.force, origin, velocity, count, spread, scalarSpeed, level, var16, null, WORLD);
                  }

                  for (int p = 0; p < packets.size(); p++) {
                     ParticleBroadcast.send(level, player, (class_2596<?>)packets.get(p));
                  }
               }
            }
         }
      }
   }

   private static class_243 anchor(SpawnParticlesBlockAction.Anchor anchor, BlockCtx ctx) {
      class_2338 pos = ctx.pos();

      return switch (anchor) {
         case HIT -> ctx.hit() != null ? ctx.hit() : class_243.method_24953(pos);
         case CENTER -> class_243.method_24953(pos);
         case CORNER -> class_243.method_24954(pos);
      };
   }

   public enum Anchor implements class_3542 {
      HIT("hit"),
      CENTER("center"),
      CORNER("corner");

      public static final Codec<SpawnParticlesBlockAction.Anchor> CODEC = class_3542.method_28140(SpawnParticlesBlockAction.Anchor::values);
      private final String name;

      Anchor(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }

   public record Cfg(
      ParticleEffect particle,
      Expression count,
      Either<Expression, ExprVector> speed,
      boolean force,
      ExprVector spread,
      Expression offsetX,
      Expression offsetY,
      Expression offsetZ,
      Expression velocityX,
      Expression velocityY,
      Expression velocityZ,
      SpawnParticlesBlockAction.Anchor anchor
   ) {
   }
}
