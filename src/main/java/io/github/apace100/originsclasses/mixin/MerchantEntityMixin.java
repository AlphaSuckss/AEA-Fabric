package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import io.github.apace100.originsclasses.util.ClassesTags;
import io.github.apace100.originsclasses.util.ItemUtil;
import io.github.apace100.originsclasses.util.TagUtil;
import java.util.Set;
import net.minecraft.class_1296;
import net.minecraft.class_1299;
import net.minecraft.class_1657;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_1937;
import net.minecraft.class_2487;
import net.minecraft.class_3988;
import net.minecraft.class_3989;
import net.minecraft.class_5819;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3988.class)
public abstract class MerchantEntityMixin extends class_1296 {
   @Shadow
   protected class_1916 field_17721;
   @Shadow
   private class_1657 field_17722;
   private int offerCountWithoutAdditional;
   private class_1916 additionalOffers;

   protected MerchantEntityMixin(class_1299<? extends class_1296> entityType, class_1937 world) {
      super(entityType, world);
   }

   // AEA: @WrapOperation instead of @Redirect
   @WrapOperation(method = "method_8262(Lnet/minecraft/class_1914;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1914;method_8244()V"))
   private void dontUseUpTrades(class_1914 tradeOffer, Operation<Void> original) {
      if ((Object)this instanceof class_3989 || !ClassPowerTypes.TRADE_AVAILABILITY.isActive(this.field_17722)) {
         original.call(tradeOffer);
      }
   }

   @Inject(method = "method_8259(Lnet/minecraft/class_1657;)V", at = @At("HEAD"))
   private void addAdditionalOffers(class_1657 customer, CallbackInfo ci) {
      // AEA: always strip previously added extra offers first, so re-opening (or saving mid-trade)
      // can't stack duplicate copies; also tolerate offers not being generated yet.
      if ((Object)this instanceof class_3989 && this.field_17721 != null) {
         if (this.additionalOffers != null) {
            while (this.field_17721.size() > this.offerCountWithoutAdditional) {
               this.field_17721.remove(this.field_17721.size() - 1);
            }
         }

         if (ClassPowerTypes.RARE_WANDERING_LOOT.isActive(customer)) {
            if (this.additionalOffers == null) {
               this.offerCountWithoutAdditional = this.field_17721.size();
               this.additionalOffers = this.buildAdditionalOffers();
            }

            if (this.additionalOffers != null) {
               this.field_17721.addAll(this.additionalOffers);
            }
         }
      }
   }

   @Inject(method = "method_5652(Lnet/minecraft/class_2487;)V", at = @At("HEAD"))
   private void writeAdditionalOffersToTag(class_2487 tag, CallbackInfo ci) {
      if (this.additionalOffers != null) {
         tag.method_10566("AdditionalOffers", this.additionalOffers.method_8268());
         tag.method_10569("OfferCountNoAdditional", this.offerCountWithoutAdditional);
      }
   }

   @Inject(method = "method_5749(Lnet/minecraft/class_2487;)V", at = @At("HEAD"))
   private void readAdditionalOffersFromTag(class_2487 tag, CallbackInfo ci) {
      if (tag.method_10545("AdditionalOffers")) {
         this.additionalOffers = new class_1916(tag.method_10562("AdditionalOffers"));
         this.offerCountWithoutAdditional = tag.method_10550("OfferCountNoAdditional");
      }
   }

   private class_1916 buildAdditionalOffers() {
      class_1916 list = new class_1916();
      class_5819 random = this.method_6051();
      Set<class_1792> excludedItems = TagUtil.getAllEntries(class_7923.field_41178, ClassesTags.MERCHANT_BLACKLIST);
      // AEA: getRandomObtainableItem returns null when nothing is left after the blacklist; skip the offer
      // instead of crashing the server when a pack blacklists (or strips loot tables down to) everything.
      class_1792 sold = ItemUtil.getRandomObtainableItem(this.method_37908().method_8503(), random, excludedItems);
      if (sold != null) {
         list.add(new class_1914(new class_1799(class_1802.field_8687, random.method_43048(12) + 6), ItemUtil.createMerchantItemStack(sold, random), 1, 5, 0.05F));
      }

      class_1792 desiredItem = ItemUtil.getRandomObtainableItem(this.method_37908().method_8503(), random, excludedItems);
      class_1792 sold2 = ItemUtil.getRandomObtainableItem(this.method_37908().method_8503(), random, excludedItems);
      if (desiredItem != null && sold2 != null) {
         list.add(
            new class_1914(
               new class_1799(desiredItem, 1 + random.method_43048(Math.max(1, Math.min(16, desiredItem.method_7882())))),
               ItemUtil.createMerchantItemStack(sold2, random),
               1,
               5,
               0.05F
            )
         );
      }

      return list;
   }
}
