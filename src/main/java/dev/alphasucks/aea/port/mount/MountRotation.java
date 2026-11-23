package dev.alphasucks.aea.port.mount;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3532;

public enum MountRotation {
   HEAD("head"),
   BODY("body");

   public static final Codec<MountRotation> CODEC = Codec.STRING.comapFlatMap(MountRotation::byName, MountRotation::getSerializedName);
   private final String name;

   MountRotation(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public float yawOf(class_1297 vehicle) {
      return this == BODY && vehicle instanceof class_1309 living ? living.field_6283 : vehicle.method_36454();
   }

   public float yawOf(class_1297 vehicle, float partialTick) {
      return this == BODY && vehicle instanceof class_1309 living
         ? class_3532.method_17821(partialTick, living.field_6220, living.field_6283)
         : class_3532.method_17821(partialTick, vehicle.field_5982, vehicle.method_36454());
   }

   private static DataResult<MountRotation> byName(String raw) {
      return switch (raw) {
         case "head", "HEAD" -> DataResult.success(HEAD);
         case "body", "BODY" -> DataResult.success(BODY);
         default -> DataResult.error(
            () -> "'"
               + raw
               + "' is not a mount rotation. Use \"head\" (the vehicle's look direction) or \"body\" (the vehicle's body yaw, so a rider stays put when the vehicle looks around)."
         );
      };
   }
}
