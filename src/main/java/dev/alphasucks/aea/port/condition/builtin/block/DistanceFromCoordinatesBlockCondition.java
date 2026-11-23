package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.data.Comparison;
import dev.alphasucks.aea.port.data.Vector;
import java.util.Optional;
import net.minecraft.class_2338;
import net.minecraft.class_2874;
import net.minecraft.class_3218;

public final class DistanceFromCoordinatesBlockCondition implements ConditionType<BlockCtx, DistanceFromCoordinatesBlockCondition.Cfg> {
   @Override
   public MapCodec<DistanceFromCoordinatesBlockCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("reference", "world_origin").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::reference),
               Vector.CODEC.optionalFieldOf("offset").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::offset),
               Codec.BOOL.optionalFieldOf("ignore_x", false).forGetter(DistanceFromCoordinatesBlockCondition.Cfg::ignoreX),
               Codec.BOOL.optionalFieldOf("ignore_y", false).forGetter(DistanceFromCoordinatesBlockCondition.Cfg::ignoreY),
               Codec.BOOL.optionalFieldOf("ignore_z", false).forGetter(DistanceFromCoordinatesBlockCondition.Cfg::ignoreZ),
               Codec.STRING.optionalFieldOf("shape", "cube").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::shape),
               Codec.BOOL.optionalFieldOf("scale_reference_to_dimension", true).forGetter(DistanceFromCoordinatesBlockCondition.Cfg::scaleReferenceToDimension),
               Codec.BOOL.optionalFieldOf("result_on_the_wrong_dimension").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::resultOnWrongDimension),
               Codec.INT.optionalFieldOf("round_to_digit").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::roundToDigit),
               Comparison.CODEC.fieldOf("comparison").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::comparison),
               Codec.FLOAT.fieldOf("compare_to").forGetter(DistanceFromCoordinatesBlockCondition.Cfg::compareTo)
            )
            .apply(i, DistanceFromCoordinatesBlockCondition.Cfg::new)
      );
   }

   public boolean test(DistanceFromCoordinatesBlockCondition.Cfg cfg, BlockCtx ctx) {
      if (ctx.level() instanceof class_3218 serverLevel) {
         double var21 = 0.0;
         double refY = 0.0;
         double refZ = 0.0;
         class_3218 referenceLevel = serverLevel;
         if ("world_spawn".equals(cfg.reference)) {
            referenceLevel = serverLevel.method_8503().method_30002();
            class_2338 spawn = referenceLevel.method_43126();
            var21 = spawn.method_10263();
            refY = spawn.method_10264();
            refZ = spawn.method_10260();
         }

         if (cfg.offset.isPresent()) {
            Vector v = (Vector)cfg.offset.get();
            var21 += v.x();
            refY += v.y();
            refZ += v.z();
         }

         if (cfg.scaleReferenceToDimension) {
            class_2874 from = referenceLevel.method_8597();
            class_2874 to = serverLevel.method_8597();
            double scale = from.comp_646() / to.comp_646();
            var21 *= scale;
            refZ *= scale;
         } else if (!serverLevel.method_27983().equals(referenceLevel.method_27983()) && cfg.resultOnWrongDimension.isPresent()) {
            return (Boolean)cfg.resultOnWrongDimension.get();
         }

         double dx = cfg.ignoreX ? 0.0 : ctx.pos().method_10263() - var21;
         double dy = cfg.ignoreY ? 0.0 : ctx.pos().method_10264() - refY;
         double dz = cfg.ignoreZ ? 0.0 : ctx.pos().method_10260() - refZ;

         double distance = switch (cfg.shape) {
            case "star", "star_hollow" -> Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
            case "cube", "cube_hollow" -> Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
            default -> Math.sqrt(dx * dx + dy * dy + dz * dz);
         };
         if (cfg.roundToDigit.isPresent()) {
            double factor = Math.pow(10.0, ((Integer)cfg.roundToDigit.get()).intValue());
            distance = Math.round(distance * factor) / factor;
         }

         return cfg.comparison.compare(distance, cfg.compareTo);
      } else {
         return false;
      }
   }

   public record Cfg(
      String reference,
      Optional<Vector> offset,
      boolean ignoreX,
      boolean ignoreY,
      boolean ignoreZ,
      String shape,
      boolean scaleReferenceToDimension,
      Optional<Boolean> resultOnWrongDimension,
      Optional<Integer> roundToDigit,
      Comparison comparison,
      float compareTo
   ) {
   }
}
