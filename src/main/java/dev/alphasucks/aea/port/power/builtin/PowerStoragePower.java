package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class PowerStoragePower extends PowerType<PowerStoragePower.Config> {
   private static final String KEY = "Powers";
   private static final int MAX_SOURCE_DEPTH = 8;

   @Override
   public MapCodec<PowerStoragePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.INT.optionalFieldOf("slots", 0).forGetter(PowerStoragePower.Config::slots),
               LoggedOptionalField.strict("powers", SingleOrList.of(IdCodecs.ID)).forGetter(PowerStoragePower.Config::powers),
               LoggedOptionalField.of("tags", SingleOrList.of(Codec.STRING), List.of()).forGetter(PowerStoragePower.Config::tags),
               Codec.BOOL.optionalFieldOf("replace_oldest", false).forGetter(PowerStoragePower.Config::replaceOldest),
               Codec.BOOL.optionalFieldOf("drop_on_death", false).forGetter(PowerStoragePower.Config::dropOnDeath),
               Codec.BOOL.optionalFieldOf("grant", true).forGetter(PowerStoragePower.Config::grant),
               Codec.BOOL.optionalFieldOf("own_key", true).forGetter(PowerStoragePower.Config::ownKey)
            )
            .apply(i, PowerStoragePower.Config::new)
      );
   }

   public static List<class_2960> stored(@Nullable PowerContainer holder, class_2960 storageId) {
      if (holder instanceof PowerContainerImpl impl) {
         class_2487 tag = impl.getAuxNbt(storageId);
         if (tag == null) {
            return List.of();
         }

         class_2499 list = tag.method_10554("Powers", 8);
         if (list.isEmpty()) {
            return List.of();
         }

         List<class_2960> out = new ArrayList(list.size());

         for (int i = 0; i < list.size(); i++) {
            class_2960 id = class_2960.method_12829(list.method_10608(i));
            if (id != null) {
               out.add(id);
            }
         }

         return out;
      } else {
         return List.of();
      }
   }

   public static List<class_2960> live(@Nullable PowerContainer holder, class_2960 storageId) {
      List<class_2960> all = stored(holder, storageId);
      if (all.isEmpty()) {
         return all;
      }

      List<class_2960> out = null;

      for (int i = 0; i < all.size(); i++) {
         class_2960 id = (class_2960)all.get(i);
         if (ApoliPowers.get(id) != null) {
            if (out != null) {
               out.add(id);
            }
         } else if (out == null) {
            out = new ArrayList(all.size() - 1);

            for (int j = 0; j < i; j++) {
               out.add((class_2960)all.get(j));
            }
         }
      }

      return out == null ? all : out;
   }

   public static int count(@Nullable PowerContainer holder, class_2960 storageId) {
      return live(holder, storageId).size();
   }

   public static boolean accepts(PowerStoragePower.Config cfg, class_2960 powerId) {
      return refusal(cfg, powerId) == null;
   }

   @Nullable
   private static PowerStoragePower.StoreResult refusal(PowerStoragePower.Config cfg, class_2960 powerId) {
      if (isStorage(powerId)) {
         return PowerStoragePower.StoreResult.IS_STORAGE;
      } else if (cfg.powers().isPresent() && !((List)cfg.powers().get()).contains(powerId)) {
         return PowerStoragePower.StoreResult.NOT_IN_POWERS;
      } else if (cfg.tags().isEmpty()) {
         return null;
      } else {
         return ApoliPowers.hasAnyTag(powerId, cfg.tags()) ? null : PowerStoragePower.StoreResult.NO_MATCHING_TAG;
      }
   }

   public static boolean keyMuted(@Nullable PowerContainer holder, class_2960 powerId) {
      return holder != null && !holder.powersOfType(ApoliIds.POWER_STORAGE).isEmpty() ? muted(holder, powerId, 0) : false;
   }

   private static boolean muted(PowerContainer holder, class_2960 powerId, int depth) {
      if (depth >= 8) {
         return false;
      }

      Set<class_2960> sources = holder.sourcesOf(powerId);
      if (sources.isEmpty()) {
         return false;
      }

      for (class_2960 source : sources) {
         Power power = ApoliPowers.get(source);
         if (power != null && power.config() instanceof PowerStoragePower.Config cfg) {
            if (cfg.ownKey()) {
               return false;
            }
         } else if (!muted(holder, source, depth + 1)) {
            return false;
         }
      }

      return true;
   }

   public static boolean isStorage(class_2960 powerId) {
      Power power = ApoliPowers.get(powerId);
      return power != null && ApoliIds.POWER_STORAGE.equals(power.typeId());
   }

   public static boolean store(@Nullable PowerContainer holder, class_2960 storageId, PowerStoragePower.Config cfg, class_2960 powerId) {
      return attempt(holder, storageId, cfg, powerId) == PowerStoragePower.StoreResult.STORED;
   }

   public static PowerStoragePower.StoreResult attempt(@Nullable PowerContainer holder, class_2960 storageId, PowerStoragePower.Config cfg, class_2960 powerId) {
      if (holder instanceof PowerContainerImpl impl) {
         if (ApoliPowers.get(powerId) == null) {
            return PowerStoragePower.StoreResult.UNKNOWN_POWER;
         }

         PowerStoragePower.StoreResult refused = refusal(cfg, powerId);
         if (refused != null) {
            return refused;
         }

         List<class_2960> current = new ArrayList(stored(impl, storageId));
         current.removeIf(id -> ApoliPowers.get(id) == null);
         if (current.contains(powerId)) {
            return PowerStoragePower.StoreResult.ALREADY_STORED;
         }

         if (cfg.slots() > 0 && current.size() >= cfg.slots()) {
            if (!cfg.replaceOldest()) {
               return PowerStoragePower.StoreResult.FULL;
            }

            current.remove(0);
         }

         current.add(powerId);
         write(impl, storageId, current);
         return PowerStoragePower.StoreResult.STORED;
      } else {
         return PowerStoragePower.StoreResult.NO_CONTAINER;
      }
   }

   public static boolean remove(@Nullable PowerContainer holder, class_2960 storageId, class_2960 powerId) {
      if (holder instanceof PowerContainerImpl impl) {
         List<class_2960> current = new ArrayList(stored(impl, storageId));
         if (!current.remove(powerId)) {
            return false;
         }

         write(impl, storageId, current);
         return true;
      } else {
         return false;
      }
   }

   public static void replace(@Nullable PowerContainer holder, class_2960 storageId, List<class_2960> powers) {
      if (holder instanceof PowerContainerImpl impl) {
         if (powers.isEmpty()) {
            clear(impl, storageId);
         } else {
            write(impl, storageId, powers);
         }
      }
   }

   public static void clear(@Nullable PowerContainer holder, class_2960 storageId) {
      if (holder instanceof PowerContainerImpl impl) {
         if (impl.getAuxNbt(storageId) != null) {
            impl.removeAux(storageId);
            sync(impl, storageId);
         }
      }
   }

   public static void sync(@Nullable PowerContainer holder, class_2960 storageId) {
      if (holder instanceof PowerContainerImpl impl) {
         Power storage = ApoliPowers.get(storageId);
         boolean grant = storage != null && storage.config() instanceof PowerStoragePower.Config cfg && cfg.grant();
         List<class_2960> wanted = grant ? live(impl, storageId) : List.of();

         for (class_2960 held : List.copyOf(impl.allPowers())) {
            if (!held.equals(storageId) && !wanted.contains(held) && impl.sourcesOf(held).contains(storageId)) {
               impl.removePower(held, storageId);
            }
         }

         for (int i = 0; i < wanted.size(); i++) {
            impl.addPower((class_2960)wanted.get(i), storageId);
         }
      }
   }

   public static void reconcile(PowerContainer holder) {
      List<class_2960> storages = storagesOf(holder);

      for (int i = 0; i < storages.size(); i++) {
         sync(holder, (class_2960)storages.get(i));
      }
   }

   private static void write(PowerContainerImpl impl, class_2960 storageId, List<class_2960> powers) {
      class_2487 tag = new class_2487();
      class_2499 list = new class_2499();

      for (int i = 0; i < powers.size(); i++) {
         list.add(class_2519.method_23256(((class_2960)powers.get(i)).toString()));
      }

      tag.method_10566("Powers", list);
      impl.setAuxNbt(storageId, tag);
      impl.markDirty();
      sync(impl, storageId);
   }

   public void tick(class_2960 powerId, PowerStoragePower.Config cfg, PowerContainer holder) {
      if (!cfg.grant()) {
         List<class_2960> powers = stored(holder, powerId);

         for (int i = 0; i < powers.size(); i++) {
            class_2960 id = (class_2960)powers.get(i);
            Power power = ApoliPowers.get(id);
            if (power != null) {
               PowerType<?> type = PowerTypeRegistry.get(power.typeId());
               if (type != null) {
                  invokeTickStored(type, id, power.config(), holder);
               }
            }
         }
      }
   }

   private static void invokeTickStored(PowerType type, class_2960 id, Object cfg, PowerContainer holder) {
      type.tickStored(id, cfg, holder);
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public void onRemoved(class_2960 powerId, PowerStoragePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.hasPower(powerId)) {
         if (holder instanceof PowerContainerImpl impl) {
            impl.removeAux(powerId);
         }
      }
   }

   public OptionalInt readResource(class_2960 powerId, PowerStoragePower.Config cfg, PowerContainer holder) {
      return OptionalInt.of(count(holder, powerId));
   }

   public OptionalInt writeResource(class_2960 powerId, PowerStoragePower.Config cfg, PowerContainer holder, int value) {
      if (holder instanceof PowerContainerImpl impl) {
         List current = live(impl, powerId);
         int target = Math.max(0, Math.min(value, current.size()));
         if (target == 0) {
            clear(impl, powerId);
            return OptionalInt.of(0);
         } else {
            replace(impl, powerId, current.subList(current.size() - target, current.size()));
            return OptionalInt.of(target);
         }
      } else {
         return OptionalInt.empty();
      }
   }

   public OptionalInt resourceBound(class_2960 powerId, PowerStoragePower.Config cfg, PowerContainer holder, boolean max) {
      if (!max) {
         return OptionalInt.of(0);
      } else {
         return cfg.slots() > 0 ? OptionalInt.of(cfg.slots()) : OptionalInt.empty();
      }
   }

   public static List<class_2960> storagesOf(@Nullable PowerContainer holder) {
      return holder == null ? List.of() : holder.powersOfType(ApoliIds.POWER_STORAGE);
   }

   public record Config(
      int slots, Optional<List<class_2960>> powers, List<String> tags, boolean replaceOldest, boolean dropOnDeath, boolean grant, boolean ownKey
   ) {
   }

   public enum StoreResult {
      STORED,
      NO_CONTAINER,
      UNKNOWN_POWER,
      IS_STORAGE,
      NOT_IN_POWERS,
      NO_MATCHING_TAG,
      ALREADY_STORED,
      FULL;
   }
}
