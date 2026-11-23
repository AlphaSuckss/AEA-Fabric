package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower.ClientView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientEntitySets {
   public static final ClientView VIEW = new ClientView() {
      @Override
      public boolean contains(class_1297 owner, class_2960 powerId, class_1297 member) {
         ClientEntitySets.Members members = ClientEntitySets.lookup(owner, powerId);
         return members != null && members.set().contains(member.method_5667());
      }

      @Override
      public int size(class_1297 owner, class_2960 powerId) {
         ClientEntitySets.Members members = ClientEntitySets.lookup(owner, powerId);
         return members == null ? 0 : members.list().size();
      }
   };
   private static volatile Map<class_2960, ClientEntitySets.Members> own = Map.of();
   private static volatile Map<UUID, Map<class_2960, ClientEntitySets.Members>> tracked = Map.of();
   private static final Set<class_2960> WARNED = new HashSet();

   private ClientEntitySets() {
   }

   public static synchronized void apply(class_2960 powerId, List<UUID> members) {
      own = with(own, powerId, members);
   }

   public static synchronized void applyTracked(UUID owner, class_2960 powerId, List<UUID> members) {
      Map<class_2960, ClientEntitySets.Members> sets = with((Map<class_2960, ClientEntitySets.Members>)tracked.getOrDefault(owner, Map.of()), powerId, members);
      Map<UUID, Map<class_2960, ClientEntitySets.Members>> next = new HashMap(tracked);
      if (sets.isEmpty()) {
         next.remove(owner);
      } else {
         next.put(owner, sets);
      }

      tracked = Map.copyOf(next);
   }

   private static Map<class_2960, ClientEntitySets.Members> with(Map<class_2960, ClientEntitySets.Members> sets, class_2960 powerId, List<UUID> members) {
      Map<class_2960, ClientEntitySets.Members> next = new HashMap(sets);
      if (members.isEmpty()) {
         next.remove(powerId);
      } else {
         next.put(powerId, ClientEntitySets.Members.of(members));
      }

      return Map.copyOf(next);
   }

   public static List<UUID> members(class_2960 powerId) {
      ClientEntitySets.Members members = (ClientEntitySets.Members)own.get(powerId);
      return members == null ? List.of() : members.list();
   }

   @Nullable
   private static ClientEntitySets.Members lookup(class_1297 owner, class_2960 powerId) {
      if (owner == class_310.method_1551().field_1724) {
         return (ClientEntitySets.Members)own.get(powerId);
      }

      Map<class_2960, ClientEntitySets.Members> sets = (Map<class_2960, ClientEntitySets.Members>)tracked.get(owner.method_5667());
      return sets == null ? null : (ClientEntitySets.Members)sets.get(powerId);
   }

   public static synchronized void clear() {
      own = Map.of();
      tracked = Map.of();
      WARNED.clear();
   }

   public static synchronized void warnIfUnknown(class_2960 setId) {
      if (WARNED.add(setId)) {
         Power power = ApoliPowers.get(setId);
         if (power == null) {
            Apoli.LOGGER
               .warn(
                  "[Apoli] An apoli:overlay names \"set\": \"{}\", but no power with that id is loaded. The id must be the apoli:entity_set power itself — remember '*:*' expands to the id of the file it is written in, so '*:*_set' in powers/hud.json means '<namespace>:hud_set'.",
                  setId
               );
         } else if (!(PowerTypeRegistry.get(power.typeId()) instanceof EntitySetPower)) {
            Apoli.LOGGER.warn("[Apoli] An apoli:overlay names \"set\": \"{}\", but that power is {}, not apoli:entity_set.", setId, power.typeId());
         } else {
            Apoli.LOGGER
               .info(
                  "[Apoli] apoli:overlay set {} is currently empty on this client. If the set has members server-side, check that the set is owned by *you* — overlays only read the viewing player's own sets.",
                  setId
               );
         }
      }
   }

   private record Members(List<UUID> list, Set<UUID> set) {
      static ClientEntitySets.Members of(List<UUID> members) {
         List<UUID> list = List.copyOf(members);
         return new ClientEntitySets.Members(list, Set.copyOf(list));
      }
   }
}
