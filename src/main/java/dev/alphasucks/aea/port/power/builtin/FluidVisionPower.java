package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_3542;
import net.minecraft.class_5636;
import org.jetbrains.annotations.Nullable;

public final class FluidVisionPower extends PowerType<FluidVisionPower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.FLUID_VISION);

   @Override
   public MapCodec<FluidVisionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               FluidVisionPower.Submersion.CODEC.optionalFieldOf("fluid", FluidVisionPower.Submersion.LAVA).forGetter(FluidVisionPower.Config::fluid),
               Codec.FLOAT.optionalFieldOf("start", 0.0F).forGetter(FluidVisionPower.Config::start),
               Codec.FLOAT.optionalFieldOf("end", 15.0F).forGetter(FluidVisionPower.Config::end),
               FluidVisionPower.FogColor.CODEC.optionalFieldOf("fog_color").forGetter(FluidVisionPower.Config::fogColor),
               Codec.BOOL.optionalFieldOf("render_overlay", true).forGetter(FluidVisionPower.Config::renderOverlay)
            )
            .apply(i, FluidVisionPower.Config::new)
      );
   }

   @Nullable
   public static FluidVisionPower.Config activeFor(@Nullable class_1297 entity, class_5636 fog) {
      if (entity != null && fog != null && IN_USE.inUse() && PowerLookup.holdsType(entity, ApoliIds.FLUID_VISION)) {
         FluidVisionPower.Config[] best = new FluidVisionPower.Config[]{null};
         PowerLookup.forEach(entity, ApoliIds.FLUID_VISION, FluidVisionPower.Config.class, cfg -> {
            if (cfg.fluid.fog() == fog) {
               if (best[0] == null || cfg.end > best[0].end) {
                  best[0] = cfg;
               }
            }
         });
         return best[0];
      } else {
         return null;
      }
   }

   public record Config(FluidVisionPower.Submersion fluid, float start, float end, Optional<FluidVisionPower.FogColor> fogColor, boolean renderOverlay) {
   }

   public record FogColor(float red, float green, float blue) {
      public static final Codec<FluidVisionPower.FogColor> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               Codec.FLOAT.optionalFieldOf("red", 0.0F).forGetter(FluidVisionPower.FogColor::red),
               Codec.FLOAT.optionalFieldOf("green", 0.0F).forGetter(FluidVisionPower.FogColor::green),
               Codec.FLOAT.optionalFieldOf("blue", 0.0F).forGetter(FluidVisionPower.FogColor::blue)
            )
            .apply(i, FluidVisionPower.FogColor::new)
      );
   }

   public enum Submersion implements class_3542 {
      NONE("none", class_5636.field_27888),
      WATER("water", class_5636.field_27886),
      LAVA("lava", class_5636.field_27885),
      POWDER_SNOW("powder_snow", class_5636.field_27887);

      public static final Codec<FluidVisionPower.Submersion> CODEC = class_3542.method_28140(FluidVisionPower.Submersion::values);
      private final String name;
      private final class_5636 fog;

      Submersion(String name, class_5636 fog) {
         this.name = name;
         this.fog = fog;
      }

      public class_5636 fog() {
         return this.fog;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
