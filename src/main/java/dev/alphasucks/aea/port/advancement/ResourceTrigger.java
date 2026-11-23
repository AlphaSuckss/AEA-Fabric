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
import net.minecraft.class_2096.class_2100;
import org.jetbrains.annotations.Nullable;

public final class ResourceTrigger extends class_4558<ResourceTrigger.TriggerInstance> {
   public static final class_2960 ID = Apoli.id("resource");

   @Override
   public class_2960 method_794() {
      return ID;
   }

   // class_4558's own method_795 is a synthetic bridge (invisible to javac); the original class carried
   // the same bridge, delegating to the final class_4558.method_27853
   @Override
   public ResourceTrigger.TriggerInstance method_795(JsonObject json, class_5257 context) {
      return super.method_27853(json, context);
   }

   @Override
   protected ResourceTrigger.TriggerInstance method_27854(JsonObject json, class_5258 player, class_5257 context) {
      class_2960 resource = json.has("resource") ? new class_2960(class_3518.method_15265(json, "resource")) : null;
      return new ResourceTrigger.TriggerInstance(player, resource, class_2100.method_9056(json.get("value")));
   }

   public void trigger(class_3222 player, class_2960 resource, int value) {
      this.method_22510(player, instance -> instance.matches(resource, value));
   }

   public static final class TriggerInstance extends class_195 {
      @Nullable
      private final class_2960 resource;
      private final class_2100 value;

      public TriggerInstance(class_5258 player, @Nullable class_2960 resource, class_2100 value) {
         super(ResourceTrigger.ID, player);
         this.resource = resource;
         this.value = value;
      }

      public boolean matches(class_2960 changed, int current) {
         return this.resource != null && !this.resource.equals(changed) ? false : this.value.method_9054(current);
      }

      @Override
      public JsonObject method_807(class_5267 context) {
         JsonObject json = super.method_807(context);
         if (this.resource != null) {
            json.addProperty("resource", this.resource.toString());
         }

         json.add("value", this.value.method_9036());
         return json;
      }
   }
}
