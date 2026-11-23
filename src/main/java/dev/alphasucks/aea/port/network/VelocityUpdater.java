package dev.alphasucks.aea.port.network;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.network.payload.ApplyVelocityS2C;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_243;

public final class VelocityUpdater {
   private VelocityUpdater() {
   }

   public static void apply(class_1297 recipient, class_243 worldDelta, boolean set) {
      if (!(recipient instanceof class_1657)) {
         class_243 current = recipient.method_18798();
         recipient.method_18799(set ? worldDelta : current.method_1019(worldDelta));
         recipient.field_6007 = true;
      }

      ApoliNetwork.sendApplyVelocityToTrackers(
         recipient, new ApplyVelocityS2C(recipient.method_5628(), worldDelta.field_1352, worldDelta.field_1351, worldDelta.field_1350, set)
      );
   }
}
