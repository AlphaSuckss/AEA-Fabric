package dev.alphasucks.aea.port.data;

import net.minecraft.class_2338;
import net.minecraft.class_2394;
import net.minecraft.class_2596;
import net.minecraft.class_2675;
import net.minecraft.class_3218;
import net.minecraft.class_3222;

public final class ParticleBroadcast {
   private ParticleBroadcast() {
   }

   public static class_2596<?> packet(
      class_2394 options, boolean force, double x, double y, double z, int count, float spreadX, float spreadY, float spreadZ, float speed
   ) {
      return new class_2675(options, force, x, y, z, spreadX, spreadY, spreadZ, speed, count);
   }

   public static void send(class_3218 level, class_3222 player, class_2596<?> packet) {
      if (player.method_37908() == level) {
         player.field_13987.method_14364(packet);
      }
   }

   public static boolean inRange(class_3222 player, boolean force, double x, double y, double z) {
      class_2338 at = player.method_24515();
      double dx = at.method_10263() + 0.5 - x;
      double dy = at.method_10264() + 0.5 - y;
      double dz = at.method_10260() + 0.5 - z;
      double limit = force ? 512.0 : 32.0;
      return dx * dx + dy * dy + dz * dz < limit * limit;
   }
}
