package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1310;
import net.minecraft.class_2960;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;

public final class ModifyTypeTagPower extends PowerType<ModifyTypeTagPower.Cfg> {
   private static final Codec<class_6862<class_1299<?>>> TAG_CODEC = IdCodecs.tagKey(class_7924.field_41266);
   private static final Codec<List<class_6862<class_1299<?>>>> TAGS_CODEC = Codec.either(TAG_CODEC, TAG_CODEC.listOf())
      .xmap(either -> (List)either.map(List::of, list -> list), list -> list.size() == 1 ? Either.left((class_6862)list.get(0)) : Either.right(list));
   private static final ThreadLocal<class_1297> LINKED = new ThreadLocal();
   private static volatile boolean active = false;
   private static volatile int activeGeneration = -1;
   private static final List<class_6862<class_1299<?>>> LEGACY_GROUPS = List.of(
      class_6862.method_40092(class_7924.field_41266, new class_2960("undead")),
      class_6862.method_40092(class_7924.field_41266, new class_2960("arthropod")),
      class_6862.method_40092(class_7924.field_41266, new class_2960("illager")),
      class_6862.method_40092(class_7924.field_41266, new class_2960("aquatic"))
   );
   private static final List<class_1310> LEGACY_MOB_TYPES = List.of(class_1310.field_6289, class_1310.field_6293, class_1310.field_6291, class_1310.field_6292);

   @Override
   public MapCodec<ModifyTypeTagPower.Cfg> configCodec() {
      return RecordCodecBuilder.<ModifyTypeTagPower.Raw>mapCodec(
            i -> i.group(
                  TAGS_CODEC.optionalFieldOf("tag").forGetter(ModifyTypeTagPower.Raw::tags),
                  Codec.STRING.optionalFieldOf("group").forGetter(ModifyTypeTagPower.Raw::group),
                  Codec.BOOL.optionalFieldOf("included", true).forGetter(ModifyTypeTagPower.Raw::included)
               )
               .apply(i, ModifyTypeTagPower.Raw::new)
         )
         .flatXmap(ModifyTypeTagPower::fromRaw, ModifyTypeTagPower::toRaw);
   }

   private static DataResult<ModifyTypeTagPower.Cfg> fromRaw(ModifyTypeTagPower.Raw raw) {
      if (raw.tags().isPresent()) {
         return DataResult.success(new ModifyTypeTagPower.Cfg((List<class_6862<class_1299<?>>>)raw.tags().get(), raw.included()));
      }

      if (raw.group().isPresent()) {
         String group = ((String)raw.group().get()).toLowerCase(Locale.ROOT);
         if ("default".equals(group)) {
            return DataResult.success(new ModifyTypeTagPower.Cfg(LEGACY_GROUPS, false));
         }

         for (class_6862<class_1299<?>> tag : LEGACY_GROUPS) {
            if (tag.comp_327().method_12832().equals(group)) {
               return DataResult.success(new ModifyTypeTagPower.Cfg(List.of(tag), raw.included()));
            }
         }

         return DataResult.error(() -> "Unknown legacy entity group: " + (String)raw.group().get());
      } else {
         return DataResult.error(() -> "apoli:modify_type_tag needs a \"tag\"");
      }
   }

   private static DataResult<ModifyTypeTagPower.Raw> toRaw(ModifyTypeTagPower.Cfg cfg) {
      return DataResult.success(new ModifyTypeTagPower.Raw(Optional.of(cfg.tags()), Optional.empty(), cfg.included()));
   }

   public static boolean active() {
      int generation = ApoliPowers.generation();
      if (generation != activeGeneration) {
         active = ApoliPowers.anyOfType(ApoliIds.MODIFY_TYPE_TAG);
         activeGeneration = generation;
      }

      return active;
   }

   public static void link(class_1297 entity) {
      LINKED.set(entity);
   }

   public static void unlink() {
      LINKED.remove();
   }

   public static boolean resolve(boolean original, class_1299<?> queried, class_6862<class_1299<?>> tag) {
      class_1297 entity = (class_1297)LINKED.get();
      return entity != null && entity.method_5864() == queried ? resolve(entity, original, tag) : original;
   }

   public static class_1310 resolveMobType(class_1309 entity, class_1310 original) {
      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         if (container.powersOfType(ApoliIds.MODIFY_TYPE_TAG).isEmpty()) {
            return original;
         }

         for (int i = 0; i < LEGACY_GROUPS.size(); i++) {
            class_6862<class_1299<?>> tag = (class_6862<class_1299<?>>)LEGACY_GROUPS.get(i);
            class_1310 mapped = (class_1310)LEGACY_MOB_TYPES.get(i);
            boolean vanilla = original == mapped;
            if (resolve(entity, vanilla, tag) != vanilla) {
               return vanilla ? class_1310.field_6290 : mapped;
            }
         }

         return original;
      } else {
         return original;
      }
   }

   public static boolean resolve(@Nullable class_1297 entity, boolean original, class_6862<class_1299<?>> tag) {
      if (entity == null) {
         return original;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.MODIFY_TYPE_TAG);
         if (powers.isEmpty()) {
            return original;
         }

         EntityCtx ctx = null;
         boolean included = false;

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof ModifyTypeTagPower.Cfg cfg && cfg.names(tag)) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  if (!cfg.included()) {
                     return false;
                  }

                  included = true;
               }
            }
         }

         return included || original;
      } else {
         return original;
      }
   }

   public record Cfg(List<class_6862<class_1299<?>>> tags, boolean included) {
      public Cfg {
         tags = List.copyOf(tags);
      }

      public boolean names(class_6862<class_1299<?>> tag) {
         for (int i = 0; i < this.tags.size(); i++) {
            if (((class_6862)this.tags.get(i)).equals(tag)) {
               return true;
            }
         }

         return false;
      }
   }

   private record Raw(Optional<List<class_6862<class_1299<?>>>> tags, Optional<String> group, boolean included) {
   }
}
