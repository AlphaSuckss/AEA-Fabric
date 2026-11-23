package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import net.minecraft.class_1297;
import net.minecraft.class_3542;
import net.minecraft.class_5636;
import org.jetbrains.annotations.Nullable;

public final class ModifyCameraSubmersionPower extends PowerType<ModifyCameraSubmersionPower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.MODIFY_CAMERA_SUBMERSION);

   @Override
   public MapCodec<ModifyCameraSubmersionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ModifyCameraSubmersionPower.Submersion.CODEC.fieldOf("from").forGetter(ModifyCameraSubmersionPower.Config::from),
               ModifyCameraSubmersionPower.Submersion.CODEC.fieldOf("to").forGetter(ModifyCameraSubmersionPower.Config::to)
            )
            .apply(i, ModifyCameraSubmersionPower.Config::new)
      );
   }

   public static class_5636 remap(@Nullable class_1297 entity, class_5636 original) {
      if (entity != null && IN_USE.inUse() && PowerLookup.holdsType(entity, ApoliIds.MODIFY_CAMERA_SUBMERSION)) {
         class_5636[] result = new class_5636[]{original};
         PowerLookup.forEach(entity, ApoliIds.MODIFY_CAMERA_SUBMERSION, ModifyCameraSubmersionPower.Config.class, cfg -> {
            if (cfg.from.fog() == result[0]) {
               result[0] = cfg.to.fog();
            }
         });
         return result[0];
      } else {
         return original;
      }
   }

   public record Config(ModifyCameraSubmersionPower.Submersion from, ModifyCameraSubmersionPower.Submersion to) {
   }

   public enum Submersion implements class_3542 {
      NONE("none", class_5636.field_27888),
      WATER("water", class_5636.field_27886),
      LAVA("lava", class_5636.field_27885),
      POWDER_SNOW("powder_snow", class_5636.field_27887);

      public static final Codec<ModifyCameraSubmersionPower.Submersion> CODEC = class_3542.method_28140(ModifyCameraSubmersionPower.Submersion::values);
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
