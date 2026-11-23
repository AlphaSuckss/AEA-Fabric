package dev.alphasucks.aea.port.condition.builtin.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.data.BodyHit;
import dev.alphasucks.aea.port.data.BodyPart;
import java.util.List;
import java.util.Optional;

public final class BodyPartDamageCondition implements ConditionType<DamageCtx, BodyPartDamageCondition.Cfg> {
   private static MapCodec<Optional<Float>> bound(String name) {
      return LoggedOptionalField.strict(name, Codec.FLOAT);
   }

   @Override
   public MapCodec<BodyPartDamageCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("body_part", BodyPart.STRICT_LIST_CODEC)
                  .forGetter(cfg -> cfg.bodyParts().isEmpty() ? Optional.empty() : Optional.of(cfg.bodyParts())),
               bound("x_min").forGetter(cfg -> Optional.of(cfg.xMin())),
               bound("x_max").forGetter(cfg -> Optional.of(cfg.xMax())),
               bound("y_min").forGetter(cfg -> Optional.of(cfg.yMin())),
               bound("y_max").forGetter(cfg -> Optional.of(cfg.yMax())),
               bound("z_min").forGetter(cfg -> Optional.of(cfg.zMin())),
               bound("z_max").forGetter(cfg -> Optional.of(cfg.zMax())),
               LoggedOptionalField.strict("require_hit_data", Codec.BOOL).forGetter(cfg -> Optional.of(cfg.requireHitData()))
            )
            .apply(
               i,
               (parts, xMin, xMax, yMin, yMax, zMin, zMax, requireHitData) -> new BodyPartDamageCondition.Cfg(
                  (List<BodyPart>)parts.orElse(List.of()),
                  (Float)xMin.orElse(-1.0F),
                  (Float)xMax.orElse(1.0F),
                  (Float)yMin.orElse(0.0F),
                  (Float)yMax.orElse(1.0F),
                  (Float)zMin.orElse(-1.0F),
                  (Float)zMax.orElse(1.0F),
                  (Boolean)requireHitData.orElse(false)
               )
            )
      );
   }

   public boolean test(BodyPartDamageCondition.Cfg cfg, DamageCtx ctx) {
      BodyHit hit = BodyHit.resolve(ctx.target(), ctx.source());
      if (cfg.requireHitData() && !hit.precise()) {
         return false;
      }

      if (!hit.within(cfg.xMin(), cfg.xMax(), cfg.yMin(), cfg.yMax(), cfg.zMin(), cfg.zMax())) {
         return false;
      }

      List<BodyPart> parts = cfg.bodyParts();
      if (parts.isEmpty()) {
         return true;
      }

      for (int i = 0; i < parts.size(); i++) {
         if (hit.in((BodyPart)parts.get(i), ctx.target())) {
            return true;
         }
      }

      return false;
   }

   public record Cfg(List<BodyPart> bodyParts, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, boolean requireHitData) {
   }
}
