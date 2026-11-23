package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Shape;
import dev.alphasucks.aea.port.data.Vector;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_3542;
import net.minecraft.server.MinecraftServer;

public final class DistanceFromCoordinatesCondition implements ConditionType<EntityCtx, DistanceFromCoordinatesCondition.Cfg> {
   @Override
   public MapCodec<DistanceFromCoordinatesCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               DistanceFromCoordinatesCondition.Reference.CODEC
                  .optionalFieldOf("reference", DistanceFromCoordinatesCondition.Reference.WORLD_ORIGIN)
                  .forGetter(DistanceFromCoordinatesCondition.Cfg::reference),
               Vector.CODEC.optionalFieldOf("offset").forGetter(DistanceFromCoordinatesCondition.Cfg::offset),
               Codec.BOOL.optionalFieldOf("ignore_x", false).forGetter(DistanceFromCoordinatesCondition.Cfg::ignoreX),
               Codec.BOOL.optionalFieldOf("ignore_y", false).forGetter(DistanceFromCoordinatesCondition.Cfg::ignoreY),
               Codec.BOOL.optionalFieldOf("ignore_z", false).forGetter(DistanceFromCoordinatesCondition.Cfg::ignoreZ),
               Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(DistanceFromCoordinatesCondition.Cfg::shape),
               Codec.BOOL.optionalFieldOf("scale_reference_to_dimension", true).forGetter(DistanceFromCoordinatesCondition.Cfg::scaleReferenceToDimension),
               Codec.BOOL.optionalFieldOf("result_on_the_wrong_dimension").forGetter(DistanceFromCoordinatesCondition.Cfg::resultOnWrongDimension),
               Codec.INT.optionalFieldOf("round_to_digit").forGetter(DistanceFromCoordinatesCondition.Cfg::roundToDigit),
               Comparison.CODEC.fieldOf("comparison").forGetter(DistanceFromCoordinatesCondition.Cfg::comparison),
               Codec.FLOAT.fieldOf("compare_to").forGetter(DistanceFromCoordinatesCondition.Cfg::compareTo)
            )
            .apply(i, DistanceFromCoordinatesCondition.Cfg::new)
      );
   }

   public boolean test(DistanceFromCoordinatesCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 e = ctx.raw();
      class_1937 level = ctx.level();
      double refX;
      double refY;
      double refZ;
      if (cfg.reference == DistanceFromCoordinatesCondition.Reference.WORLD_SPAWN) {
         class_2338 spawn = level.method_43126();
         refX = spawn.method_10263();
         refY = spawn.method_10264();
         refZ = spawn.method_10260();
         MinecraftServer server = level.method_8503();
         if (server != null && cfg.resultOnWrongDimension.isPresent()) {
            class_3218 overworld = server.method_30002();
            if (level != overworld) {
               return (Boolean)cfg.resultOnWrongDimension.get();
            }
         }
      } else {
         refX = 0.0;
         refY = 0.0;
         refZ = 0.0;
      }

      if (cfg.offset.isPresent()) {
         refX += ((Vector)cfg.offset.get()).x();
         refY += ((Vector)cfg.offset.get()).y();
         refZ += ((Vector)cfg.offset.get()).z();
      }

      if (cfg.scaleReferenceToDimension) {
         double scale = level.method_8597().comp_646();
         refX /= scale;
         refZ /= scale;
      }

      double dx = cfg.ignoreX ? 0.0 : e.method_23317() - refX;
      double dy = cfg.ignoreY ? 0.0 : e.method_23318() - refY;
      double dz = cfg.ignoreZ ? 0.0 : e.method_23321() - refZ;

      double dist = switch (cfg.shape.solid()) {
         case CUBE -> Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
         case STAR -> Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
         default -> Math.sqrt(dx * dx + dy * dy + dz * dz);
      };
      if (cfg.roundToDigit.isPresent()) {
         double scale = Math.pow(10.0, ((Integer)cfg.roundToDigit.get()).intValue());
         dist = Math.round(dist * scale) / scale;
      }

      return cfg.comparison.compare((float)dist, cfg.compareTo);
   }

   public record Cfg(
      DistanceFromCoordinatesCondition.Reference reference,
      Optional<Vector> offset,
      boolean ignoreX,
      boolean ignoreY,
      boolean ignoreZ,
      Shape shape,
      boolean scaleReferenceToDimension,
      Optional<Boolean> resultOnWrongDimension,
      Optional<Integer> roundToDigit,
      Comparison comparison,
      float compareTo
   ) {
   }

   public enum Reference implements class_3542 {
      WORLD_ORIGIN("world_origin"),
      WORLD_SPAWN("world_spawn");

      public static final Codec<DistanceFromCoordinatesCondition.Reference> CODEC = class_3542.method_28140(DistanceFromCoordinatesCondition.Reference::values);
      private final String name;

      Reference(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
