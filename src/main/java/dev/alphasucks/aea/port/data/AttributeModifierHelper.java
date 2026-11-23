package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.data.AttributeModifierOperation.Phase;
import dev.alphasucks.aea.port.data.AttributeModifierOperation.Result;
import dev.alphasucks.aea.port.data.expr.ExprPeer;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class AttributeModifierHelper {
   private static final Comparator<AttributeModifier> ORDERING = Comparator.comparingInt((AttributeModifier m) -> m.operation().phase().ordinal())
      .thenComparingInt(m -> m.operation().order());
   private static final Comparator<AttributeModifierHelper.Owned> OWNED_ORDERING = Comparator.comparingInt((AttributeModifierHelper.Owned o) -> o.modifier().operation().phase().ordinal())
      .thenComparingInt(o -> o.modifier().operation().order());

   private AttributeModifierHelper() {
   }

   public static List<AttributeModifier> flatten(Optional<AttributeModifier> single, Optional<List<AttributeModifier>> multi) {
      if (single.isEmpty() && multi.isEmpty()) {
         return List.of();
      }

      if (single.isPresent() && multi.isEmpty()) {
         return List.of((AttributeModifier)single.get());
      }

      if (single.isEmpty()) {
         return ensureSorted((List<AttributeModifier>)multi.get());
      }

      List<AttributeModifier> combined = new ArrayList(((List)multi.get()).size() + 1);
      combined.add((AttributeModifier)single.get());
      combined.addAll((Collection)multi.get());
      return ensureSorted(combined);
   }

   public static AttributeModifier withAttribute(AttributeModifier modifier, class_2960 attribute) {
      return modifier.attribute().isPresent()
         ? modifier
         : new AttributeModifier(modifier.operation(), modifier.value(), Optional.of(attribute), modifier.name(), modifier.resource(), modifier.nested());
   }

   public static List<AttributeModifier> ensureSorted(List<AttributeModifier> mods) {
      int n = mods.size();
      if (n < 2) {
         return mods;
      }

      for (int i = 1; i < n; i++) {
         if (ORDERING.compare((AttributeModifier)mods.get(i - 1), (AttributeModifier)mods.get(i)) > 0) {
            AttributeModifier[] copy = (AttributeModifier[])mods.toArray(new AttributeModifier[0]);
            Arrays.sort(copy, ORDERING);
            return Arrays.asList(copy);
         }
      }

      return mods;
   }

   public static float apply(float baseValue, List<AttributeModifier> mods, @Nullable class_1297 entity) {
      return (float)apply((double)baseValue, mods, entity);
   }

   public static double apply(double baseValue, List<AttributeModifier> mods, @Nullable class_1297 entity) {
      if (mods.isEmpty()) {
         return baseValue;
      }

      PowerContainer container = null;
      if (entity != null) {
         int i = 0;

         for (int n = mods.size(); i < n; i++) {
            if (((AttributeModifier)mods.get(i)).needsContainer()) {
               container = PowerContainer.of(entity);
               break;
            }
         }
      }

      return apply(baseValue, mods, entity, container);
   }

   public static double apply(double baseValue, List<AttributeModifier> mods, @Nullable class_1297 entity, @Nullable PowerContainer container) {
      if (mods.isEmpty()) {
         return baseValue;
      }

      List<AttributeModifier> ordered = ensureSorted(mods);
      double base = baseValue;
      double total = baseValue;
      int i = 0;

      for (int n = ordered.size(); i < n; i++) {
         AttributeModifier mod = (AttributeModifier)ordered.get(i);
         double modValue = mod.resolveInput(entity, container, baseValue);
         Result r = mod.operation().apply(base, total, modValue);
         base = r.base();
         total = r.total();
         if (mod.operation().phase() == Phase.BASE) {
            total = base;
         }
      }

      return total;
   }

   public static double applyBounds(double baseValue, List<AttributeModifier> mods, @Nullable class_1297 entity, @Nullable PowerContainer container) {
      if (mods.isEmpty()) {
         return baseValue;
      }

      List<AttributeModifier> ordered = ensureSorted(mods);
      double base = baseValue;
      double total = baseValue;
      int i = 0;

      for (int n = ordered.size(); i < n; i++) {
         AttributeModifier mod = (AttributeModifier)ordered.get(i);
         if (mod.operation().bounds()) {
            double modValue = mod.resolveInput(entity, container, baseValue);
            Result r = mod.operation().apply(base, total, modValue);
            base = r.base();
            total = r.total();
            if (mod.operation().phase() == Phase.BASE) {
               total = base;
            }
         }
      }

      return total;
   }

   public static double apply(
      double baseValue,
      List<AttributeModifier> mods,
      @Nullable class_1297 entity,
      @Nullable PowerContainer container,
      @Nullable class_1297 actor,
      @Nullable class_1297 target
   ) {
      if (mods.isEmpty()) {
         return baseValue;
      }

      class_1297[] frame = ExprPeer.frame();
      class_1297 previousActor = frame[0];
      class_1297 previousTarget = frame[1];
      frame[0] = actor;
      frame[1] = target;

      try {
         return apply(baseValue, mods, entity, container);
      } finally {
         frame[0] = previousActor;
         frame[1] = previousTarget;
      }
   }

   public static double applyOwned(double baseValue, List<AttributeModifierHelper.Owned> mods) {
      int n = mods.size();
      if (n == 0) {
         return baseValue;
      }

      AttributeModifierHelper.Owned[] ordered = (AttributeModifierHelper.Owned[])mods.toArray(new AttributeModifierHelper.Owned[0]);
      if (n > 1) {
         Arrays.sort(ordered, OWNED_ORDERING);
      }

      double base = baseValue;
      double total = baseValue;
      class_1297[] frame = ExprPeer.frame();
      class_1297 previousActor = frame[0];
      class_1297 previousTarget = frame[1];

      try {
         for (int i = 0; i < n; i++) {
            AttributeModifierHelper.Owned owned = ordered[i];
            frame[0] = owned.actor();
            frame[1] = owned.target();
            AttributeModifier mod = owned.modifier();
            double modValue = mod.resolveInput(owned.entity(), owned.container(), baseValue);
            Result r = mod.operation().apply(base, total, modValue);
            base = r.base();
            total = r.total();
            if (mod.operation().phase() == Phase.BASE) {
               total = base;
            }
         }
      } finally {
         frame[0] = previousActor;
         frame[1] = previousTarget;
      }

      return total;
   }

   public record Owned(
      AttributeModifier modifier, @Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1297 actor, @Nullable class_1297 target
   ) {
   }
}
