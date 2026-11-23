package dev.alphasucks.aea.port.effects;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1291;
import net.minecraft.class_1309;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_4081;
import org.jetbrains.annotations.NotNull;

public final class CustomMobEffect extends class_1291 {
   public final class_2960 id;
   /** Power source used for the powers this effect grants: {@code apoli:effect/<namespace>/<path>}. */
   public final class_2960 source;
   public volatile Optional<String> name;
   public volatile Optional<class_2960> icon;
   private volatile class_4081 category;
   private volatile int color;
   private volatile List<class_2960> powers;
   private volatile String descriptionId;

   CustomMobEffect(class_2960 id, class_4081 category, int color, List<class_2960> powers, Optional<String> name, Optional<class_2960> icon) {
      super(category, color);
      this.id = id;
      this.source = Apoli.id("effect/" + id.method_12836() + "/" + id.method_12832());
      this.category = category;
      this.color = color;
      this.powers = List.copyOf(powers);
      this.name = name;
      this.icon = icon;
      this.descriptionId = (String)name.orElseGet(() -> class_156.method_646("effect", id));
   }

   public void copyFrom(CustomMobEffect source) {
      this.category = source.category;
      this.color = source.color;
      this.powers = source.powers;
      this.name = source.name;
      this.icon = source.icon;
      this.descriptionId = source.descriptionId;
   }

   public List<class_2960> powers() {
      return this.powers;
   }

   @NotNull
   @Override
   public class_4081 method_18792() {
      return this.category;
   }

   @Override
   public int method_5556() {
      return this.color;
   }

   @Override
   public boolean method_5573() {
      return this.category == class_4081.field_18271;
   }

   public void grantPowers(class_1309 entity) {
      List<class_2960> granted = this.powers;
      if (!granted.isEmpty() && !entity.method_37908().method_8608()) {
         PowerContainer holder = null;

         for (int i = 0; i < granted.size(); i++) {
            class_2960 power = (class_2960)granted.get(i);
            if (ApoliPowers.get(power) != null) {
               if (holder == null) {
                  holder = PowerContainerAttachment.getOrCreate(entity);
                  if (holder == null) {
                     return;
                  }
               }

               holder.addPower(power, this.source);
            }
         }
      }
   }

   public void revokePowers(PowerContainer holder) {
      holder.removeAllFromSource(this.source);
      List<class_2960> granted = this.powers;

      for (int i = 0; i < granted.size(); i++) {
         holder.removePower((class_2960)granted.get(i), this.id);
      }
   }

   @NotNull
   @Override
   public String method_5567() {
      return this.descriptionId;
   }

   // AEA: vanilla's lazy description id asks the registry for this effect's key; answer directly so it also
   // works while the effect is between registrations.
   @NotNull
   @Override
   protected String method_5559() {
      return this.descriptionId;
   }
}
