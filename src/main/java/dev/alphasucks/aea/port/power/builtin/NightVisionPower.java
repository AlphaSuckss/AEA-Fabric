package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import org.jetbrains.annotations.Nullable;

public final class NightVisionPower extends PowerType<NightVisionPower.Config> {
   private static final ThreadLocal<List<NightVisionPower.Config>> SCRATCH = ThreadLocal.withInitial(() -> new ArrayList(2));

   @Override
   public MapCodec<NightVisionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Expression.FLOAT_OR_EXPR.optionalFieldOf("strength", Expression.constant(1.0)).forGetter(NightVisionPower.Config::strength))
            .apply(i, NightVisionPower.Config::new)
      );
   }

   public static float strengthFor(@Nullable class_1309 entity) {
      if (entity == null) {
         return 0.0F;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         if (container.powersOfType(ApoliIds.NIGHT_VISION).isEmpty()) {
            return 0.0F;
         }

         List<NightVisionPower.Config> active = (List<NightVisionPower.Config>)SCRATCH.get();
         active.clear();
         PowerLookup.collect(entity, ApoliIds.NIGHT_VISION, NightVisionPower.Config.class, active);
         float best = 0.0F;

         for (int i = 0; i < active.size(); i++) {
            float strength = (float)((NightVisionPower.Config)active.get(i)).strength().eval(entity);
            if (strength > best) {
               best = strength;
            }
         }

         active.clear();
         return best;
      } else {
         return 0.0F;
      }
   }

   public static float effectScale(class_1309 entity) {
      class_1293 effect = entity.method_6112(class_1294.field_5925);
      if (effect == null) {
         return 0.0F;
      } else {
         return !effect.method_48557(200) ? 1.0F : 0.7F + class_3532.method_15374(effect.method_5584() * (float) Math.PI * 0.2F) * 0.3F;
      }
   }

   public record Config(Expression strength) {
   }
}
