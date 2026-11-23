package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record CameraKeyframe(
   float time,
   Optional<Float> x,
   Optional<Float> y,
   Optional<Float> z,
   Optional<Float> pitch,
   Optional<Float> yaw,
   Optional<Float> roll,
   Optional<Float> fov,
   Easing easing
) {
   public static final Codec<CameraKeyframe> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            Codec.FLOAT.fieldOf("time").forGetter(CameraKeyframe::time),
            Codec.FLOAT.optionalFieldOf("x").forGetter(CameraKeyframe::x),
            Codec.FLOAT.optionalFieldOf("y").forGetter(CameraKeyframe::y),
            Codec.FLOAT.optionalFieldOf("z").forGetter(CameraKeyframe::z),
            Codec.FLOAT.optionalFieldOf("pitch").forGetter(CameraKeyframe::pitch),
            Codec.FLOAT.optionalFieldOf("yaw").forGetter(CameraKeyframe::yaw),
            Codec.FLOAT.optionalFieldOf("roll").forGetter(CameraKeyframe::roll),
            Codec.FLOAT.optionalFieldOf("fov").forGetter(CameraKeyframe::fov),
            Easing.CODEC.optionalFieldOf("easing", Easing.LINEAR).forGetter(CameraKeyframe::easing)
         )
         .apply(i, CameraKeyframe::new)
   );

   Optional<Float> channel(int channel) {
      return switch (channel) {
         case 0 -> this.x;
         case 1 -> this.y;
         case 2 -> this.z;
         case 3 -> this.pitch;
         case 4 -> this.yaw;
         case 5 -> this.roll;
         default -> this.fov;
      };
   }
}
