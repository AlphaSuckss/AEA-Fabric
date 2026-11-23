package dev.alphasucks.aea.port.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.model.BedrockModelParser;
import dev.alphasucks.aea.port.client.model.CustomModel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import net.minecraft.class_3300;
import org.jetbrains.annotations.Nullable;

public final class CustomModelManager implements SimpleSynchronousResourceReloadListener {
   public static final CustomModelManager INSTANCE = new CustomModelManager();
   private static final String[] PREFIXES = new String[]{"geo/", "models/apoli/"};
   private static final String SUFFIX = ".geo.json";
   private static final Map<class_2960, CustomModel> MODELS = new HashMap();
   private static final Map<class_2960, CustomModel> UNBOUND = new HashMap();

   private CustomModelManager() {
   }

   public class_2960 getFabricId() {
      return Apoli.id("custom_models");
   }

   public void method_14491(class_3300 manager) {
      MODELS.clear();
      UNBOUND.clear();

      for (String prefix : PREFIXES) {
         String scanPath = prefix.substring(0, prefix.length() - 1);
         Map<class_2960, class_3298> found = manager.method_14488(scanPath, location -> location.method_12832().endsWith(".geo.json"));

         for (Entry<class_2960, class_3298> entry : found.entrySet()) {
            class_2960 file = (class_2960)entry.getKey();
            String path = file.method_12832();
            if (path.startsWith(prefix)) {
               String trimmed = path.substring(prefix.length(), path.length() - ".geo.json".length());
               class_2960 id = new class_2960(file.method_12836(), trimmed);
               if (!MODELS.containsKey(id)) {
                  try {
                     InputStream stream = ((class_3298)entry.getValue()).method_14482();

                     try {
                        JsonElement raw = JsonParser.parseReader(new InputStreamReader(stream));
                        Dynamic<JsonElement> json = new Dynamic(JsonOps.INSTANCE, raw);
                        CustomModel[] variants = BedrockModelParser.parseVariants(id, json);
                        MODELS.put(id, variants[0]);
                        if (variants[1] != variants[0]) {
                           UNBOUND.put(id, variants[1]);
                        }
                     } catch (Throwable var19) {
                        if (stream != null) {
                           try {
                              stream.close();
                           } catch (Throwable var18) {
                              var19.addSuppressed(var18);
                           }
                        }

                        throw var19;
                     }

                     if (stream != null) {
                        stream.close();
                     }
                  } catch (Exception e) {
                     Apoli.LOGGER.error("[Apoli] Failed to load custom model {}: {}", id, e.getMessage());
                  }
               }
            }
         }
      }

      Apoli.LOGGER.info("[Apoli] Loaded {} custom model(s) for custom_model_render.", MODELS.size());
   }

   @Nullable
   public static CustomModel get(class_2960 id) {
      return (CustomModel)MODELS.get(id);
   }

   @Nullable
   public static CustomModel get(class_2960 id, boolean bindBodyParts) {
      if (!bindBodyParts) {
         CustomModel unbound = (CustomModel)UNBOUND.get(id);
         if (unbound != null) {
            return unbound;
         }
      }

      return (CustomModel)MODELS.get(id);
   }
}
