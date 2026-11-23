package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.keybind.ApoliKeybinds;
import dev.alphasucks.aea.port.keybind.Keybind;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record SyncKeybindsS2C(List<Keybind> keybinds) {
   public static final class_2960 CHANNEL = Apoli.internal("sync_keybinds");

   public static SyncKeybindsS2C fromCurrent() {
      return new SyncKeybindsS2C(new ArrayList(ApoliKeybinds.view().values()));
   }

   public void write(class_2540 buf) {
      buf.method_10804(this.keybinds.size());

      for (Keybind kb : this.keybinds) {
         buf.method_10812(kb.id());
         buf.method_10814(kb.key());
         buf.method_10814(kb.category());
         buf.writeBoolean(kb.name().isPresent());
         kb.name().ifPresent(component -> buf.method_10814(Keybind.encodeName(component)));
      }
   }

   public static SyncKeybindsS2C read(class_2540 buf) {
      int n = buf.method_10816();
      List<Keybind> out = new ArrayList(n);

      for (int i = 0; i < n; i++) {
         class_2960 id = buf.method_10810();
         String key = buf.method_19772();
         String category = buf.method_19772();
         Optional<class_2561> name = buf.readBoolean() ? Optional.of(Keybind.decodeName(buf.method_19772())) : Optional.empty();
         out.add(new Keybind(id, key, category, name));
      }

      return new SyncKeybindsS2C(out);
   }
}
