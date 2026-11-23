package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;
import net.minecraft.class_2902.class_2903;

public enum Heightmap implements class_3542 {
   WORLD_SURFACE("world_surface", class_2903.field_13202),
   WORLD_SURFACE_WG("world_surface_wg", class_2903.field_13194),
   OCEAN_FLOOR("ocean_floor", class_2903.field_13200),
   OCEAN_FLOOR_WG("ocean_floor_wg", class_2903.field_13195),
   MOTION_BLOCKING("motion_blocking", class_2903.field_13197),
   MOTION_BLOCKING_NO_LEAVES("motion_blocking_no_leaves", class_2903.field_13203);

   public static final Codec<Heightmap> CODEC = class_3542.method_28140(Heightmap::values);
   private final String name;
   private final class_2903 vanilla;

   Heightmap(String name, class_2903 vanilla) {
      this.name = name;
      this.vanilla = vanilla;
   }

   public class_2903 vanilla() {
      return this.vanilla;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
