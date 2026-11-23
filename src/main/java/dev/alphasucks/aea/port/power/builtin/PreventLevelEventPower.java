package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

public final class PreventLevelEventPower extends PowerType<PreventLevelEventPower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.PREVENT_LEVEL_EVENT);

   @Override
   public MapCodec<PreventLevelEventPower.Config> configCodec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(SingleOrList.of(Codec.INT).optionalFieldOf("events").forGetter(PreventLevelEventPower.Config::events))
               .apply(i, PreventLevelEventPower.Config::new)
         ),
         Map.of("event", "events")
      );
   }

   public static boolean prevents(@Nullable class_1657 player, int event) {
      return player != null && IN_USE.inUse() && PowerLookup.holdsType(player, ApoliIds.PREVENT_LEVEL_EVENT)
         ? PowerLookup.anyActive(player, ApoliIds.PREVENT_LEVEL_EVENT, PreventLevelEventPower.Config.class, PreventLevelEventPower.Config::matches, event)
         : false;
   }

   public record Config(Optional<List<Integer>> events, int[] ids) {
      public Config(Optional<List<Integer>> events) {
         this(events, (int[])events.map(list -> list.stream().mapToInt(Integer::intValue).toArray()).orElse(null));
      }

      boolean matches(int event) {
         if (this.ids == null) {
            return true;
         }

         for (int id : this.ids) {
            if (id == event) {
               return true;
            }
         }

         return false;
      }
   }
}
