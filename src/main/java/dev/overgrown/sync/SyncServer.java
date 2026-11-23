package dev.overgrown.sync;

import net.fabricmc.api.DedicatedServerModInitializer;

public class SyncServer implements DedicatedServerModInitializer {
   public void onInitializeServer() {
      // KeyPressManager.serverTick is already driven by ServerTickEventMixin (common mixin, runs on integrated and
      // dedicated servers alike); registering it on END_SERVER_TICK as well made it run twice per tick here
   }
}
