package dev.alphasucks.aea.port.effects;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.effects.CustomEffectNetworking.ClientEffectData;
import dev.alphasucks.aea.port.effects.CustomEffectNetworking.SyncCustomEffectsPacket;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2783;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3300;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime registration of data-driven mob effects into the (frozen) vanilla mob effect registry.
 *
 * <p>AEA: Overgrown's design, with these changes: the client places effects at the raw ids the server sent and
 * verifies them; everything is unregistered when a server stops or a client leaves one; orphaned effect powers
 * are dropped on entity load too; the custom effects of a player are re-sent once their client acknowledged a
 * sync (packets decoded before the client registered the effects were dropped).
 */
public final class CustomEffectRegistry {
   private static final int SYNC_TIMEOUT_TICKS = 100;
   public static volatile boolean reloading = false;
   // AEA: concurrent set; canBeAffected reads it from the client thread too in singleplayer
   public static final Set<UUID> waiting = ConcurrentHashMap.newKeySet();
   public static volatile int timeout;
   private static volatile List<CustomEffect> definitions = List.of();
   private static volatile List<CustomEffect> previousDefinitions = List.of();
   private static volatile List<CustomMobEffect> active = List.of();
   private static volatile Set<class_2960> ids = Set.of();
   private static volatile SyncCustomEffectsPacket payload = new SyncCustomEffectsPacket(List.of());

   private CustomEffectRegistry() {
   }

   public static List<CustomMobEffect> active() {
      return active;
   }

   public static Set<class_2960> ids() {
      return ids;
   }

   public static SyncCustomEffectsPacket payload() {
      return payload;
   }

   public static boolean blocksCustomEffects() {
      return reloading || !waiting.isEmpty();
   }

   public static void stage(class_3300 resourceManager) {
      previousDefinitions = definitions;
      definitions = CustomEffectLoader.load(resourceManager);
      installDefinitions(definitions);
   }

   public static void finishReload(boolean success) {
      if (!success && previousDefinitions != definitions) {
         definitions = previousDefinitions;
         installDefinitions(definitions);
      }

      previousDefinitions = definitions;
      validate();
      reloading = false;
   }

   public static void validate() {
      for (CustomEffect definition : definitions) {
         for (class_2960 power : definition.powers()) {
            if (ApoliPowers.get(power) == null) {
               Apoli.LOGGER.warn("[Apoli] Custom effect {} lists unknown power {}; it will not be granted.", definition.id(), power);
            }
         }
      }
   }

   private static void installDefinitions(List<CustomEffect> source) {
      List<CustomMobEffect> effects = new ArrayList(source.size());

      for (CustomEffect definition : source) {
         effects.add(
            new CustomMobEffect(
               definition.id(), definition.mobEffectCategory(), definition.colorInt(), definition.powers(), definition.name(), definition.icon()
            )
         );
      }

      install(effects);
      payload = SyncCustomEffectsPacket.of(active);
   }

   /** AEA: registry lookup that never trips over an unbound holder. */
   @Nullable
   static class_1291 lookup(class_2960 id) {
      Optional<class_6880.class_6883<class_1291>> ref = class_7923.field_41174.method_40264(class_5321.method_29179(class_7924.field_41208, id));
      return ref.isPresent() && ref.get().method_40227() ? (class_1291)ref.get().comp_349() : null;
   }

   /** Server side (and singleplayer): keeps effects that still exist where they are, retires the rest, appends new ones. */
   public static synchronized void install(List<CustomMobEffect> effects) {
      RuntimeMobEffectRegistry registry = (RuntimeMobEffectRegistry)class_7923.field_41174;
      Map<class_2960, CustomMobEffect> reusable = new HashMap();

      for (CustomMobEffect effect : effects) {
         if (lookup(effect.id) instanceof CustomMobEffect existing && class_7923.field_41174.method_10206(existing) >= 0) {
            reusable.put(effect.id, existing);
         }
      }

      registry.apoli$unregisterCustom(reusable.keySet());
      List<CustomMobEffect> installed = new ArrayList(effects.size());
      Set<class_2960> installedIds = new HashSet();

      for (CustomMobEffect effect : effects) {
         if (!installedIds.add(effect.id)) {
            Apoli.LOGGER.error("[Apoli] Skipped custom effect {}: it is defined more than once.", effect.id);
         } else {
            CustomMobEffect existing = (CustomMobEffect)reusable.get(effect.id);
            if (existing != null) {
               existing.copyFrom(effect);
               installed.add(existing);
            } else if (class_7923.field_41174.method_10250(effect.id)) {
               installedIds.remove(effect.id);
               Apoli.LOGGER.error("[Apoli] Skipped custom effect {}: a status effect with that id already exists.", effect.id);
            } else {
               installed.add(registry.apoli$register(effect));
            }
         }
      }

      active = List.copyOf(installed);
      ids = Set.copyOf(installedIds);
   }

   /**
    * AEA: client side of a remote server. Overgrown registered the payload in id order, which only matched the
    * server while no reload had added an effect that sorts before an existing one; raw ids are what the server
    * puts in effect packets, so the effects are registered in the server's raw-id order and checked afterwards.
    */
   public static synchronized boolean installFromServer(List<ClientEffectData> data) {
      RuntimeMobEffectRegistry registry = (RuntimeMobEffectRegistry)class_7923.field_41174;
      List<ClientEffectData> sorted = new ArrayList(data);
      sorted.sort((a, b) -> Integer.compare(a.rawId(), b.rawId()));
      Set<class_2960> wanted = new HashSet();
      for (ClientEffectData d : sorted) {
         wanted.add(d.id());
      }

      registry.apoli$unregisterCustom(wanted);
      boolean inPlace = true;
      for (ClientEffectData d : sorted) {
         if (!(lookup(d.id()) instanceof CustomMobEffect existing) || class_7923.field_41174.method_10206(existing) != d.rawId()) {
            inPlace = false;
            break;
         }
      }

      List<CustomMobEffect> installed = new ArrayList(sorted.size());
      Set<class_2960> installedIds = new HashSet();
      if (inPlace) {
         for (ClientEffectData d : sorted) {
            CustomMobEffect existing = (CustomMobEffect)lookup(d.id());
            existing.copyFrom(d.toEffect());
            installed.add(existing);
            installedIds.add(d.id());
         }
      } else {
         registry.apoli$unregisterCustom(Set.of());
         for (ClientEffectData d : sorted) {
            if (!installedIds.add(d.id())) {
               continue;
            }
            if (class_7923.field_41174.method_10250(d.id())) {
               installedIds.remove(d.id());
               Apoli.LOGGER.error("[Apoli] Server custom effect {} clashes with a status effect this client already has.", d.id());
               continue;
            }
            installed.add(registry.apoli$register(d.toEffect()));
         }
      }

      active = List.copyOf(installed);
      ids = Set.copyOf(installedIds);
      boolean matches = installed.size() == sorted.size();
      for (int i = 0; matches && i < installed.size(); i++) {
         int local = class_7923.field_41174.method_10206(installed.get(i));
         if (local != sorted.get(i).rawId()) {
            matches = false;
            Apoli.LOGGER.error(
               "[Apoli] Custom effect {} got raw id {} on this client but {} on the server; this client has status effects the server lacks. "
                  + "Custom effect icons and particles will be wrong.",
               new Object[]{sorted.get(i).id(), local, sorted.get(i).rawId()}
            );
         }
      }
      return matches;
   }

   /** AEA: forgets every runtime effect (server stopped, or this client left a remote server). */
   public static synchronized void clear() {
      definitions = List.of();
      previousDefinitions = List.of();
      waiting.clear();
      reloading = false;
      install(List.of());
      payload = new SyncCustomEffectsPacket(List.of());
   }

   public static void purge(MinecraftServer server) {
      if (!active.isEmpty()) {
         for (class_3218 level : server.method_3738()) {
            for (class_1297 entity : level.method_27909()) {
               if (entity instanceof class_1309 living) {
                  purge(living);
               }
            }
         }
      }
   }

   private static void purge(class_1309 living) {
      Map<class_1291, class_1293> effects = living.method_6088();
      if (!effects.isEmpty()) {
         List<class_1291> custom = null;

         for (class_1291 effect : effects.keySet()) {
            if (effect instanceof CustomMobEffect) {
               if (custom == null) {
                  custom = new ArrayList(2);
               }

               custom.add(effect);
            }
         }

         if (custom != null) {
            for (int i = 0; i < custom.size(); i++) {
               living.method_6016((class_1291)custom.get(i));
            }
         }
      }
   }

   public static void dropOrphanedGrants(class_1309 entity) {
      if (entity.method_37908().method_8608()) {
         return;
      }
      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         Set<class_2960> sources = container.allSources();
         List<CustomMobEffect> effects = active;

         for (int i = 0; i < effects.size(); i++) {
            CustomMobEffect effect = (CustomMobEffect)effects.get(i);
            if ((sources.contains(effect.source) || sources.contains(effect.id)) && !entity.method_6059(effect)) {
               effect.revokePowers(container);
            }
         }

         // AEA: effects that no longer exist (removed from the data packs) are not in the active list, but the
         // powers they granted are saved with the entity; drop every apoli:effect/... source no held effect owns.
         for (class_2960 source : sources) {
            if (Apoli.MOD_ID.equals(source.method_12836()) && source.method_12832().startsWith("effect/") && !holdsEffectWithSource(entity, source)) {
               container.removeAllFromSource(source);
            }
         }
      }
   }

   private static boolean holdsEffectWithSource(class_1309 entity, class_2960 source) {
      for (class_1291 effect : entity.method_6088().keySet()) {
         if (effect instanceof CustomMobEffect custom && custom.source.equals(source)) {
            return true;
         }
      }
      return false;
   }

   public static void awaitAck(class_3222 player) {
      waiting.add(player.method_5667());
      timeout = player.field_13995.method_3780();
   }

   public static void acknowledge(class_3222 player, boolean success) {
      waiting.remove(player.method_5667());
      if (!success) {
         Apoli.LOGGER.warn("[Apoli] {} could not apply the server's custom effects.", player.method_7334().getName());
      }

      // AEA: effect packets for custom effects that reached the client before it registered them were dropped
      if (player.field_13987 != null) {
         for (class_1293 instance : List.copyOf(player.method_6026())) {
            if (instance.method_5579() instanceof CustomMobEffect) {
               player.field_13987.method_14364(new class_2783(player.method_5628(), instance));
            }
         }
      }
   }

   public static void tick(MinecraftServer server) {
      if (!waiting.isEmpty() && server.method_3780() > timeout + SYNC_TIMEOUT_TICKS) {
         List<String> names = new ArrayList(waiting.size());

         for (UUID uuid : waiting) {
            class_3222 player = server.method_3760().method_14602(uuid);
            names.add(player != null ? player.method_7334().getName() : uuid.toString());
         }

         Apoli.LOGGER.warn("[Apoli] Custom effect sync timed out for: {}", String.join(", ", names));
         waiting.clear();
      }
   }
}
