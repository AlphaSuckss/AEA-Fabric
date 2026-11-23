package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.entity.OfflinePlayers;
import dev.alphasucks.aea.port.network.payload.SyncEntitySetsS2C;
import dev.alphasucks.aea.port.network.payload.SyncTrackedEntitySetS2C;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2487;
import net.minecraft.class_2509;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4844;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class EntitySetPower extends PowerType<EntitySetPower.Cfg> {
   private static final Codec<List<EntitySetPower.SavedSet>> SAVED_SETS = RecordCodecBuilder.<EntitySetPower.SavedSet>create(
         i -> i.group(
               class_4844.field_25122.fieldOf("owner").forGetter(EntitySetPower.SavedSet::owner),
               class_2960.field_25139.fieldOf("set").forGetter(EntitySetPower.SavedSet::set),
               class_4844.field_25122.listOf().fieldOf("members").forGetter(EntitySetPower.SavedSet::members),
               Codec.unboundedMap(class_4844.field_41525, Codec.LONG).optionalFieldOf("expire_at", Map.of()).forGetter(EntitySetPower.SavedSet::expireAt)
            )
            .apply(i, EntitySetPower.SavedSet::new)
      )
      .listOf();
   private static final Map<UUID, Map<class_2960, EntitySetPower.SetState>> BY_OWNER = new HashMap();
   private static final Map<UUID, Set<EntitySetPower.SetState>> MEMBERSHIPS = new HashMap();
   private static final Set<EntitySetPower.SetState> DIRTY = new LinkedHashSet();
   private static final Map<UUID, Boolean> PENDING_REMOVAL = new LinkedHashMap();
   @Nullable
   private static EntitySetPower.ClientView clientView;
   @Nullable
   private static Runnable persistentChanged;

   public static void setClientView(EntitySetPower.ClientView view) {
      clientView = view;
   }

   @Override
   public MapCodec<EntitySetPower.Cfg> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("action_on_add", BiEntityAction.CODEC).forGetter(EntitySetPower.Cfg::actionOnAdd),
               LoggedOptionalField.of("action_on_remove", BiEntityAction.CODEC).forGetter(EntitySetPower.Cfg::actionOnRemove),
               Codec.BOOL.optionalFieldOf("persistent", false).forGetter(EntitySetPower.Cfg::persistent)
            )
            .apply(i, EntitySetPower.Cfg::new)
      );
   }

   public void tick(class_2960 powerId, EntitySetPower.Cfg cfg, PowerContainer holder) {
      class_1309 owner = holder.owner();
      if (owner != null && owner.method_37908() instanceof class_3218 level) {
         EntitySetPower.SetState state = state(owner.method_5667(), powerId);
         if (state != null && state.expireAt != null && !state.expireAt.isEmpty()) {
            long now = level.method_8510();
            Iterator<Entry<UUID, Long>> it = state.expireAt.entrySet().iterator();

            while (it.hasNext()) {
               Entry<UUID, Long> e = (Entry<UUID, Long>)it.next();
               if (now >= (Long)e.getValue()) {
                  UUID uuid = (UUID)e.getKey();
                  it.remove();
                  if (state.members.remove(uuid)) {
                     unlink(uuid, state);
                     DIRTY.add(state);
                     changed(state);
                     runBi(cfg.actionOnRemove, owner, uuid, level);
                  }
               }
            }

            dropIfEmpty(state);
         }
      }
   }

   public void onRemoved(class_2960 powerId, EntitySetPower.Cfg cfg, PowerContainer holder, class_2960 source) {
      class_1309 owner = holder.owner();
      if (owner != null && owner.method_37908() instanceof class_3218 level) {
         EntitySetPower.SetState state = state(owner.method_5667(), powerId);
         if (state != null) {
            List<UUID> members = List.copyOf(state.members);
            changed(state);
            drop(state);
            DIRTY.add(state);
            if (!cfg.actionOnRemove.isEmpty()) {
               for (UUID uuid : members) {
                  runBi(cfg.actionOnRemove, owner, uuid, level);
               }
            }
         }
      }
   }

   @Nullable
   public static EntitySetPower.Cfg resolveCfg(class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return null;
      } else {
         return loaded.config() instanceof EntitySetPower.Cfg c ? c : null;
      }
   }

   public static boolean isPersistent(class_2960 powerId) {
      EntitySetPower.Cfg cfg = resolveCfg(powerId);
      return cfg != null && cfg.persistent;
   }

   public static boolean add(class_1297 owner, class_2960 powerId, EntitySetPower.Cfg cfg, class_1297 target, OptionalInt timeLimit) {
      if (target.method_31481()) {
         return false;
      } else if (!(owner.method_37908() instanceof class_3218 level)) {
         return false;
      } else {
         EntitySetPower.SetState var9 = stateOrCreate(owner.method_5667(), powerId);
         UUID uuid = target.method_5667();
         boolean firstTime = var9.members.add(uuid);
         if (timeLimit.isPresent()) {
            var9.expiry().put(uuid, level.method_8510() + timeLimit.getAsInt());
         }

         if (firstTime) {
            link(uuid, var9);
            DIRTY.add(var9);
         }

         if (firstTime || timeLimit.isPresent()) {
            changed(var9);
         }

         if (firstTime) {
            cfg.actionOnAdd.ifPresent(a -> a.run(new BiEntityCtx(owner, target, level)));
         }

         return firstTime;
      }
   }

   public static boolean remove(class_1297 owner, class_2960 powerId, EntitySetPower.Cfg cfg, class_1297 target) {
      if (owner.method_37908() instanceof class_3218 level) {
         EntitySetPower.SetState state = state(owner.method_5667(), powerId);
         if (state == null) {
            return false;
         }

         UUID uuid = target.method_5667();
         boolean removed = state.members.remove(uuid);
         boolean timed = state.expireAt != null && state.expireAt.remove(uuid) != null;
         if (removed) {
            unlink(uuid, state);
            DIRTY.add(state);
         }

         if (removed || timed) {
            changed(state);
         }

         dropIfEmpty(state);
         if (removed) {
            cfg.actionOnRemove.ifPresent(a -> a.run(new BiEntityCtx(owner, target, level)));
         }

         return removed;
      } else {
         return false;
      }
   }

   public static boolean contains(class_1297 owner, class_2960 powerId, class_1297 target) {
      if ((target.method_31481() || !target.method_5805()) && !isPersistent(powerId)) {
         return false;
      } else if (owner.method_37908().method_8608()) {
         EntitySetPower.ClientView view = clientView;
         return view != null && view.contains(owner, powerId, target);
      } else {
         EntitySetPower.SetState state = state(owner.method_5667(), powerId);
         return state != null && state.members.contains(target.method_5667());
      }
   }

   public static int size(class_1297 owner, class_2960 powerId) {
      if (owner.method_37908().method_8608()) {
         EntitySetPower.ClientView view = clientView;
         return view == null ? 0 : view.size(owner, powerId);
      } else {
         EntitySetPower.SetState state = state(owner.method_5667(), powerId);
         return state == null ? 0 : state.members.size();
      }
   }

   public static List<UUID> iterationOrder(class_1297 owner, class_2960 powerId, boolean reverse) {
      EntitySetPower.SetState state = state(owner.method_5667(), powerId);
      if (state != null && !state.members.isEmpty()) {
         List<UUID> list = new ArrayList(state.members);
         if (reverse) {
            Collections.reverse(list);
         }

         return list;
      } else {
         return List.of();
      }
   }

   public static List<UUID> ownersContaining(class_1297 member, class_2960 powerId, boolean reverse) {
      Set<EntitySetPower.SetState> states = (Set<EntitySetPower.SetState>)MEMBERSHIPS.get(member.method_5667());
      if (states != null && !states.isEmpty()) {
         List<UUID> owners = null;

         for (EntitySetPower.SetState state : states) {
            if (state.powerId.equals(powerId)) {
               if (owners == null) {
                  owners = new ArrayList(2);
               }

               owners.add(state.owner);
            }
         }

         if (owners == null) {
            return List.of();
         }

         if (reverse) {
            Collections.reverse(owners);
         }

         return owners;
      } else {
         return List.of();
      }
   }

   public static int membershipCount(class_1297 member, @Nullable class_2960 powerId) {
      Set<EntitySetPower.SetState> states = (Set<EntitySetPower.SetState>)MEMBERSHIPS.get(member.method_5667());
      if (states == null) {
         return 0;
      }

      if (powerId == null) {
         return states.size();
      }

      int count = 0;

      for (EntitySetPower.SetState state : states) {
         if (state.powerId.equals(powerId)) {
            count++;
         }
      }

      return count;
   }

   public static void flushSync(MinecraftServer server) {
      if (!DIRTY.isEmpty()) {
         for (EntitySetPower.SetState state : DIRTY) {
            List<UUID> members = state.members.isEmpty() ? List.of() : List.copyOf(state.members);
            List<UUID> previous = state.lastSent;
            if (previous == null ? !members.isEmpty() : !previous.equals(members)) {
               state.lastSent = members.isEmpty() ? null : members;
               broadcast(server, state, members);
            }
         }

         DIRTY.clear();
      }
   }

   private static void broadcast(MinecraftServer server, EntitySetPower.SetState state, List<UUID> members) {
      class_3222 ownerPlayer = server.method_3760().method_14602(state.owner);
      if (ownerPlayer != null) {
         ApoliNetwork.sendEntitySets(ownerPlayer, new SyncEntitySetsS2C(state.powerId, members));
      }

      class_1297 owner = ownerPlayer != null ? ownerPlayer : resolveEntity(server, state.owner);
      if (owner != null) {
         ApoliNetwork.sendEntitySetToTrackers(owner, new SyncTrackedEntitySetS2C(state.owner, state.powerId, members));
      }
   }

   public static void syncAllTo(class_3222 player) {
      Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.get(player.method_5667());
      if (owned != null) {
         for (EntitySetPower.SetState state : owned.values()) {
            List<UUID> members = List.copyOf(state.members);
            ApoliNetwork.sendEntitySets(player, new SyncEntitySetsS2C(state.powerId, members));
            state.lastSent = members.isEmpty() ? null : members;
         }
      }
   }

   public static void syncTracking(class_1297 owner, class_3222 viewer, boolean tracking) {
      if (!BY_OWNER.isEmpty() && owner != viewer) {
         Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.get(owner.method_5667());
         if (owned != null) {
            for (EntitySetPower.SetState state : owned.values()) {
               if (!state.members.isEmpty()) {
                  List<UUID> members = tracking ? List.copyOf(state.members) : List.of();
                  ApoliNetwork.sendTrackedEntitySet(viewer, new SyncTrackedEntitySetS2C(state.owner, state.powerId, members));
               }
            }
         }
      }
   }

   @Nullable
   public static class_1297 resolveEntity(MinecraftServer server, UUID uuid) {
      if (server == null) {
         return null;
      }

      for (class_3218 level : server.method_3738()) {
         class_1297 e = level.method_14190(uuid);
         if (e != null && !e.method_31481()) {
            return e;
         }
      }

      return null;
   }

   @Nullable
   public static class_1297 resolveEntity(MinecraftServer server, UUID uuid, boolean includeOffline) {
      class_1297 live = resolveEntity(server, uuid);
      return live == null && includeOffline && server != null ? OfflinePlayers.get(server, uuid) : live;
   }

   public static void onEntityGone(class_1297 entity) {
      PENDING_REMOVAL.put(entity.method_5667(), entity instanceof class_1657);
   }

   public static void onEntityConverted(UUID oldUuid, UUID newUuid) {
      if (!oldUuid.equals(newUuid)) {
         PENDING_REMOVAL.remove(oldUuid);
         Set<EntitySetPower.SetState> states = (Set<EntitySetPower.SetState>)MEMBERSHIPS.remove(oldUuid);
         if (states != null) {
            for (EntitySetPower.SetState state : states) {
               if (state.members.remove(oldUuid)) {
                  state.members.add(newUuid);
                  DIRTY.add(state);
               }

               if (state.expireAt != null) {
                  Long expiry = (Long)state.expireAt.remove(oldUuid);
                  if (expiry != null) {
                     state.expireAt.put(newUuid, expiry);
                  }
               }

               link(newUuid, state);
               changed(state);
            }
         }

         Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.remove(oldUuid);
         if (owned != null) {
            Map<class_2960, EntitySetPower.SetState> target = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.computeIfAbsent(newUuid, k -> new HashMap(4));

            for (EntitySetPower.SetState state : owned.values()) {
               state.owner = newUuid;
               target.putIfAbsent(state.powerId, state);
               DIRTY.add(state);
               changed(state);
            }
         }
      }
   }

   public static void flushPendingRemovals() {
      if (!PENDING_REMOVAL.isEmpty()) {
         for (Entry<UUID, Boolean> gone : PENDING_REMOVAL.entrySet()) {
            UUID uuid = (UUID)gone.getKey();
            Set<EntitySetPower.SetState> states = (Set<EntitySetPower.SetState>)MEMBERSHIPS.get(uuid);
            if (states != null) {
               for (EntitySetPower.SetState state : List.copyOf(states)) {
                  if (!state.persistent()) {
                     if (state.members.remove(uuid)) {
                        DIRTY.add(state);
                     }

                     if (state.expireAt != null) {
                        state.expireAt.remove(uuid);
                     }

                     unlink(uuid, state);
                     dropIfEmpty(state);
                  }
               }
            }

            if (!(Boolean)gone.getValue()) {
               dropOwner(uuid);
            }
         }

         PENDING_REMOVAL.clear();
      }
   }

   public static void attachStorage(@Nullable Runnable onChange) {
      persistentChanged = onChange;
   }

   public static class_2487 savePersistent(class_2487 tag) {
      List<EntitySetPower.SavedSet> saved = new ArrayList();

      for (Map<class_2960, EntitySetPower.SetState> owned : BY_OWNER.values()) {
         for (EntitySetPower.SetState state : owned.values()) {
            if (!state.members.isEmpty() && state.persistent()) {
               saved.add(
                  new EntitySetPower.SavedSet(
                     state.owner, state.powerId, List.copyOf(state.members), state.expireAt == null ? Map.of() : Map.copyOf(state.expireAt)
                  )
               );
            }
         }
      }

      SAVED_SETS.encodeStart(class_2509.field_11560, saved)
         .resultOrPartial(error -> Apoli.LOGGER.error("[Apoli] Could not save persistent entity sets: {}", error))
         .ifPresent(sets -> tag.method_10566("sets", sets));
      return tag;
   }

   public static void loadPersistent(class_2487 tag) {
      class_2520 sets = tag.method_10580("sets");
      if (sets != null) {
         SAVED_SETS.parse(class_2509.field_11560, sets)
            .resultOrPartial(error -> Apoli.LOGGER.error("[Apoli] Could not load persistent entity sets: {}", error))
            .ifPresent(list -> {
               for (EntitySetPower.SavedSet saved : list) {
                  restore(saved);
               }
            });
      }
   }

   private static void restore(EntitySetPower.SavedSet saved) {
      if (!saved.members.isEmpty()) {
         EntitySetPower.SetState state = stateOrCreate(saved.owner, saved.set);
         state.loadedPersistent = true;

         for (UUID member : saved.members) {
            if (state.members.add(member)) {
               link(member, state);
            }
         }

         for (Entry<UUID, Long> expiry : saved.expireAt.entrySet()) {
            if (state.members.contains(expiry.getKey())) {
               state.expiry().put((UUID)expiry.getKey(), (Long)expiry.getValue());
            }
         }
      }
   }

   public static void reset() {
      BY_OWNER.clear();
      MEMBERSHIPS.clear();
      DIRTY.clear();
      PENDING_REMOVAL.clear();
      persistentChanged = null;
   }

   @Nullable
   private static EntitySetPower.SetState state(UUID owner, class_2960 powerId) {
      Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.get(owner);
      return owned == null ? null : (EntitySetPower.SetState)owned.get(powerId);
   }

   private static EntitySetPower.SetState stateOrCreate(UUID owner, class_2960 powerId) {
      Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.computeIfAbsent(owner, k -> new HashMap(4));
      EntitySetPower.SetState state = (EntitySetPower.SetState)owned.get(powerId);
      if (state == null) {
         state = new EntitySetPower.SetState(owner, powerId);
         owned.put(powerId, state);
      }

      return state;
   }

   private static void dropIfEmpty(EntitySetPower.SetState state) {
      if (state.members.isEmpty()) {
         drop(state);
      }
   }

   private static void drop(EntitySetPower.SetState state) {
      Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.get(state.owner);
      if (owned != null && owned.get(state.powerId) == state) {
         owned.remove(state.powerId);
         if (owned.isEmpty()) {
            BY_OWNER.remove(state.owner);
         }
      }

      for (UUID member : state.members) {
         unlink(member, state);
      }

      state.members.clear();
      state.expireAt = null;
   }

   private static void dropOwner(UUID owner) {
      Map<class_2960, EntitySetPower.SetState> owned = (Map<class_2960, EntitySetPower.SetState>)BY_OWNER.get(owner);
      if (owned != null) {
         for (EntitySetPower.SetState state : List.copyOf(owned.values())) {
            changed(state);
            drop(state);
         }
      }
   }

   private static void changed(EntitySetPower.SetState state) {
      Runnable onChange = persistentChanged;
      if (onChange != null && state.persistent()) {
         onChange.run();
      }
   }

   private static void link(UUID member, EntitySetPower.SetState state) {
      ((Set)MEMBERSHIPS.computeIfAbsent(member, k -> new LinkedHashSet(2))).add(state);
   }

   private static void unlink(UUID member, EntitySetPower.SetState state) {
      Set<EntitySetPower.SetState> states = (Set<EntitySetPower.SetState>)MEMBERSHIPS.get(member);
      if (states != null) {
         states.remove(state);
         if (states.isEmpty()) {
            MEMBERSHIPS.remove(member);
         }
      }
   }

   private static void runBi(Optional<BiEntityAction> action, class_1309 owner, UUID targetUuid, class_3218 level) {
      if (!action.isEmpty()) {
         class_1297 target = resolveEntity(level.method_8503(), targetUuid);
         if (target != null) {
            ((BiEntityAction)action.get()).run(new BiEntityCtx(owner, target, level));
         }
      }
   }

   public record Cfg(Optional<BiEntityAction> actionOnAdd, Optional<BiEntityAction> actionOnRemove, boolean persistent) {
   }

   public interface ClientView {
      boolean contains(class_1297 var1, class_2960 var2, class_1297 var3);

      int size(class_1297 var1, class_2960 var2);
   }

   private record SavedSet(UUID owner, class_2960 set, List<UUID> members, Map<UUID, Long> expireAt) {
   }

   private static final class SetState {
      UUID owner;
      final class_2960 powerId;
      final LinkedHashSet<UUID> members = new LinkedHashSet();
      @Nullable
      Map<UUID, Long> expireAt;
      @Nullable
      List<UUID> lastSent;
      boolean loadedPersistent;

      SetState(UUID owner, class_2960 powerId) {
         this.owner = owner;
         this.powerId = powerId;
      }

      Map<UUID, Long> expiry() {
         Map<UUID, Long> map = this.expireAt;
         if (map == null) {
            map = new HashMap(4);
            this.expireAt = map;
         }

         return map;
      }

      boolean persistent() {
         EntitySetPower.Cfg cfg = EntitySetPower.resolveCfg(this.powerId);
         return cfg != null ? cfg.persistent : this.loadedPersistent;
      }
   }
}
