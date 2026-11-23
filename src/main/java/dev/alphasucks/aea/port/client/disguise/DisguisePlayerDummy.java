package dev.alphasucks.aea.port.client.disguise;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_1664;
import net.minecraft.class_638;
import net.minecraft.class_745;

public final class DisguisePlayerDummy extends class_745 {
   private static final byte ALL_MODEL_PARTS = allModelParts();

   public DisguisePlayerDummy(class_638 level, GameProfile profile) {
      super(level, profile);
      this.method_5841().method_12778(field_7518, ALL_MODEL_PARTS);
   }

   public boolean method_7325() {
      return false;
   }

   public boolean method_7337() {
      return false;
   }

   public boolean method_5810() {
      return false;
   }

   protected void method_6070() {
   }

   public void snapCloak() {
      this.field_7500 = this.method_23317();
      this.field_7521 = this.method_23318();
      this.field_7499 = this.method_23321();
      this.field_7524 = this.field_7500;
      this.field_7502 = this.field_7521;
      this.field_7522 = this.field_7499;
   }

   private static byte allModelParts() {
      int mask = 0;

      for (class_1664 part : class_1664.values()) {
         mask |= part.method_7430();
      }

      return (byte)mask;
   }
}
