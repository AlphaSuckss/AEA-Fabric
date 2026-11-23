package dev.alphasucks.aea.port.entity;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_6862;
import net.minecraft.class_2338.class_2339;
import org.jetbrains.annotations.Nullable;

public final class StagedLanding {
   private static final double GROUND_PROBE = 0.001;
   @Nullable
   private static class_1297 staged;

   private StagedLanding() {
   }

   public static boolean isStaged(class_1297 entity) {
      return staged != null && staged == entity;
   }

   @Nullable
   static class_1297 begin(class_1297 entity) {
      class_1297 previous = staged;
      staged = entity;
      return previous;
   }

   static void end(@Nullable class_1297 previous) {
      staged = previous;
   }

   public static double fluidHeight(class_1297 entity, class_6862<class_3611> fluid) {
      if (entity.method_33724()) {
         return 0.0;
      }

      class_238 box = entity.method_5829().method_1011(0.001);
      int minX = class_3532.method_15357(box.field_1323);
      int maxX = class_3532.method_15384(box.field_1320);
      int minY = class_3532.method_15357(box.field_1322);
      int maxY = class_3532.method_15384(box.field_1325);
      int minZ = class_3532.method_15357(box.field_1321);
      int maxZ = class_3532.method_15384(box.field_1324);
      class_1937 level = entity.method_37908();
      class_2339 pos = new class_2339();
      double height = 0.0;

      for (int x = minX; x < maxX; x++) {
         for (int y = minY; y < maxY; y++) {
            for (int z = minZ; z < maxZ; z++) {
               class_3610 state = level.method_8316(pos.method_10103(x, y, z));
               if (state.method_15767(fluid)) {
                  double surface = y + state.method_15763(level, pos);
                  if (surface >= box.field_1322) {
                     height = Math.max(height, surface - box.field_1322);
                  }
               }
            }
         }
      }

      return height;
   }

   public static boolean eyeInFluid(class_1297 entity, class_6862<class_3611> fluid) {
      double eyeY = entity.method_23320() - 0.11111111F;
      class_2338 pos = class_2338.method_49637(entity.method_23317(), eyeY, entity.method_23321());
      class_3610 state = entity.method_37908().method_8316(pos);
      return state.method_15767(fluid) && pos.method_10264() + state.method_15763(entity.method_37908(), pos) > eyeY;
   }

   public static boolean onGround(class_1297 entity) {
      class_238 box = entity.method_5829();
      class_238 below = new class_238(box.field_1323, box.field_1322 - 0.001, box.field_1321, box.field_1320, box.field_1322, box.field_1324);
      return !entity.method_37908().method_8587(entity, below);
   }
}
