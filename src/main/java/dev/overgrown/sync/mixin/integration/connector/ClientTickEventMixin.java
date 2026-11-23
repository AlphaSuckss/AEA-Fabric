package dev.overgrown.sync.mixin.integration.connector;

import dev.overgrown.sync.SyncClient;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class ClientTickEventMixin {
   // Delegates to the same handler (and the same last-sent state) as SyncClient's START_CLIENT_TICK listener, so the
   // updates are sent once per change even when both run (previously each kept its own copy and both sent packets).
   @Inject(method = "method_1574", at = @At("HEAD"))
   private void sync$onClientTick(CallbackInfo ci) {
      SyncClient.clientTick((class_310)(Object)this);
   }
}
