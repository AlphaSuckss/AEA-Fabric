package dev.alphasucks.aea.port.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.data.ModelParts;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ParticleBlend {
   TRANSLUCENT,
   ADDITIVE;

   private static final Map<String, ParticleBlend> BY_NAME = new HashMap();
   public static final Codec<ParticleBlend> CODEC;

   static {
      for (ParticleBlend blend : values()) {
         BY_NAME.put(ModelParts.normalize(blend.name()), blend);
      }

      BY_NAME.put("alpha", TRANSLUCENT);
      BY_NAME.put("glow", ADDITIVE);
      CODEC = Codec.STRING
         .comapFlatMap(
            string -> {
               ParticleBlend blendx = (ParticleBlend)BY_NAME.get(ModelParts.normalize(string));
               return blendx != null
                  ? DataResult.success(blendx)
                  : DataResult.error(() -> "Unknown particle blend: '" + string + "' (expected translucent or additive)");
            },
            blendx -> blendx.name().toLowerCase(Locale.ROOT)
         );
   }
}
