package dev.alphasucks.aea.port.advancement;

import com.google.gson.JsonObject;
import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_195;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3518;
import net.minecraft.class_4558;
import net.minecraft.class_5257;
import net.minecraft.class_5258;
import net.minecraft.class_5267;
import org.jetbrains.annotations.Nullable;

public final class PowerTrigger extends class_4558<PowerTrigger.TriggerInstance> {
   public static final class_2960 ID = Apoli.id("power");

   @Override
   public class_2960 method_794() {
      return ID;
   }

   // class_4558's own method_795 is a synthetic bridge (invisible to javac); the original class carried
   // the same bridge, delegating to the final class_4558.method_27853
   @Override
   public PowerTrigger.TriggerInstance method_795(JsonObject json, class_5257 context) {
      return super.method_27853(json, context);
   }

   @Override
   protected PowerTrigger.TriggerInstance method_27854(JsonObject json, class_5258 player, class_5257 context) {
      class_2960 power = json.has("power") ? new class_2960(class_3518.method_15265(json, "power")) : null;
      class_2960 source = json.has("source") ? new class_2960(class_3518.method_15265(json, "source")) : null;
      return new PowerTrigger.TriggerInstance(player, power, source);
   }

   public void trigger(class_3222 player, class_2960 power, class_2960 source) {
      this.method_22510(player, instance -> instance.matches(power, source));
   }

   public static final class TriggerInstance extends class_195 {
      @Nullable
      private final class_2960 power;
      @Nullable
      private final class_2960 source;

      public TriggerInstance(class_5258 player, @Nullable class_2960 power, @Nullable class_2960 source) {
         super(PowerTrigger.ID, player);
         this.power = power;
         this.source = source;
      }

      public boolean matches(class_2960 granted, class_2960 grantedFrom) {
         return this.power != null && !this.power.equals(granted) ? false : this.source == null || this.source.equals(grantedFrom);
      }

      @Override
      public JsonObject method_807(class_5267 context) {
         JsonObject json = super.method_807(context);
         if (this.power != null) {
            json.addProperty("power", this.power.toString());
         }

         if (this.source != null) {
            json.addProperty("source", this.source.toString());
         }

         return json;
      }
   }
}
