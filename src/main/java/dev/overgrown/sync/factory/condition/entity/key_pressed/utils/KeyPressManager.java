package dev.overgrown.sync.factory.condition.entity.key_pressed.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_156;
import net.minecraft.server.MinecraftServer;

public class KeyPressManager {
   private static final Map<UUID, Map<String, KeyPressManager.KeyState>> PLAYER_KEY_STATES = new ConcurrentHashMap();
   private static final Map<UUID, Map<String, Boolean>> PREVIOUS_KEY_STATES = new ConcurrentHashMap();
   private static final Map<UUID, List<String>> KEY_PRESS_EVENTS = new ConcurrentHashMap();

   public static final int MAX_KEY_LENGTH = 256;
   private static final int MAX_KEYS_PER_PLAYER = 2048;
   private static final int MAX_PRESS_EVENTS_PER_TICK = 64;

   public static void updateKeyState(UUID playerUuid, String key, boolean pressed) {
      // key names come straight from the client packet: ignore absurd ones instead of storing them
      if (key == null || key.isEmpty() || key.length() > MAX_KEY_LENGTH) {
         return;
      }

      Map<String, KeyPressManager.KeyState> playerKeys = PLAYER_KEY_STATES.computeIfAbsent(playerUuid, k -> new HashMap<>());
      // and don't let the client grow the per-player map without bound
      if (playerKeys.size() >= MAX_KEYS_PER_PLAYER && !playerKeys.containsKey(key)) {
         return;
      }

      playerKeys.compute(key, (k, state) -> {
         if (state == null) {
            return new KeyPressManager.KeyState(pressed, class_156.method_658());
         }

         state.current = pressed;
         state.lastUpdateTime = class_156.method_658();
         return state;
      });
      if (pressed) {
         List<String> events = KEY_PRESS_EVENTS.computeIfAbsent(playerUuid, k -> new CopyOnWriteArrayList<>());
         if (events.size() < MAX_PRESS_EVENTS_PER_TICK) {
            events.add(key);
         }
      }
   }

   public static boolean getKeyState(UUID playerUuid, String key, boolean continuous) {
      Map<String, KeyPressManager.KeyState> playerKeys = (Map<String, KeyPressManager.KeyState>)PLAYER_KEY_STATES.get(playerUuid);
      if (playerKeys == null) {
         return false;
      }

      KeyPressManager.KeyState state = (KeyPressManager.KeyState)playerKeys.get(key);
      if (state == null) {
         return false;
      }

      if (continuous) {
         return state.current;
      }

      // read-only: serverTick() moves the current states into PREVIOUS_KEY_STATES once per tick, so every
      // evaluation during the tick sees the same press
      Map<String, Boolean> previousStates = (Map<String, Boolean>)PREVIOUS_KEY_STATES.get(playerUuid);
      boolean wasPressed = previousStates != null && (Boolean)previousStates.getOrDefault(key, false);
      boolean isPressed = state.current;
      return isPressed && !wasPressed;
   }

   public static List<String> getKeyPressEvents(UUID playerUuid) {
      return (List<String>)KEY_PRESS_EVENTS.getOrDefault(playerUuid, Collections.emptyList());
   }

   public static void removePlayer(UUID playerUuid) {
      PLAYER_KEY_STATES.remove(playerUuid);
      PREVIOUS_KEY_STATES.remove(playerUuid);
      KEY_PRESS_EVENTS.remove(playerUuid);
   }

   public static void serverTick(MinecraftServer server) {
      server.method_3760().method_14571().forEach(player -> {
         UUID uuid = player.method_5667();
         Map<String, Boolean> previousStates = (Map<String, Boolean>)PREVIOUS_KEY_STATES.computeIfAbsent(uuid, k -> new HashMap());
         Map<String, KeyPressManager.KeyState> currentStates = (Map<String, KeyPressManager.KeyState>)PLAYER_KEY_STATES.get(uuid);
         if (currentStates != null) {
            currentStates.forEach((key, state) -> previousStates.put(key, state.current));
         }
      });
      KEY_PRESS_EVENTS.clear();
   }

   private static class KeyState {
      boolean current;
      long lastUpdateTime;

      KeyState(boolean current, long lastUpdateTime) {
         this.current = current;
         this.lastUpdateTime = lastUpdateTime;
      }
   }
}
