package dev.alphasucks.aea.port.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.data.ModelParts;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ParticleFacing {
   CAMERA,
   VERTICAL;

   private static final Map<String, ParticleFacing> BY_NAME = new HashMap();
   public static final Codec<ParticleFacing> CODEC;

   static {
      for (ParticleFacing facing : values()) {
         BY_NAME.put(ModelParts.normalize(facing.name()), facing);
      }

      BY_NAME.put("billboard", CAMERA);
      BY_NAME.put("upright", VERTICAL);
      CODEC = Codec.STRING
         .comapFlatMap(
            string -> {
               ParticleFacing facingx = (ParticleFacing)BY_NAME.get(ModelParts.normalize(string));
               return facingx != null
                  ? DataResult.success(facingx)
                  : DataResult.error(() -> "Unknown particle facing: '" + string + "' (expected camera or vertical)");
            },
            facingx -> facingx.name().toLowerCase(Locale.ROOT)
         );
   }
}
