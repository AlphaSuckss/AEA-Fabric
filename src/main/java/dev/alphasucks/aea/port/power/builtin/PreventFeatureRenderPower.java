package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class PreventFeatureRenderPower extends PowerType<PreventFeatureRenderPower.Config> {
   private static final String[] ALL = new String[0];

   @Override
   public MapCodec<PreventFeatureRenderPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("feature").forGetter(c -> Optional.empty()),
               Codec.STRING.listOf().optionalFieldOf("features").forGetter(c -> Optional.of(List.of(c.features()))),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(PreventFeatureRenderPower.Config::bientityCondition)
            )
            .apply(i, (feature, features, bientityCondition) -> {
               Set<String> merged = new LinkedHashSet();
               features.ifPresent(merged::addAll);
               feature.ifPresent(merged::add);
               return new PreventFeatureRenderPower.Config(merged.isEmpty() ? ALL : (String[])merged.toArray(String[]::new), bientityCondition);
            })
      );
   }

   public static boolean prevents(@Nullable class_1297 entity, String[] layerKeys, @Nullable class_1297 viewer) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.PREVENT_FEATURE_RENDER);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         BiEntityCtx biCtx = null;
         int i = 0;

         while (true) {
            if (i >= powers.size()) {
               return false;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               label54:
               if (power != null && power.config() instanceof PreventFeatureRenderPower.Config cfg && cfg.matches(layerKeys)) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        break label54;
                     }
                  }

                  if (!cfg.bientityCondition().isPresent()) {
                     break;
                  }

                  if (viewer != null) {
                     if (biCtx == null) {
                        biCtx = new BiEntityCtx(viewer, entity, entity.method_37908());
                     }

                     if (((BiEntityCondition)cfg.bientityCondition().get()).test(biCtx)) {
                        break;
                     }
                  }
               }
            }

            i++;
         }

         return true;
      } else {
         return false;
      }
   }

   public record Config(String[] features, Optional<BiEntityCondition> bientityCondition) {
      public boolean matches(String[] layerKeys) {
         if (this.features.length == 0) {
            return true;
         }

         for (int i = 0; i < layerKeys.length; i++) {
            String key = layerKeys[i];

            for (int j = 0; j < this.features.length; j++) {
               if (this.features[j].equals(key)) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}
