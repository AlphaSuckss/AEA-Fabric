package dev.alphasucks.aea.port.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3262;
import net.minecraft.class_3264;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class JsonDataFiles {
   private JsonDataFiles() {
   }

   public static Map<class_2960, JsonElement> load(class_3300 manager, String directory, Logger log) {
      Map<class_2960, JsonDataFiles.Source> sources = topSources(manager, directory, log);
      Map<class_2960, JsonElement> loaded = new HashMap(sources.size());

      for (Entry<class_2960, JsonDataFiles.Source> entry : sources.entrySet()) {
         JsonElement json = read((JsonDataFiles.Source)entry.getValue(), (class_2960)entry.getKey(), log);
         if (json != null) {
            loaded.put((class_2960)entry.getKey(), json);
         }
      }

      return loaded;
   }

   public static Map<class_2960, JsonDataFiles.Source> topSources(class_3300 manager, String directory, Logger log) {
      Map<class_2960, class_3298> files = manager.method_14488(directory, JsonDataFiles::isDataFile);
      Map<class_2960, JsonDataFiles.Source> top = new HashMap(files.size());
      JsonDataFiles.PackOrder order = null;

      for (Entry<class_2960, class_3298> entry : files.entrySet()) {
         class_2960 file = (class_2960)entry.getKey();
         JsonFormat format = JsonFormat.of(file.method_12832());
         class_2960 id = idOf(file, directory, format);
         if (id != null) {
            JsonDataFiles.Source source = new JsonDataFiles.Source(file, (class_3298)entry.getValue(), format);
            JsonDataFiles.Source previous = (JsonDataFiles.Source)top.putIfAbsent(id, source);
            if (previous != null) {
               if (order == null) {
                  order = new JsonDataFiles.PackOrder(manager);
               }

               if (order.compare(source, previous, id, log) > 0) {
                  top.put(id, source);
               }
            }
         }
      }

      return top;
   }

   public static Map<class_2960, List<JsonDataFiles.Source>> stacks(class_3300 manager, String directory, Logger log) {
      Map<class_2960, List<class_3298>> files = manager.method_41265(directory, JsonDataFiles::isDataFile);
      Map<class_2960, List<JsonDataFiles.Source>> stacks = new HashMap(files.size());
      JsonDataFiles.PackOrder order = null;

      for (Entry<class_2960, List<class_3298>> entry : files.entrySet()) {
         class_2960 file = (class_2960)entry.getKey();
         JsonFormat format = JsonFormat.of(file.method_12832());
         class_2960 id = idOf(file, directory, format);
         if (id != null) {
            List<class_3298> resources = (List<class_3298>)entry.getValue();
            List<JsonDataFiles.Source> sources = new ArrayList(resources.size());
            int i = 0;

            for (int n = resources.size(); i < n; i++) {
               sources.add(new JsonDataFiles.Source(file, (class_3298)resources.get(i), format));
            }

            List<JsonDataFiles.Source> previous = (List<JsonDataFiles.Source>)stacks.putIfAbsent(id, sources);
            if (previous != null) {
               if (order == null) {
                  order = new JsonDataFiles.PackOrder(manager);
               }

               stacks.put(id, order.merge(previous, sources, id, log));
            }
         }
      }

      return stacks;
   }

   public static Map<class_2960, JsonDataFiles.Version> loadByPriority(class_3300 manager, String directory, Logger log) {
      Map<class_2960, List<JsonDataFiles.Source>> stacks = stacks(manager, directory, log);
      Map<class_2960, JsonDataFiles.Version> chosen = new HashMap(stacks.size());

      for (Entry<class_2960, List<JsonDataFiles.Source>> entry : stacks.entrySet()) {
         class_2960 id = (class_2960)entry.getKey();
         List<JsonDataFiles.Source> sources = (List<JsonDataFiles.Source>)entry.getValue();
         JsonDataFiles.Version best = null;
         int bestPriority = 0;
         int i = 0;

         for (int n = sources.size(); i < n; i++) {
            JsonDataFiles.Source source = (JsonDataFiles.Source)sources.get(i);
            JsonElement json = read(source, id, log);
            if (json != null) {
               int priority = n == 1 ? 0 : loadingPriority(json);
               if (best == null || priority >= bestPriority) {
                  best = new JsonDataFiles.Version(source, json);
                  bestPriority = priority;
               }
            }
         }

         if (best != null) {
            chosen.put(id, best);
         }
      }

      return chosen;
   }

   public static int loadingPriority(JsonElement json) {
      return new Dynamic(JsonOps.INSTANCE, json).get("loading_priority").asInt(0);
   }

   @Nullable
   public static JsonElement read(JsonDataFiles.Source source, class_2960 id, Logger log) {
      return read(source, id, log, null);
   }

   @Nullable
   public static JsonElement read(JsonDataFiles.Source source, class_2960 id, Logger log, @Nullable String mustMention) {
      byte[] bytes;
      try {
         InputStream in = source.resource().method_14482();

         try {
            bytes = in.readAllBytes();
         } catch (Throwable var10) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var8) {
                  var10.addSuppressed(var8);
               }
            }

            throw var10;
         }

         if (in != null) {
            in.close();
         }
      } catch (IOException e) {
         log.error("Couldn't read data file {} ({} in {}): {}", new Object[]{id, source.file(), source.packId(), e.getMessage()});
         return null;
      }

      if (mustMention != null && !contains(bytes, mustMention.getBytes(StandardCharsets.UTF_8))) {
         return null;
      }

      try {
         JsonTextParser.Parsed parsed = JsonTextParser.read(bytes, source.format());
         if (parsed.trailingLine() > 0) {
            log.warn(
               "Data file {} ({} in {}) has text after its last closing bracket on line {}, which is ignored",
               new Object[]{id, source.file(), source.packId(), parsed.trailingLine()}
            );
         }

         return parsed.value();
      } catch (JsonParseException e) {
         log.error("Couldn't parse data file {} ({} in {}): {}", new Object[]{id, source.file(), source.packId(), e.getMessage()});
         return null;
      }
   }

   private static boolean contains(byte[] haystack, byte[] needle) {
      int last = haystack.length - needle.length;
      byte first = needle[0];

      for (int i = 0; i <= last; i++) {
         if (haystack[i] == first) {
            int k = 1;

            while (k < needle.length && haystack[i + k] == needle[k]) {
               k++;
            }

            if (k == needle.length) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isDataFile(class_2960 location) {
      return JsonFormat.of(location.method_12832()) != null;
   }

   @Nullable
   public static class_2960 idOf(class_2960 file, String directory, @Nullable JsonFormat format) {
      if (format == null) {
         return null;
      }

      String path = file.method_12832();
      int start = directory.length() + 1;
      int stop = path.length() - format.extension().length();
      return stop > start && path.startsWith(directory) && path.charAt(directory.length()) == '/' ? file.method_45136(path.substring(start, stop)) : null;
   }

   private static final class PackOrder {
      private final List<class_3262> packs;
      private final IdentityHashMap<class_3262, Integer> index;

      private PackOrder(class_3300 manager) {
         this.packs = manager.method_29213().toList();
         this.index = new IdentityHashMap(this.packs.size());

         for (int i = 0; i < this.packs.size(); i++) {
            this.index.put((class_3262)this.packs.get(i), i);
         }
      }

      private int topRank(JsonDataFiles.Source source) {
         Integer known = (Integer)this.index.get(source.resource().method_45304());
         if (known != null) {
            return known;
         }

         for (int i = this.packs.size() - 1; i >= 0; i--) {
            if (((class_3262)this.packs.get(i)).method_14405(class_3264.field_14190, source.file()) != null) {
               return i;
            }
         }

         return -1;
      }

      private int compare(JsonDataFiles.Source a, JsonDataFiles.Source b, class_2960 id, Logger log) {
         int byPack = Integer.compare(this.topRank(a), this.topRank(b));
         if (byPack != 0) {
            return byPack;
         }

         JsonDataFiles.Source kept = a.format().ordinal() < b.format().ordinal() ? a : b;
         log.warn("Data file {} is defined twice in {} ({} and {}); using {}", new Object[]{id, a.packId(), a.file(), b.file(), kept.file()});
         return Integer.compare(b.format().ordinal(), a.format().ordinal());
      }

      private List<JsonDataFiles.Source> merge(List<JsonDataFiles.Source> first, List<JsonDataFiles.Source> second, class_2960 id, Logger log) {
         List<JsonDataFiles.Source> merged = new ArrayList(first.size() + second.size());
         int[] firstRanks = this.ranks(first);
         int[] secondRanks = this.ranks(second);
         int i = 0;
         int j = 0;

         while (i < first.size() || j < second.size()) {
            if (j >= second.size()) {
               merged.add((JsonDataFiles.Source)first.get(i++));
            } else if (i >= first.size()) {
               merged.add((JsonDataFiles.Source)second.get(j++));
            } else if (firstRanks[i] != secondRanks[j]) {
               merged.add(firstRanks[i] < secondRanks[j] ? (JsonDataFiles.Source)first.get(i++) : (JsonDataFiles.Source)second.get(j++));
            } else {
               JsonDataFiles.Source a = (JsonDataFiles.Source)first.get(i);
               JsonDataFiles.Source b = (JsonDataFiles.Source)second.get(j);
               this.compare(a, b, id, log);
               if (a.format().ordinal() > b.format().ordinal()) {
                  merged.add((JsonDataFiles.Source)first.get(i++));
               } else {
                  merged.add((JsonDataFiles.Source)second.get(j++));
               }
            }
         }

         return merged;
      }

      private int[] ranks(List<JsonDataFiles.Source> sources) {
         int[] ranks = new int[sources.size()];
         int floor = 0;
         boolean previousKnown = false;

         for (int k = 0; k < ranks.length; k++) {
            JsonDataFiles.Source source = (JsonDataFiles.Source)sources.get(k);
            Integer known = (Integer)this.index.get(source.resource().method_45304());
            int rank;
            if (known != null) {
               rank = Math.max(known, floor);
            } else {
               rank = this.lowestPackWith(source.file(), previousKnown ? floor + 1 : floor);
            }

            ranks[k] = rank;
            floor = rank;
            previousKnown = known != null;
         }

         return ranks;
      }

      private int lowestPackWith(class_2960 file, int from) {
         for (int i = from; i < this.packs.size(); i++) {
            if (((class_3262)this.packs.get(i)).method_14405(class_3264.field_14190, file) != null) {
               return i;
            }
         }

         return this.packs.size();
      }
   }

   public record Source(class_2960 file, class_3298 resource, JsonFormat format) {
      public String packId() {
         return this.resource.method_14480();
      }
   }

   public record Version(JsonDataFiles.Source source, JsonElement json) {
   }
}
