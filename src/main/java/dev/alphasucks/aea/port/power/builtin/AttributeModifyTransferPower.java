package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.access.ModifierSummaryHolder;
import dev.alphasucks.aea.port.alias.NamespaceAlias;
import dev.alphasucks.aea.port.attribute.ApoliAttributes;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class AttributeModifyTransferPower extends PowerType<AttributeModifyTransferPower.Config> {
   private static final Map<class_2960, AttributeModifyTransferPower.Target> TARGETS = new LinkedHashMap();
   public static final AttributeModifyTransferPower.Target AIR_SPEED = target(ApoliIds.MODIFY_AIR_SPEED);
   public static final AttributeModifyTransferPower.Target SWIM_SPEED = target(ApoliIds.MODIFY_SWIM_SPEED);
   public static final AttributeModifyTransferPower.Target JUMP = target(ApoliIds.MODIFY_JUMP);
   public static final AttributeModifyTransferPower.Target SLIPPERINESS = target(ApoliIds.MODIFY_SLIPPERINESS);
   public static final AttributeModifyTransferPower.Target VELOCITY = target(ApoliIds.MODIFY_VELOCITY);
   public static final AttributeModifyTransferPower.Target FALLING = target(ApoliIds.MODIFY_FALLING);
   public static final AttributeModifyTransferPower.Target BREAK_SPEED = target(ApoliIds.MODIFY_BREAK_SPEED);
   public static final AttributeModifyTransferPower.Target EXHAUSTION = target(ApoliIds.MODIFY_EXHAUSTION);
   public static final AttributeModifyTransferPower.Target HEALING = target(ApoliIds.MODIFY_HEALING);
   public static final AttributeModifyTransferPower.Target DAMAGE_DEALT = target(Apoli.id("modify_damage_dealt"));
   public static final AttributeModifyTransferPower.Target DAMAGE_TAKEN = target(Apoli.id("modify_damage_taken"));
   public static final AttributeModifyTransferPower.Target PROJECTILE_DAMAGE = target(Apoli.id("modify_projectile_damage"));
   public static final AttributeModifyTransferPower.Target STATUS_EFFECT_AMPLIFIER = target(ApoliIds.MODIFY_STATUS_EFFECT_AMPLIFIER);
   public static final AttributeModifyTransferPower.Target STATUS_EFFECT_DURATION = target(ApoliIds.MODIFY_STATUS_EFFECT_DURATION);
   public static final AttributeModifyTransferPower.Target FOV = target(Apoli.id("modify_fov"));
   private static final Codec<AttributeModifyTransferPower.Target> TARGET_CODEC = Codec.STRING
      .comapFlatMap(AttributeModifyTransferPower::readTarget, target -> target.id().toString())
      .stable();
   private static final Codec<class_1320> ATTRIBUTE_CODEC = IdCodecs.ID
      .comapFlatMap(AttributeModifyTransferPower::readAttribute, attribute -> class_7923.field_41190.method_10221(attribute))
      .stable();
   public static final MapCodec<AttributeModifyTransferPower.Config> CONFIG_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            TARGET_CODEC.fieldOf("class").forGetter(AttributeModifyTransferPower.Config::target),
            ATTRIBUTE_CODEC.fieldOf("attribute").forGetter(AttributeModifyTransferPower.Config::attribute),
            Codec.DOUBLE.optionalFieldOf("multiplier", 1.0).forGetter(AttributeModifyTransferPower.Config::multiplier)
         )
         .apply(i, AttributeModifyTransferPower.Config::new)
   );

   @Override
   public MapCodec<AttributeModifyTransferPower.Config> configCodec() {
      return CONFIG_CODEC;
   }

   private static AttributeModifyTransferPower.Target target(class_2960 id) {
      AttributeModifyTransferPower.Target target = new AttributeModifyTransferPower.Target(id);
      TARGETS.put(id, target);
      return target;
   }

   private static DataResult<AttributeModifyTransferPower.Target> readTarget(String raw) {
      return IdCodecs.readId(raw.indexOf(58) < 0 ? "apoli:" + raw : raw)
         .flatMap(
            id -> {
               class_2960 named = NamespaceAlias.hasAlias(id.method_12836()) ? NamespaceAlias.resolve(id) : id;
               AttributeModifyTransferPower.Target target = (AttributeModifyTransferPower.Target)TARGETS.get(named);
               if (target == null) {
                  target = (AttributeModifyTransferPower.Target)TARGETS.get(PowerTypeRegistry.resolveId(named));
               }

               if (target != null) {
                  return DataResult.success(target);
               }

               String known = String.join(", ", TARGETS.keySet().stream().map(class_2960::toString).toList());
               return PowerTypeRegistry.get(named) != null
                  ? DataResult.error(() -> "'" + raw + "' does not take transferred attribute modifiers. 'class' must be one of: " + known)
                  : DataResult.error(() -> "Unknown power type '" + raw + "' in 'class'. It must be one of: " + known);
            }
         );
   }

   private static DataResult<class_1320> readAttribute(class_2960 id) {
      class_1320 attribute = ApoliAttributes.get(id);
      return attribute == null ? DataResult.error(() -> "Unknown attribute '" + id + "'") : DataResult.success(attribute);
   }

   private static boolean targetedByAnyPower(AttributeModifyTransferPower.Target target) {
      if (!ApoliPowers.anyOfType(ApoliIds.ATTRIBUTE_MODIFY_TRANSFER)) {
         return false;
      }

      for (Power power : ApoliPowers.view().values()) {
         if (power.config() instanceof AttributeModifyTransferPower.Config cfg && cfg.target() == target) {
            return true;
         }
      }

      return false;
   }

   private static List<AttributeModifier> transferred(class_1309 entity, AttributeModifyTransferPower.Config cfg) {
      return entity.method_6127().method_26842(cfg.attribute()) instanceof ModifierSummaryHolder holder
         ? holder.apoli$modifierSummary().modifiers(cfg.multiplier())
         : List.of();
   }

   public record Config(AttributeModifyTransferPower.Target target, class_1320 attribute, double multiplier) {
   }

   public static final class Target {
      private final class_2960 id;
      private volatile int inUse = -2;

      private Target(class_2960 id) {
         this.id = id;
      }

      public class_2960 id() {
         return this.id;
      }

      public boolean appliesTo(@Nullable PowerContainer container) {
         return container != null && this.inUse() && !container.powersOfType(ApoliIds.ATTRIBUTE_MODIFY_TRANSFER).isEmpty();
      }

      public boolean inUse() {
         int generation = ApoliPowers.generation();
         int cached = this.inUse;
         if (cached >> 1 == generation) {
            return (cached & 1) != 0;
         }

         boolean used = AttributeModifyTransferPower.targetedByAnyPower(this);
         this.inUse = generation << 1 | (used ? 1 : 0);
         return used;
      }

      public List<AttributeModifier> merge(class_1309 entity, PowerContainer container, List<AttributeModifier> own) {
         List<class_2960> transfers = container.powersOfType(ApoliIds.ATTRIBUTE_MODIFY_TRANSFER);
         List<AttributeModifier> result = own;
         boolean copied = false;
         EntityCtx ctx = null;
         int i = 0;

         for (int n = transfers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)transfers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof AttributeModifyTransferPower.Config cfg && cfg.target() == this) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  List<AttributeModifier> modifiers = AttributeModifyTransferPower.transferred(entity, cfg);
                  if (!modifiers.isEmpty()) {
                     if (result.isEmpty()) {
                        result = modifiers;
                     } else {
                        if (!copied) {
                           result = new ArrayList(result);
                           copied = true;
                        }

                        result.addAll(modifiers);
                     }
                  }
               }
            }
         }

         return result;
      }
   }
}
