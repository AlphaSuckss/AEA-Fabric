package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.Apoli;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class ParticleTextures {
   private static final String TEXTURES = "textures/";
   private static final String PARTICLE_DIR = "textures/particle/";
   private static final String PNG = ".png";
   private static final Map<class_2960, class_2960> RESOLVED = new HashMap();
   private static final Map<class_2960, class_2960> BOUND = new HashMap();
   private static final Map<class_2960, class_2960> BLED = new HashMap();

   private ParticleTextures() {
   }

   public static synchronized void clearCache() {
      RESOLVED.clear();
      BOUND.clear();
      BLED.clear();
   }

   public static synchronized class_2960 bound(class_2960 source, boolean bleed) {
      Map<class_2960, class_2960> cache = bleed ? BLED : BOUND;
      class_2960 cached = (class_2960)cache.get(source);
      if (cached != null) {
         return cached;
      }

      class_2960 id = new class_2960("apoli", (bleed ? "particle_bled/" : "particle/") + source.method_12836() + "/" + source.method_12832());
      class_310.method_1551().method_1531().method_4616(id, new RepairedParticleTexture(source, bleed));
      cache.put(source, id);
      return id;
   }

   public static synchronized class_2960 resolve(class_2960 declared) {
      class_2960 cached = (class_2960)RESOLVED.get(declared);
      if (cached != null) {
         return cached;
      }

      class_2960 found = search(declared);
      if (found == null) {
         Apoli.LOGGER
            .warn(
               "[Apoli] Particle texture {} is not in any loaded resource pack — expecting assets/{}/{}. The particle will draw as the missing-texture checker.",
               new Object[]{declared, declared.method_12836(), declared.method_12832()}
            );
         found = declared;
      }

      RESOLVED.put(declared, found);
      return found;
   }

   private static class_2960 search(class_2960 declared) {
      String path = declared.method_12832();
      if (exists(declared)) {
         return declared;
      }

      for (String candidate : candidates(path)) {
         class_2960 id = new class_2960(declared.method_12836(), candidate);
         if (exists(id)) {
            return id;
         }
      }

      return null;
   }

   private static String[] candidates(String path) {
      String withPng = path.endsWith(".png") ? path : path + ".png";
      return path.startsWith("textures/") ? new String[]{withPng} : new String[]{withPng, "textures/particle/" + withPng, "textures/" + withPng};
   }

   private static boolean exists(class_2960 id) {
      return class_310.method_1551().method_1478().method_14486(id).isPresent();
   }
}
