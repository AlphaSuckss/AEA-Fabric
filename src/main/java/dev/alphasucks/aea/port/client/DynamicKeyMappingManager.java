package dev.alphasucks.aea.port.client;

import com.mojang.logging.LogUtils;
import dev.alphasucks.aea.port.keybind.Keybind;
import dev.alphasucks.aea.port.mixin.keybinding.KeyMappingCategoryAccessor;
import dev.alphasucks.aea.port.mixin.keybinding.OptionsAccessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3675;
import net.minecraft.class_3675.class_306;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public final class DynamicKeyMappingManager {
   private static final Logger LOG = LogUtils.getLogger();
   private static final String KEY_PREFIX = "key_";
   private static final List<class_304> SESSION_BINDINGS = new ArrayList();
   private static final Map<String, class_2561> NAME_HINTS = new HashMap();
   private static volatile boolean hasNameHints;
   private static boolean resolvingHint;

   private DynamicKeyMappingManager() {
   }

   public static synchronized void applyKeybinds(List<Keybind> definitions) {
      unregisterAll();
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1690 != null) {
         Map<String, String> saved = savedKeyLines(new File(mc.field_1697, "options.txt"));

         for (Keybind def : definitions) {
            String translationKey = def.translationKey();
            def.name().ifPresent(component -> {
               NAME_HINTS.put(translationKey, component);
               hasNameHints = true;
            });
            if (isAlreadyRegistered(translationKey, mc.field_1690.field_1839)) {
               LOG.debug("[Apoli] '{}' already present in Options.keyMappings — skipping.", translationKey);
            } else {
               class_306 defaultKey = parseKey(def.key(), def.id().toString());
               ensureCategoryExists(def.category());
               class_304 km = new class_304(translationKey, defaultKey.method_1442(), defaultKey.method_1444(), def.category());
               String binding = (String)saved.get("key_" + translationKey);
               if (binding != null) {
                  restoreSavedBinding(km, binding);
               }

               class_304[] current = mc.field_1690.field_1839;
               class_304[] extended = (class_304[])Arrays.copyOf(current, current.length + 1);
               extended[current.length] = km;
               ((OptionsAccessor)mc.field_1690).apoli$setKeyMappings(extended);
               SESSION_BINDINGS.add(km);
               LOG.debug("[Apoli] Registered keybind '{}' (default {}, category {}).", new Object[]{translationKey, def.key(), def.category()});
            }
         }

         class_304.method_1426();
         ApoliKeyCache.refresh(); // AEA: Apoli's active-power key cache still points at the previous objects
         LOG.info("[Apoli] {} dynamic keybind(s) active for this session.", SESSION_BINDINGS.size());
      } else {
         LOG.warn("[Apoli] Options not yet initialized; skipping dynamic keybind registration.");
      }
   }

   public static synchronized void unregisterAll() {
      if (SESSION_BINDINGS.isEmpty()) {
         NAME_HINTS.clear();
         hasNameHints = false;
      } else {
         class_310 mc = class_310.method_1551();
         if (mc != null) {
            class_315 options = mc.field_1690;
            if (options != null) {
               Set<class_304> remove = Collections.newSetFromMap(new IdentityHashMap());
               remove.addAll(SESSION_BINDINGS);
               class_304[] filtered = (class_304[])Arrays.stream(options.field_1839).filter(km -> !remove.contains(km)).toArray(class_304[]::new);
               ((OptionsAccessor)options).apoli$setKeyMappings(filtered);
               class_304.method_1426();
               ApoliKeyCache.refresh();
            }
         }

         SESSION_BINDINGS.clear();
         NAME_HINTS.clear();
         hasNameHints = false;
      }
   }

   public static boolean hasNameHints() {
      return hasNameHints;
   }

   @Nullable
   public static String nameHint(String translationKey) {
      if (hasNameHints && !resolvingHint) {
         class_2561 component = (class_2561)NAME_HINTS.get(translationKey);
         if (component == null) {
            return null;
         }

         resolvingHint = true;

         try {
            return component.getString();
         } finally {
            resolvingHint = false;
         }
      } else {
         return null;
      }
   }

   private static boolean isAlreadyRegistered(String translationKey, class_304[] all) {
      for (class_304 km : all) {
         if (translationKey.equals(km.method_1431())) {
            return true;
         }
      }

      return false;
   }

   private static class_306 parseKey(String name, String owner) {
      try {
         return class_3675.method_15981(name);
      } catch (Exception e) {
         LOG.warn("[Apoli] Could not parse key '{}' for '{}': {}", new Object[]{name, owner, e.getMessage()});
         return class_3675.field_16237;
      }
   }

   private static void ensureCategoryExists(String category) {
      Map<String, Integer> map = KeyMappingCategoryAccessor.apoli$getCategorySortOrder();
      if (!map.containsKey(category)) {
         int next = (Integer)map.values().stream().max(Integer::compareTo).orElse(0) + 1;
         map.put(category, next);
         LOG.debug("[Apoli] Registered new controls category '{}'.", category);
      }
   }

   private static void restoreSavedBinding(class_304 km, String savedKey) {
      try {
         km.method_1422(class_3675.method_15981(savedKey.trim()));
      } catch (Exception ex) {
         LOG.warn("[Apoli] Ignoring invalid saved binding '{}' for '{}': {}", new Object[]{savedKey, km.method_1431(), ex.getMessage()});
      }
   }

   public static Map<String, String> retainedKeyLines(File optionsFile, class_304[] registered) {
      Map<String, String> lines = savedKeyLines(optionsFile);
      if (lines.isEmpty()) {
         return lines;
      }

      for (class_304 km : registered) {
         lines.remove("key_" + km.method_1431());
      }

      return lines;
   }

   private static Map<String, String> savedKeyLines(File optionsFile) {
      Map<String, String> lines = new LinkedHashMap();
      if (optionsFile != null && optionsFile.exists()) {
         try {
            BufferedReader reader = Files.newBufferedReader(optionsFile.toPath(), StandardCharsets.UTF_8);

            String line;
            try {
               while ((line = reader.readLine()) != null) {
                  if (line.startsWith("key_")) {
                     int colon = line.indexOf(58);
                     if (colon > 0) {
                        lines.put(line.substring(0, colon), line.substring(colon + 1));
                     }
                  }
               }
            } catch (Throwable var6) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var5) {
                     var6.addSuppressed(var5);
                  }
               }

               throw var6;
            }

            if (reader != null) {
               reader.close();
            }
         } catch (IOException ex) {
            LOG.warn("[Apoli] Could not read {} to restore keybinds: {}", optionsFile.getName(), ex.getMessage());
         }

         return lines;
      } else {
         return lines;
      }
   }
}
