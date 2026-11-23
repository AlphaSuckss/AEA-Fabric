package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2960;

public final class MultiplePower extends PowerType<MultiplePower.Cfg> {
   public static final Set<String> RESERVED_FIELDS = Set.of(
      new String[]{"type", "loading_priority", "name", "description", "hidden", "condition", "tags", "sub_powers", "skill", "load_condition", "badges"}
   );

   @Override
   public MapCodec<MultiplePower.Cfg> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.listOf().optionalFieldOf("sub_powers", List.of()).forGetter(MultiplePower.Cfg::subPowerIds)).apply(i, MultiplePower.Cfg::new)
      );
   }

   public void onAdded(class_2960 powerId, MultiplePower.Cfg cfg, PowerContainer holder, class_2960 source) {
      for (class_2960 subId : cfg.subPowerIds) {
         holder.addPower(subId, powerId);
      }
   }

   public void onRemoved(class_2960 powerId, MultiplePower.Cfg cfg, PowerContainer holder, class_2960 source) {
      for (class_2960 subId : cfg.subPowerIds) {
         holder.removePower(subId, powerId);
      }
   }

   public void tickStored(class_2960 powerId, MultiplePower.Cfg cfg, PowerContainer holder) {
      List<class_2960> subs = cfg.subPowerIds;

      for (int i = 0; i < subs.size(); i++) {
         class_2960 subId = (class_2960)subs.get(i);
         Power sub = ApoliPowers.get(subId);
         if (sub != null) {
            PowerType<?> type = PowerTypeRegistry.get(sub.typeId());
            if (type != null && !(type instanceof MultiplePower)) {
               invokeTickStored(type, subId, sub.config(), holder);
            }
         }
      }
   }

   private static void invokeTickStored(PowerType type, class_2960 id, Object cfg, PowerContainer holder) {
      type.tickStored(id, cfg, holder);
   }

   public static void reconcile(PowerContainer holder) {
      Set<class_2960> current = holder.allPowers();
      if (!current.isEmpty()) {
         List<class_2960> held = new ArrayList(current);

         for (int i = 0; i < held.size(); i++) {
            class_2960 superId = (class_2960)held.get(i);
            Power power = ApoliPowers.get(superId);
            if (power != null && power.config() instanceof MultiplePower.Cfg cfg) {
               List<class_2960> wanted = cfg.subPowerIds();

               for (int j = 0; j < held.size(); j++) {
                  class_2960 subId = (class_2960)held.get(j);
                  if (!subId.equals(superId) && !wanted.contains(subId) && holder.sourcesOf(subId).contains(superId)) {
                     holder.removePower(subId, superId);
                  }
               }

               for (int j = 0; j < wanted.size(); j++) {
                  holder.addPower((class_2960)wanted.get(j), superId);
               }
            }
         }
      }
   }

   public record Cfg(List<class_2960> subPowerIds) {
   }
}
