package dev.alphasucks.aea.port.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.class_2487;
import net.minecraft.class_2509;
import net.minecraft.class_2874;
import net.minecraft.class_3218;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class OfflinePlayerSnapshot extends FakePlayer {
   private OfflinePlayerSnapshot(class_3218 level, GameProfile profile) {
      super(level, profile);
   }

   @Nullable
   public static OfflinePlayerSnapshot load(MinecraftServer server, GameProfile profile) {
      OfflinePlayerSnapshot snapshot = new OfflinePlayerSnapshot(server.method_30002(), profile);
      class_2487 data = server.method_3760().method_14600(snapshot);
      if (data == null) {
         return null;
      }

      class_3218 level = (class_3218)class_2874.method_28521(new Dynamic(class_2509.field_11560, data.method_10580("Dimension")))
         .result()
         .map(server::method_3847)
         .orElse(null);
      if (level != null && level != snapshot.method_51469()) {
         snapshot.method_51468(level);
      }

      return snapshot;
   }
}
