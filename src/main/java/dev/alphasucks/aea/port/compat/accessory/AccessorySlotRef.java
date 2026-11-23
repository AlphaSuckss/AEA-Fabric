package dev.alphasucks.aea.port.compat.accessory;

import net.minecraft.class_1799;

public interface AccessorySlotRef {
   String provider();

   String group();

   String type();

   int index();

   class_1799 getStack();

   void setStack(class_1799 var1);

   default String slotId() {
      String g = this.group();
      return g != null && !g.isEmpty() ? g + "/" + this.type() + "/" + this.index() : this.type() + "/" + this.index();
   }
}
