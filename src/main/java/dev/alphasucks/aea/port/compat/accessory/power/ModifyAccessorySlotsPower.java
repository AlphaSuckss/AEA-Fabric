package dev.alphasucks.aea.port.compat.accessory.power;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.SlotModifier;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_2960;

public final class ModifyAccessorySlotsPower extends PowerType<ModifyAccessorySlotsPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_accessory_slots");
   private static final Map<class_1309, Map<class_2960, ModifyAccessorySlotsPower.Config>> APPLIED = new WeakHashMap();

   @Override
   public MapCodec<ModifyAccessorySlotsPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(SlotModifier.LIST.fieldOf("modifiers").forGetter(ModifyAccessorySlotsPower.Config::modifiers))
            .apply(i, ModifyAccessorySlotsPower.Config::new)
      );
   }

   private static Multimap<String, class_1322> build(ModifyAccessorySlotsPower.Config cfg) {
      Multimap<String, class_1322> map = HashMultimap.create();

      for (SlotModifier m : cfg.modifiers()) {
         map.put(m.slot(), m.toVanilla());
      }

      return map;
   }

   public void onAdded(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder, class_2960 source) {
      sync(powerId, cfg, holder);
   }

   public void onRemoved(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder, class_2960 source) {
      sync(powerId, cfg, holder);
   }

   public void onSuppressed(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder) {
      sync(powerId, cfg, holder);
   }

   public void onUnsuppressed(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder) {
      sync(powerId, cfg, holder);
   }

   public void tick(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder) {
      sync(powerId, cfg, holder);
   }

   private static void sync(class_2960 powerId, ModifyAccessorySlotsPower.Config cfg, PowerContainer holder) {
      class_1309 entity = holder.owner();
      if (entity != null && !entity.method_37908().method_8608()) {
         boolean wanted = holder.hasPower(powerId) && !holder.isSuppressed(powerId) && conditionHolds(entity, powerId);
         Map<class_2960, ModifyAccessorySlotsPower.Config> applied = (Map<class_2960, ModifyAccessorySlotsPower.Config>)APPLIED.get(entity);
         ModifyAccessorySlotsPower.Config current = applied == null ? null : (ModifyAccessorySlotsPower.Config)applied.get(powerId);
         if (wanted ? current != cfg : current != null) {
            if (current != null) {
               Accessories.removeSlotModifiers(entity, build(current));
            }

            if (wanted) {
               Multimap<String, class_1322> modifiers = build(cfg);
               Accessories.removeSlotModifiers(entity, modifiers);
               Accessories.applySlotModifiers(entity, modifiers);
               if (applied == null) {
                  applied = new HashMap(2);
                  APPLIED.put(entity, applied);
               }

               applied.put(powerId, cfg);
            } else {
               applied.remove(powerId);
               if (applied.isEmpty()) {
                  APPLIED.remove(entity);
               }
            }
         }
      }
   }

   private static boolean conditionHolds(class_1309 entity, class_2960 powerId) {
      Power power = ApoliPowers.get(powerId);
      return power != null && !power.condition().isEmpty()
         ? ((EntityCondition)power.condition().get()).test(EntityCtx.of(entity, entity.method_37908()))
         : true;
   }

   public record Config(List<SlotModifier> modifiers) {
   }
}
