package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record RadialMenuOpenS2C(int nonce, Optional<class_2960> sprite, List<RadialMenuOpenS2C.Entry> entries) {
   public static final class_2960 CHANNEL = Apoli.internal("radial_menu_open");

   public void write(class_2540 buf) {
      buf.method_10804(this.nonce);
      buf.method_37435(this.sprite, class_2540::method_10812);
      buf.method_10804(this.entries.size());

      for (RadialMenuOpenS2C.Entry e : this.entries) {
         buf.method_10793(e.item());
         buf.method_37435(e.buttonTexture(), class_2540::method_10812);
         buf.method_37435(e.icon(), class_2540::method_10812);
         buf.method_37435(e.highlightIcon(), class_2540::method_10812);
         buf.method_37435(e.highlightButtonTexture(), class_2540::method_10812);
         buf.method_37435(e.tooltip(), class_2540::method_10805);
         buf.writeInt(e.distance());
         buf.writeInt(e.velocity());
         buf.method_10804(e.buttonWidth());
         buf.method_10804(e.buttonHeight());
         buf.method_10804(e.iconWidth());
         buf.method_10804(e.iconHeight());
         buf.method_10804(e.itemWidth());
         buf.method_10804(e.itemHeight());
         buf.writeInt(e.offsetX());
         buf.writeInt(e.offsetY());
         buf.method_37435(e.angle(), class_2540::writeFloat);
      }
   }

   public static RadialMenuOpenS2C read(class_2540 buf) {
      int nonce = buf.method_10816();
      Optional<class_2960> sprite = buf.method_37436(class_2540::method_10810);
      int n = buf.method_10816();
      List<RadialMenuOpenS2C.Entry> entries = new ArrayList(Math.min(n, 256));

      for (int i = 0; i < n; i++) {
         class_1799 item = buf.method_10819();
         Optional<class_2960> buttonTexture = buf.method_37436(class_2540::method_10810);
         Optional<class_2960> icon = buf.method_37436(class_2540::method_10810);
         Optional<class_2960> highlightIcon = buf.method_37436(class_2540::method_10810);
         Optional<class_2960> highlightButtonTexture = buf.method_37436(class_2540::method_10810);
         Optional<class_2561> tooltip = buf.method_37436(class_2540::method_10808);
         int distance = buf.readInt();
         int velocity = buf.readInt();
         int buttonWidth = buf.method_10816();
         int buttonHeight = buf.method_10816();
         int iconWidth = buf.method_10816();
         int iconHeight = buf.method_10816();
         int itemWidth = buf.method_10816();
         int itemHeight = buf.method_10816();
         int offsetX = buf.readInt();
         int offsetY = buf.readInt();
         Optional<Float> angle = buf.method_37436(class_2540::readFloat);
         entries.add(
            new RadialMenuOpenS2C.Entry(
               item,
               buttonTexture,
               icon,
               highlightIcon,
               highlightButtonTexture,
               tooltip,
               distance,
               velocity,
               buttonWidth,
               buttonHeight,
               iconWidth,
               iconHeight,
               itemWidth,
               itemHeight,
               offsetX,
               offsetY,
               angle
            )
         );
      }

      return new RadialMenuOpenS2C(nonce, sprite, entries);
   }

   public record Entry(
      class_1799 item,
      Optional<class_2960> buttonTexture,
      Optional<class_2960> icon,
      Optional<class_2960> highlightIcon,
      Optional<class_2960> highlightButtonTexture,
      Optional<class_2561> tooltip,
      int distance,
      int velocity,
      int buttonWidth,
      int buttonHeight,
      int iconWidth,
      int iconHeight,
      int itemWidth,
      int itemHeight,
      int offsetX,
      int offsetY,
      Optional<Float> angle
   ) {
   }
}
