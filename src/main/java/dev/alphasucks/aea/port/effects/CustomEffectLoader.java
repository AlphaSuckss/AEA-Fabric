package dev.alphasucks.aea.port.effects;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.loader.IdWildcards;
import dev.alphasucks.aea.port.loader.JsonDataFiles;
import dev.alphasucks.aea.port.loader.JsonDataFiles.Version;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import org.slf4j.Logger;

/** Reads {@code data/<namespace>/effects/<path>.json}; the file path is the effect id. */
public final class CustomEffectLoader {
   private static final Logger LOG = LogUtils.getLogger();
   private static final String DIR = "effects";

   private CustomEffectLoader() {
   }

   public static List<CustomEffect> load(class_3300 resourceManager) {
      Map<class_2960, Version> versions = JsonDataFiles.loadByPriority(resourceManager, DIR, LOG);
      List<CustomEffect> loaded = new ArrayList<>(versions.size());

      for (Entry<class_2960, Version> entry : versions.entrySet()) {
         class_2960 id = (class_2960)entry.getKey();
         Version version = (Version)entry.getValue();
         CustomEffect.codec(id)
            .parse(IdWildcards.apply(new Dynamic<>(JsonOps.INSTANCE, version.json()), id))
            .resultOrPartial(err -> LOG.error("[Apoli] Failed to parse custom effect {} from pack {}: {}", new Object[]{id, version.source().packId(), err}))
            .ifPresent(loaded::add);
      }

      loaded.sort(Comparator.comparing(CustomEffect::id));
      if (!loaded.isEmpty()) {
         LOG.info("[Apoli] Loaded {} custom effect(s).", loaded.size());
      }

      return List.copyOf(loaded);
   }
}
