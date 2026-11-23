package dev.alphasucks.aea.port.loader;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.keybind.ApoliKeybinds;
import dev.alphasucks.aea.port.keybind.Keybind;
import dev.alphasucks.aea.port.network.payload.SyncKeybindsS2C;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4080;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class ApoliKeybindLoader extends class_4080<Map<class_2960, JsonElement>> {
   private static final Logger LOG = LogUtils.getLogger();
   private static final String DIR = "keybinds";
   private MinecraftServer server;

   // the original class carried javac bridges from class_4080's intermediary names to prepare/apply
   @Override
   protected Map<class_2960, JsonElement> method_18789(class_3300 manager, class_3695 profiler) {
      return this.prepare(manager, profiler);
   }

   @Override
   protected void method_18788(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.apply(data, manager, profiler);
   }

   @NotNull
   protected Map<class_2960, JsonElement> prepare(class_3300 resourceManager, class_3695 profilerFiller) {
      return JsonDataFiles.load(resourceManager, "keybinds", LOG);
   }

   public void attachServer(MinecraftServer server) {
      this.server = server;
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 rm, class_3695 profiler) {
      Map<class_2960, Keybind> loaded = new LinkedHashMap();

      for (Entry<class_2960, JsonElement> e : data.entrySet()) {
         class_2960 id = (class_2960)e.getKey();
         Keybind.CODEC_NO_ID
            .parse(JsonOps.INSTANCE, (JsonElement)e.getValue())
            .resultOrPartial(err -> LOG.error("Failed to parse keybind {}: {}", id, err))
            .ifPresent(kb -> loaded.put(id, kb.withId(id)));
      }

      ApoliKeybinds.replaceAll(loaded);
      LOG.info("[Apoli] Loaded {} data-driven keybind(s).", loaded.size());
      if (this.server != null) {
         ApoliNetwork.broadcastKeybinds(this.server, SyncKeybindsS2C.fromCurrent());
      }
   }
}
