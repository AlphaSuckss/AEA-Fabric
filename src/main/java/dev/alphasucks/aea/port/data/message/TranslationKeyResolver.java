package dev.alphasucks.aea.port.data.message;

import dev.alphasucks.aea.port.Apoli;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.class_2477;

public final class TranslationKeyResolver {
   private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([^}]+)}");
   private static final Set<String> WANTED = ConcurrentHashMap.newKeySet();
   private static volatile Map<String, Set<String>> translations = Collections.emptyMap();
   private static volatile boolean loaded;

   private TranslationKeyResolver() {
   }

   public static boolean hasPlaceholder(String pattern) {
      return pattern.indexOf("#{") >= 0 && PLACEHOLDER.matcher(pattern).find();
   }

   public static void require(String pattern) {
      Matcher matcher = PLACEHOLDER.matcher(pattern);

      while (matcher.find()) {
         if (WANTED.add(matcher.group(1))) {
            loaded = false;
         }
      }
   }

   public static void invalidate() {
      loaded = false;
   }

   private static void ensureLoaded() {
      if (!loaded) {
         synchronized (TranslationKeyResolver.class) {
            if (!loaded) {
               translations = scan(Set.copyOf(WANTED));
               loaded = true;
            }
         }
      }
   }

   private static Map<String, Set<String>> scan(Set<String> wanted) {
      if (wanted.isEmpty()) {
         return Collections.emptyMap();
      }

      Map<String, Set<String>> map = new HashMap(wanted.size());
      int files = 0;

      for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
         for (Path root : mod.getRootPaths()) {
            Path assets = root.resolve("assets");
            if (Files.isDirectory(assets, new LinkOption[0])) {
               try {
                  DirectoryStream<Path> namespaces = Files.newDirectoryStream(assets);

                  try {
                     for (Path namespace : namespaces) {
                        Path langDir = namespace.resolve("lang");
                        if (Files.isDirectory(langDir, new LinkOption[0])) {
                           DirectoryStream<Path> langFiles = Files.newDirectoryStream(langDir, "*.json");

                           try {
                              for (Path langFile : langFiles) {
                                 if (loadFile(langFile, wanted, map)) {
                                    files++;
                                 }
                              }
                           } catch (Throwable var17) {
                              if (langFiles != null) {
                                 try {
                                    langFiles.close();
                                 } catch (Throwable var16) {
                                    var17.addSuppressed(var16);
                                 }
                              }

                              throw var17;
                           }

                           if (langFiles != null) {
                              langFiles.close();
                           }
                        }
                     }
                  } catch (Throwable var18) {
                     if (namespaces != null) {
                        try {
                           namespaces.close();
                        } catch (Throwable var15) {
                           var18.addSuppressed(var15);
                        }
                     }

                     throw var18;
                  }

                  if (namespaces != null) {
                     namespaces.close();
                  }
               } catch (Exception e) {
                  Apoli.LOGGER.warn("[Apoli] Failed to scan lang files for {}: {}", mod.getMetadata().getId(), e.getMessage());
               }
            }
         }
      }

      Apoli.LOGGER.info("[Apoli] Resolved {} of {} referenced translation key(s) from {} language file(s).", new Object[]{map.size(), wanted.size(), files});

      for (String key : wanted) {
         if (!map.containsKey(key)) {
            Apoli.LOGGER.warn("[Apoli] Translation key '{}' is used in a message filter but was not found in any language file.", key);
         }
      }

      return map;
   }

   private static boolean loadFile(Path file, Set<String> wanted, Map<String, Set<String>> map) {
      try {
         InputStream stream = Files.newInputStream(file, new OpenOption[0]);

         boolean var4;
         try {
            class_2477.method_29425(stream, (key, value) -> {
               if (wanted.contains(key) && !value.isEmpty()) {
                  ((Set)map.computeIfAbsent(key, k -> new LinkedHashSet())).add(value);
               }
            });
            var4 = true;
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }

         return var4;
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] Failed to read language file {}: {}", file, e.getMessage());
         return false;
      }
   }

   public static String expandPattern(String rawPattern) {
      Matcher matcher = PLACEHOLDER.matcher(rawPattern);
      if (!matcher.find()) {
         return rawPattern;
      }

      ensureLoaded();
      Map<String, Set<String>> current = translations;
      StringBuilder builder = new StringBuilder();
      matcher.reset();

      while (matcher.find()) {
         String key = matcher.group(1);
         Set<String> values = (Set<String>)current.get(key);
         if (values != null && !values.isEmpty()) {
            String alternatives = (String)values.stream()
               .sorted(Comparator.comparingInt(String::length).reversed())
               .map(Pattern::quote)
               .collect(Collectors.joining("|"));
            matcher.appendReplacement(builder, Matcher.quoteReplacement("(?i:" + alternatives + ")"));
         } else {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(matcher.group()));
         }
      }

      matcher.appendTail(builder);
      return builder.toString();
   }
}
