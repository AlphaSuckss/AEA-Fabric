package dev.alphasucks.aea.port.client.render;

public final class GhostArmState {
   private static boolean active;
   private static float alpha = 1.0F;

   private GhostArmState() {
   }

   public static void begin(float ghostAlpha) {
      active = true;
      alpha = ghostAlpha;
   }

   public static void end() {
      active = false;
      alpha = 1.0F;
   }

   public static boolean isActive() {
      return active;
   }

   public static float alpha() {
      return alpha;
   }
}
