package dev.alphasucks.aea.port.mixin.input;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.client.CursorSpeedState;
import dev.alphasucks.aea.port.client.MouseMovementWatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_1297.class)
@Environment(EnvType.CLIENT)
public abstract class EntityTurnCursorSpeedMixin {
   @WrapMethod(method = "method_5872(DD)V")
   private void apoli$turn(double yRot, double xRot, Operation<Void> original) {
      class_1297 self = (class_1297)(Object)this;
      MouseMovementWatcher.onTurn(self, yRot, xRot);
      if (!CursorSpeedState.appliesTo(self)) {
         original.call(new Object[]{yRot, xRot});
      } else {
         original.call(new Object[]{yRot * CursorSpeedState.horizontal(), xRot * CursorSpeedState.vertical()});
      }
   }
}
