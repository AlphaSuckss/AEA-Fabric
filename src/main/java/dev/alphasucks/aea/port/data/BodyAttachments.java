package dev.alphasucks.aea.port.data;

public final class BodyAttachments {
   public static final int RIGHT_WING = 1;
   public static final int LEFT_WING = 2;
   public static final int WING_PAIR = 4;
   public static final int RIGHT_EAR = 8;
   public static final int LEFT_EAR = 16;
   public static final int EAR_PAIR = 32;
   public static final int HORNS = 64;
   public static final int SNOUT = 128;
   public static final int TAIL = 256;
   public static final int RIGHT_ARM_CLAW = 512;
   public static final int LEFT_ARM_CLAW = 1024;
   public static final int RIGHT_LEG_CLAW = 2048;
   public static final int LEFT_LEG_CLAW = 4096;
   public static final int CHEST = 8192;
   public static final int CAPE = 16384;
   public static final int RIGHT_DIGITIGRADE_LEG = 32768;
   public static final int LEFT_DIGITIGRADE_LEG = 65536;
   public static final int HALO = 131072;
   public static final int RIGHT_ITEM = 262144;
   public static final int LEFT_ITEM = 524288;
   public static final int COUNT = 20;
   public static final int WINGS = 7;
   public static final int EARS = 56;
   public static final int CLAWS = 7680;
   public static final int DIGITIGRADE_LEGS = 98304;
   public static final int ITEMS = 786432;

   private BodyAttachments() {
   }

   public static int index(int bit) {
      return Integer.numberOfTrailingZeros(bit);
   }
}
