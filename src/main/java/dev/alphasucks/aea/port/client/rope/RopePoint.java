package dev.alphasucks.aea.port.client.rope;

import net.minecraft.class_243;

public class RopePoint {
   public class_243 pos;
   public class_243 prevPos;

   public RopePoint(class_243 pos) {
      this.pos = pos;
      this.prevPos = pos;
   }
}
