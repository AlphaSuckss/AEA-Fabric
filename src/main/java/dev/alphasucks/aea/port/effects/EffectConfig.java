package dev.alphasucks.aea.port.effects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;

/** {@code config/apoli-effects.json}: {@code {"enabled": true}} turns data-driven custom mob effects on or off. */
public record EffectConfig(boolean enabled) {
   private static final Gson PRINTER = new GsonBuilder().setPrettyPrinting().create();
   private static final EffectConfig DEFAULTS = new EffectConfig(true);
   private static final Codec<EffectConfig> CODEC = RecordCodecBuilder.create(
      i -> i.group(Codec.BOOL.optionalFieldOf("enabled").forGetter(c -> Optional.of(c.enabled))).apply(i, opt -> new EffectConfig((Boolean)opt.orElse(true)))
   );
   private static volatile EffectConfig instance;

   public static EffectConfig get() {
      EffectConfig local = instance;
      if (local == null) {
         synchronized (EffectConfig.class) {
            local = instance;
            if (local == null) {
               instance = local = load();
            }
         }
      }

      return local;
   }

   private static Path path() {
      return FabricLoader.getInstance().getConfigDir().resolve("apoli-effects.json");
   }

   private static EffectConfig load() {
      Path path = path();
      if (Files.exists(path, new LinkOption[0])) {
         try {
            JsonElement json = JsonParser.parseString(Files.readString(path));
            EffectConfig loaded = (EffectConfig)CODEC.parse(JsonOps.INSTANCE, json)
               .resultOrPartial(err -> Apoli.LOGGER.warn("[Apoli] apoli-effects.json: {}", err))
               .orElse(null);
            if (loaded != null) {
               return loaded;
            }
         } catch (Exception e) {
            Apoli.LOGGER.warn("[Apoli] Could not read apoli-effects.json; using defaults", e);
         }

         return DEFAULTS;
      } else {
         DEFAULTS.save();
         return DEFAULTS;
      }
   }

   private void save() {
      try {
         Path path = path();
         Files.createDirectories(path.getParent(), new FileAttribute[0]);
         JsonElement json = (JsonElement)CODEC.encodeStart(JsonOps.INSTANCE, this)
            .resultOrPartial(err -> Apoli.LOGGER.warn("[Apoli] apoli-effects.json: {}", err))
            .orElse(null);
         if (json != null) {
            Files.writeString(path, PRINTER.toJson(json), new OpenOption[0]);
         }
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] Could not write apoli-effects.json", e);
      }
   }
}
