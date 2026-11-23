package dev.alphasucks.aea.port.rope;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import java.util.Optional;
import net.minecraft.class_3532;
import net.minecraft.class_3542;

public record RopeParams(
   float minLength,
   float maxLength,
   float stiffness,
   float radialDamping,
   float springScaling,
   float swingBoost,
   float maxSwingSpeed,
   float controlAccel,
   float reelStep,
   float slackPullRate,
   boolean constrainFrom,
   boolean constrainTo,
   boolean controllable,
   RopeParams.Mode mode,
   float breakBeyond,
   Optional<Float> startLength
) {
   public static final RopeParams DEFAULT = new RopeParams(
      1.0F, 30.0F, 0.1F, 0.85F, 0.65F, 1.08F, 0.7F, 0.08F, 0.2F, 5.0F, true, true, true, RopeParams.Mode.LEASH, 0.0F, Optional.empty()
   );
   public static final MapCodec<RopeParams> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            Codec.FLOAT.optionalFieldOf("min_length", DEFAULT.minLength).forGetter(RopeParams::minLength),
            Codec.FLOAT.optionalFieldOf("max_length", DEFAULT.maxLength).forGetter(RopeParams::maxLength),
            Codec.FLOAT.optionalFieldOf("stiffness", DEFAULT.stiffness).forGetter(RopeParams::stiffness),
            Codec.FLOAT.optionalFieldOf("radial_damping", DEFAULT.radialDamping).forGetter(RopeParams::radialDamping),
            Codec.FLOAT.optionalFieldOf("spring_scaling", DEFAULT.springScaling).forGetter(RopeParams::springScaling),
            Codec.FLOAT.optionalFieldOf("swing_boost", DEFAULT.swingBoost).forGetter(RopeParams::swingBoost),
            Codec.FLOAT.optionalFieldOf("max_swing_speed", DEFAULT.maxSwingSpeed).forGetter(RopeParams::maxSwingSpeed),
            Codec.FLOAT.optionalFieldOf("control_accel", DEFAULT.controlAccel).forGetter(RopeParams::controlAccel),
            Codec.FLOAT.optionalFieldOf("reel_step", DEFAULT.reelStep).forGetter(RopeParams::reelStep),
            Codec.FLOAT.optionalFieldOf("slack_pull_rate", DEFAULT.slackPullRate).forGetter(RopeParams::slackPullRate),
            Codec.BOOL.optionalFieldOf("constrain_from", DEFAULT.constrainFrom).forGetter(RopeParams::constrainFrom),
            Codec.BOOL.optionalFieldOf("constrain_to", DEFAULT.constrainTo).forGetter(RopeParams::constrainTo),
            Codec.BOOL.optionalFieldOf("controllable", DEFAULT.controllable).forGetter(RopeParams::controllable),
            RopeParams.Mode.CODEC.optionalFieldOf("mode", DEFAULT.mode).forGetter(RopeParams::mode),
            Codec.FLOAT.optionalFieldOf("break_beyond", DEFAULT.breakBeyond).forGetter(RopeParams::breakBeyond),
            Codec.FLOAT.optionalFieldOf("start_length").forGetter(RopeParams::startLength)
         )
         .apply(i, RopeParams::new)
   );

   public RopeParams {
      minLength = Math.max(0.0F, minLength);
      maxLength = Math.max(minLength, maxLength);
      stiffness = clamped("stiffness", stiffness, 0.0F, 1.0F);
      radialDamping = clamped("radial_damping", radialDamping, 0.0F, 1.0F);
      springScaling = clamped("spring_scaling", springScaling, 0.0F, 1.0F);
      swingBoost = clamped("swing_boost", swingBoost, 0.0F, 2.0F);
      maxSwingSpeed = Math.max(0.0F, maxSwingSpeed);
      controlAccel = Math.max(0.0F, controlAccel);
      reelStep = Math.max(0.0F, reelStep);
      slackPullRate = Math.max(0.0F, slackPullRate);
      breakBeyond = Math.max(0.0F, breakBeyond);
   }

   private static float clamped(String field, float value, float min, float max) {
      if (Float.isNaN(value)) {
         return min;
      }

      if (value >= min && value <= max) {
         return value;
      }

      float fixed = class_3532.method_15363(value, min, max);
      Apoli.LOGGER
         .warn(
            "[Apoli] Rope '{}' was {} — outside the stable range [{}, {}], so it was clamped to {}. Values beyond that range make the rope spring integrator diverge.",
            new Object[]{field, value, min, max, fixed}
         );
      return fixed;
   }

   public double initialLength(double endpointDistance) {
      double wanted = (Double)this.startLength.map(Float::doubleValue).orElseGet(() -> this.controllable ? endpointDistance : this.maxLength);
      return class_3532.method_15350(wanted, this.minLength, this.maxLength);
   }

   public enum Mode implements class_3542 {
      LEASH("leash"),
      SPRING("spring"),
      RIGID("rigid");

      public static final Codec<RopeParams.Mode> CODEC = class_3542.method_28140(RopeParams.Mode::values);
      private final String name;

      Mode(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
