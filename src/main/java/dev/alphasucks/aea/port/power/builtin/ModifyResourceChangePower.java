package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

public final class ModifyResourceChangePower extends PowerType<ModifyResourceChangePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_resource_change");
   private static volatile int cachedGeneration = -1;
   private static volatile boolean cachedInUse;
   private static volatile class_2960[] cooldownTypes = new class_2960[0];
   private static boolean applying;
   private final Map<ModifyResourceChangePower.CarryKey, Float> carry = new HashMap();

   @Override
   public MapCodec<ModifyResourceChangePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyResourceChangePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyResourceChangePower.Config::modifiers),
               ModifyResourceChangePower.Direction.CODEC
                  .optionalFieldOf("modify", ModifyResourceChangePower.Direction.BOTH)
                  .forGetter(ModifyResourceChangePower.Config::modify),
               Codec.BOOL.optionalFieldOf("resources", true).forGetter(ModifyResourceChangePower.Config::resources),
               Codec.BOOL.optionalFieldOf("cooldowns", true).forGetter(ModifyResourceChangePower.Config::cooldowns),
               IdCodecs.ID.optionalFieldOf("resource").forGetter(ModifyResourceChangePower.Config::resource)
            )
            .apply(i, ModifyResourceChangePower.Config::new)
      );
   }

   public static boolean inUse() {
      int generation = ApoliPowers.generation();
      if (generation != cachedGeneration) {
         cachedInUse = ApoliPowers.anyOfType(CANONICAL);
         cachedGeneration = generation;
      }

      return cachedInUse;
   }

   private static class_2960[] cooldownTypes() {
      class_2960[] known = cooldownTypes;
      if (known.length > 0) {
         return known;
      }

      List<class_2960> found = new ArrayList();

      for (Entry<class_2960, PowerType<?>> entry : PowerTypeRegistry.view().entrySet()) {
         if (((PowerType)entry.getValue()).isCooldown()) {
            found.add((class_2960)entry.getKey());
         }
      }

      class_2960[] built = (class_2960[])found.toArray(new class_2960[0]);
      cooldownTypes = built;
      return built;
   }

   public static int adjust(@Nullable PowerContainer container, class_2960 target, int current, int proposed) {
      if (applying || current == proposed) {
         return proposed;
      }

      if (container != null && inUse()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         if (powers.isEmpty()) {
            return proposed;
         }

         class_1297 owner = container.rawOwner();
         if (owner != null && !owner.method_37908().method_8608()) {
            boolean cooldown = isCooldownPower(target);
            boolean gaining = proposed > current;
            double magnitude = Math.abs((double)proposed - current);
            double scaled = magnitude;
            EntityCtx ctx = null;
            int i = 0;

            for (int n = powers.size(); i < n; i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (!container.isSuppressed(powerId)) {
                  Power power = ApoliPowers.get(powerId);
                  if (power != null
                     && power.config() instanceof ModifyResourceChangePower.Config cfg
                     && (cooldown ? cfg.cooldowns() : cfg.resources())
                     && cfg.modify().covers(gaining)
                     && (!cfg.resource().isPresent() || ((class_2960)cfg.resource().get()).equals(target))) {
                     List<AttributeModifier> mods = cfg.flattened();
                     if (!mods.isEmpty()) {
                        if (power.condition().isPresent()) {
                           if (ctx == null) {
                              ctx = EntityCtx.of(owner, owner.method_37908());
                           }

                           if (!((EntityCondition)power.condition().get()).test(ctx)) {
                              continue;
                           }
                        }

                        scaled = AttributeModifierHelper.apply(scaled, mods, owner, container);
                     }
                  }
               }
            }

            if (scaled == magnitude) {
               return proposed;
            }

            long shifted = Math.round(gaining ? current + scaled : current - scaled);
            return (int)Math.max(-2147483648L, Math.min(2147483647L, shifted));
         } else {
            return proposed;
         }
      } else {
         return proposed;
      }
   }

   private static boolean isCooldownPower(class_2960 powerId) {
      Power loaded = ApoliPowers.get(powerId);
      if (loaded == null) {
         return false;
      }

      PowerType<?> type = PowerTypeRegistry.get(loaded.typeId());
      return type != null && type.isCooldown();
   }

   public void tick(class_2960 powerId, ModifyResourceChangePower.Config cfg, PowerContainer holder) {
      if (cfg.cooldowns()) {
         if (powerId.equals(cooldownDriver(holder))) {
            class_1297 owner = holder.rawOwner();
            if (owner != null && !owner.method_37908().method_8608()) {
               class_2960[] types = cooldownTypes();

               for (int t = 0; t < types.length; t++) {
                  List<class_2960> cooldowns = holder.powersOfType(types[t]);
                  int i = 0;

                  for (int n = cooldowns.size(); i < n; i++) {
                     this.accelerate(holder, owner.method_5667(), (class_2960)cooldowns.get(i));
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static class_2960 cooldownDriver(PowerContainer holder) {
      List<class_2960> powers = holder.powersOfType(CANONICAL);
      int i = 0;

      for (int n = powers.size(); i < n; i++) {
         class_2960 candidate = (class_2960)powers.get(i);
         if (!holder.isSuppressed(candidate)) {
            Power power = ApoliPowers.get(candidate);
            if (power != null && power.config() instanceof ModifyResourceChangePower.Config cfg && cfg.cooldowns()) {
               return candidate;
            }
         }
      }

      return null;
   }

   private void accelerate(PowerContainer holder, UUID entity, class_2960 cooldownId) {
      if (!holder.isSuppressed(cooldownId)) {
         OptionalInt remaining = PowerResources.read(holder, cooldownId);
         if (!remaining.isEmpty()) {
            int value = remaining.getAsInt();
            ModifyResourceChangePower.CarryKey key = new ModifyResourceChangePower.CarryKey(entity, cooldownId);
            if (value <= 0) {
               this.carry.remove(key);
            } else {
               int stepped = adjust(holder, cooldownId, value, value - 1);
               float extra = value - 1 - stepped + (Float)this.carry.getOrDefault(key, 0.0F);
               int whole = (int)extra;
               float fraction = extra - whole;
               if (fraction < 0.0F) {
                  whole--;
                  fraction++;
               }

               this.carry.put(key, fraction);
               if (whole != 0) {
                  applying = true;

                  try {
                     PowerResources.write(holder, cooldownId, Math.max(0, value - whole));
                  } finally {
                     applying = false;
                  }
               }
            }
         }
      }
   }

   public void onRemoved(class_2960 powerId, ModifyResourceChangePower.Config cfg, PowerContainer holder, class_2960 source) {
      if (!holder.allPowers().contains(powerId)) {
         UUID owner = holder.rawOwner() == null ? null : holder.rawOwner().method_5667();
         if (owner != null) {
            this.carry.keySet().removeIf(key -> key.entity().equals(owner));
         }
      }
   }

   private record CarryKey(UUID entity, class_2960 power) {
   }

   public record Config(
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      ModifyResourceChangePower.Direction modify,
      boolean resources,
      boolean cooldowns,
      Optional<class_2960> resource
   ) {
      public List<AttributeModifier> flattened() {
         return AttributeModifierHelper.flatten(this.modifier, this.modifiers);
      }
   }

   public enum Direction implements class_3542 {
      GAIN("gain"),
      DRAIN("drain"),
      BOTH("both");

      public static final Codec<ModifyResourceChangePower.Direction> CODEC = class_3542.method_28140(ModifyResourceChangePower.Direction::values);
      private final String name;

      Direction(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }

      boolean covers(boolean gaining) {
         return this == BOTH || (gaining ? this == GAIN : this == DRAIN);
      }
   }
}
