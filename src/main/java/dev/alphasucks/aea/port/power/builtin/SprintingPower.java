package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.shared.EmptyCfg;
import net.minecraft.class_1309;
import net.minecraft.class_2960;

public final class SprintingPower extends PowerType<EmptyCfg> {
   public static final class_2960 CANONICAL = Apoli.id("sprinting");

   @Override
   public MapCodec<EmptyCfg> configCodec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public static boolean isSprinting(class_1309 entity) {
      return ApoliPowers.anyOfType(CANONICAL) && PowerLookup.hasActive(entity, CANONICAL);
   }
}
