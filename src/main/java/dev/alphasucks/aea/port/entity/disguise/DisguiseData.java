package dev.alphasucks.aea.port.entity.disguise;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_2487;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record DisguiseData(class_2960 entityTypeId, Optional<UUID> playerUuid, Optional<class_2487> nbt, Optional<String> name) {
   public boolean isPlayerDisguise() {
      return this.playerUuid.isPresent();
   }

   public void write(class_2540 buf) {
      buf.method_10812(this.entityTypeId);
      buf.method_37435(this.playerUuid, (b, uuid) -> b.method_10797(uuid));
      buf.method_37435(this.nbt, (b, tag) -> b.method_10794(tag));
      buf.method_37435(this.name, (b, string) -> b.method_10814(string));
   }

   public static DisguiseData read(class_2540 buf) {
      return new DisguiseData(
         buf.method_10810(), buf.method_37436(b -> b.method_10790()), buf.method_37436(b -> b.method_10798()), buf.method_37436(b -> b.method_19772())
      );
   }
}
