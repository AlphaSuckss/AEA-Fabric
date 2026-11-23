package dev.alphasucks.aea.port.mixin.trade;

import dev.alphasucks.aea.port.power.builtin.ExtraTradesPower;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1657;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3988;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3988.class)
public abstract class AbstractVillagerExtraTradesMixin {
   @Shadow
   @Nullable
   protected class_1916 field_17721;
   @Unique
   private Map<class_2960, class_1916> apoli$rolledTrades = Map.of();
   @Unique
   private List<class_1914> apoli$shownTrades = List.of();

   @Shadow
   public abstract class_1916 method_8264();

   @Inject(method = "method_8259", at = @At("HEAD"))
   private void apoli$showExtraTrades(@Nullable class_1657 player, CallbackInfo ci) {
      class_3988 self = (class_3988)(Object)this;
      this.apoli$hideExtraTrades();
      if (player != null && !self.method_37908().method_8608()) {
         if (apoli$rolled().isEmpty()) {
            this.apoli$rolledTrades = new LinkedHashMap();
         }

         List<class_1914> extra = ExtraTradesPower.offersFor(self, player, apoli$rolled());
         if (!extra.isEmpty()) {
            this.method_8264().addAll(extra);
            this.apoli$shownTrades = extra;
         }
      }
   }

   @Inject(method = "method_5652", at = @At("HEAD"))
   private void apoli$saveWithoutExtraTrades(class_2487 tag, CallbackInfo ci) {
      if (!apoli$shown().isEmpty() && this.field_17721 != null) {
         this.field_17721.removeAll(apoli$shown());
      }
   }

   @Inject(method = "method_5652", at = @At("RETURN"))
   private void apoli$saveExtraTrades(class_2487 tag, CallbackInfo ci) {
      if (!apoli$shown().isEmpty() && this.field_17721 != null) {
         this.field_17721.addAll(apoli$shown());
      }

      ExtraTradesPower.save((class_3988)(Object)this, apoli$rolled(), tag);
   }

   @Inject(method = "method_5749", at = @At("RETURN"))
   private void apoli$loadExtraTrades(class_2487 tag, CallbackInfo ci) {
      Map<class_2960, class_1916> loaded = ExtraTradesPower.load((class_3988)(Object)this, tag);
      if (!loaded.isEmpty()) {
         this.apoli$rolledTrades = loaded;
      }
   }

   @Unique
   private void apoli$hideExtraTrades() {
      if (!apoli$shown().isEmpty()) {
         if (this.field_17721 != null) {
            this.field_17721.removeAll(apoli$shown());
         }

         this.apoli$shownTrades = List.of();
      }
   }

   // AEA: the field initialisers don't run for merchants created without the normal constructor path (seen during
   // structure generation in a large pack: "Saving entity NBT" NPE in ExtraTradesPower.save) - treat null as empty.
   @Unique
   private Map<class_2960, class_1916> apoli$rolled() {
      if (this.apoli$rolledTrades == null) {
         this.apoli$rolledTrades = Map.of();
      }
      return this.apoli$rolledTrades;
   }

   @Unique
   private List<class_1914> apoli$shown() {
      if (this.apoli$shownTrades == null) {
         this.apoli$shownTrades = List.of();
      }
      return this.apoli$shownTrades;
   }
}
