package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum RenderMode {
   TRANSLUCENT,
   TRANSLUCENT_CULL,
   CUTOUT,
   CUTOUT_NO_CULL,
   SOLID,
   EMISSIVE,
   EYES,
   ENERGY_SWIRL;

   private static final Map<String, RenderMode> BY_NAME = new HashMap();
   public static final Codec<RenderMode> CODEC;

   static {
      for (RenderMode mode : values()) {
         BY_NAME.put(ModelParts.normalize(mode.name()), mode);
      }

      BY_NAME.put(ModelParts.normalize("glow"), EMISSIVE);
      BY_NAME.put(ModelParts.normalize("translucent_emissive"), EMISSIVE);
      BY_NAME.put(ModelParts.normalize("glowing"), EMISSIVE);
      BY_NAME.put(ModelParts.normalize("eyes_emissive"), EYES);
      BY_NAME.put(ModelParts.normalize("swirl"), ENERGY_SWIRL);
      CODEC = Codec.STRING.comapFlatMap(string -> {
         RenderMode modex = (RenderMode)BY_NAME.get(ModelParts.normalize(string));
         return modex != null ? DataResult.success(modex) : DataResult.error(() -> "Unknown render type: '" + string + "'");
      }, modex -> modex.name().toLowerCase(Locale.ROOT));
   }
}
