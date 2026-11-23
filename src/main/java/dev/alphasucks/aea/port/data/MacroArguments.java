package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.class_1297;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2503;
import net.minecraft.class_2516;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record MacroArguments(
   Optional<class_2960> storage, String path, Optional<class_2487> values, Map<String, class_2960> resources, Map<String, class_2960> targetResources
) {
   private static final Logger LOG = LogUtils.getLogger();
   private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
   private static final Codec<Map<String, class_2960>> RESOURCE_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, IdCodecs.ID), IdCodecs.ID.listOf())
      .xmap(either -> (Map)either.map(Function.identity(), MacroArguments::keyByPath), Either::left);
   public static final Codec<MacroArguments> CODEC = RecordCodecBuilder.create(
      i -> i.group(
            IdCodecs.ID.optionalFieldOf("storage").forGetter(MacroArguments::storage),
            Codec.STRING.optionalFieldOf("path", "").forGetter(MacroArguments::path),
            class_2487.field_25128.optionalFieldOf("values").forGetter(MacroArguments::values),
            RESOURCE_MAP.optionalFieldOf("resources", Map.of()).forGetter(MacroArguments::resources),
            RESOURCE_MAP.optionalFieldOf("target_resources", Map.of()).forGetter(MacroArguments::targetResources)
         )
         .apply(i, MacroArguments::new)
   );
   public static final MapCodec<Optional<MacroArguments>> FIELD = LoggedOptionalField.strict("arguments", CODEC);

   public MacroArguments {
      resources = Map.copyOf(resources);
      targetResources = Map.copyOf(targetResources);
   }

   private static Map<String, class_2960> keyByPath(List<class_2960> ids) {
      Map<String, class_2960> out = new LinkedHashMap(ids.size());

      for (class_2960 id : ids) {
         String path = id.method_12832();
         out.put(path.substring(path.lastIndexOf(47) + 1), id);
      }

      return out;
   }

   public class_2487 resolve(MinecraftServer server) {
      return this.resolve(server, null, null);
   }

   public class_2487 resolve(MinecraftServer server, @Nullable class_1297 self, @Nullable class_1297 target) {
      class_2487 out = new class_2487();
      if (this.storage.isPresent()) {
         class_2487 stored = StorageTarget.read(server, (class_2960)this.storage.get(), this.path);
         if (stored != null) {
            out.method_10543(stored);
         }
      }

      readResources(out, this.resources, self);
      readResources(out, this.targetResources, target);
      this.values.ifPresent(out::method_10543);
      return out;
   }

   private static void readResources(class_2487 out, Map<String, class_2960> wanted, @Nullable class_1297 holder) {
      if (!wanted.isEmpty()) {
         PowerContainer container = holder == null ? null : PowerContainer.of(holder);

         for (Entry<String, class_2960> entry : wanted.entrySet()) {
            class_2960 powerId = (class_2960)entry.getValue();
            String key = (String)entry.getKey();
            OptionalInt value = container == null ? OptionalInt.empty() : PowerResources.read(container, powerId);
            if (value.isEmpty()) {
               reportUnreadable(holder, container, key, powerId);
            } else {
               out.method_10569(key, value.getAsInt());
               PowerResources.bound(container, powerId, true).ifPresent(max -> out.method_10569(key + "_max", max));
               PowerResources.bound(container, powerId, false).ifPresent(min -> out.method_10569(key + "_min", min));
            }
         }
      }
   }

   private static void reportUnreadable(@Nullable class_1297 holder, @Nullable PowerContainer container, String key, class_2960 powerId) {
      String reason;
      if (container == null) {
         reason = "this action has no entity to read powers from";
      } else if (!container.hasPower(powerId)) {
         reason = ApoliPowers.get(powerId) == null ? "no power with that id is loaded" : "the holder does not have that power";
      } else if (!PowerResources.isResource(powerId)) {
         reason = "that power is loaded, but it is not a resource";
      } else {
         reason = "that resource holds no value yet";
      }

      String line = "$(" + key + ") is undefined: " + powerId + " could not be read — " + reason + ".";
      DevMode.report(holder, line);
      if (WARNED.add(key + "\u0000" + powerId)) {
         LOG.warn("[Apoli] {}", line);
      }
   }

   public static void resetWarnings() {
      WARNED.clear();
   }

   public static String expand(String command, class_2487 arguments, @Nullable class_1297 actor) {
      int open = command.indexOf("$(");
      if (open < 0) {
         return command;
      }

      StringBuilder out = new StringBuilder(command.length());

      int cursor;
      for (cursor = 0; open >= 0; open = command.indexOf("$(", cursor)) {
         int close = command.indexOf(41, open + 2);
         if (close < 0) {
            break;
         }

         String key = command.substring(open + 2, close);
         class_2520 value = arguments.method_10580(key);
         if (value == null) {
            warnMissing(command, key, actor);
            return null;
         }

         out.append(command, cursor, open).append(render(value));
         cursor = close + 1;
      }

      return out.append(command, cursor, command.length()).toString();
   }

   private static void warnMissing(String command, String key, @Nullable class_1297 actor) {
      DevMode.report(actor, "skipped \"" + command + "\" — nothing supplied a value for $(" + key + ").");
      if (WARNED.add(key + "\u0000" + command)) {
         LOG.warn(
            "[Apoli] apoli:execute_command skipped \"{}\" — nothing supplied a value for $({}). Check the 'arguments' object: a resource the holder does not have, or a storage path that is empty, leaves its key undefined.",
            command,
            key
         );
      }
   }

   private static String render(class_2520 tag) {
      if (tag instanceof class_2494 value) {
         return String.valueOf(value.method_10700());
      } else if (tag instanceof class_2489 value) {
         return String.valueOf(value.method_10697());
      } else if (tag instanceof class_2481 value) {
         return String.valueOf(value.method_10698());
      } else if (tag instanceof class_2516 value) {
         return String.valueOf(value.method_10696());
      } else {
         return tag instanceof class_2503 value ? String.valueOf(value.method_10699()) : tag.method_10714();
      }
   }
}
