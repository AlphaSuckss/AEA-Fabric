package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.Apoli;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1060;
import net.minecraft.class_1079;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3298;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class AnimatedTextures {
   private static final Map<class_2960, AnimatedTextures.Entry> BY_SOURCE = new HashMap();

   private AnimatedTextures() {
   }

   public static class_2960 bindable(class_2960 source) {
      AnimatedTextures.Entry entry = (AnimatedTextures.Entry)BY_SOURCE.get(source);
      if (entry == null) {
         entry = detect(source);
         BY_SOURCE.put(source, entry);
      }

      if (entry.texture() != null) {
         entry.texture().markUsed();
      }

      return entry.bound();
   }

   public static void clearCache() {
      if (!BY_SOURCE.isEmpty()) {
         class_1060 textures = class_310.method_1551().method_1531();

         for (AnimatedTextures.Entry entry : BY_SOURCE.values()) {
            AnimatedTexture texture = entry.texture();
            if (texture != null && textures.method_34590(entry.bound(), null) == texture) {
               textures.method_4615(entry.bound());
            }
         }

         BY_SOURCE.clear();
      }
   }

   private static AnimatedTextures.Entry detect(class_2960 source) {
      class_310 minecraft = class_310.method_1551();
      Optional<class_3298> resource = minecraft.method_1478().method_14486(source);
      if (!resource.isEmpty() && animated((class_3298)resource.get())) {
         class_2960 key = Apoli.id("animated/" + source.method_12836() + "/" + source.method_12832());
         AnimatedTexture texture = new AnimatedTexture(source);
         class_1060 textures = minecraft.method_1531();
         textures.method_4616(key, texture);
         return textures.method_34590(key, null) != texture ? new AnimatedTextures.Entry(source, null) : new AnimatedTextures.Entry(key, texture);
      } else {
         return new AnimatedTextures.Entry(source, null);
      }
   }

   private static boolean animated(class_3298 resource) {
      try {
         return resource.method_14481().method_43041(class_1079.field_5337).isPresent();
      } catch (IOException | RuntimeException e) {
         return false;
      }
   }

   private record Entry(class_2960 bound, @Nullable AnimatedTexture texture) {
   }
}
