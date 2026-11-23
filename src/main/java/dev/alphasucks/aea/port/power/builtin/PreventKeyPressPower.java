package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

public final class PreventKeyPressPower extends PowerType<PreventKeyPressPower.Config> {
   private static final ThreadLocal<boolean[]> REENTRY = ThreadLocal.withInitial(() -> new boolean[1]);

   @Override
   public MapCodec<PreventKeyPressPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.list(Key.CODEC).optionalFieldOf("keys").forGetter(PreventKeyPressPower.Config::keys),
               Codec.BOOL.optionalFieldOf("scroll").forGetter(PreventKeyPressPower.Config::scroll),
               Codec.BOOL.optionalFieldOf("affect_forced", false).forGetter(PreventKeyPressPower.Config::affectForced),
               Codec.BOOL.optionalFieldOf("unpress", true).forGetter(PreventKeyPressPower.Config::unpress)
            )
            .apply(i, PreventKeyPressPower.Config::new)
      );
   }

   public static boolean any(@Nullable class_1297 entity) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() ? !container.powersOfType(ApoliIds.PREVENT_KEY_PRESS).isEmpty() : false;
   }

   public static void forEachActive(@Nullable class_1297 entity, Consumer<PreventKeyPressPower.Config> consumer) {
      if (any(entity)) {
         boolean[] guard = (boolean[])REENTRY.get();
         if (!guard[0]) {
            guard[0] = true;

            try {
               PowerLookup.forEach(entity, ApoliIds.PREVENT_KEY_PRESS, PreventKeyPressPower.Config.class, consumer);
            } finally {
               guard[0] = false;
            }
         }
      }
   }

   public static boolean blocks(@Nullable class_1297 entity, String key) {
      return scan(entity, cfg -> cfg.blocks(key));
   }

   public static boolean blocksForcedKeys(@Nullable class_1297 entity, String key) {
      return scan(entity, cfg -> cfg.affectForced() && cfg.blocks(key));
   }

   private static boolean scan(@Nullable class_1297 entity, Predicate<PreventKeyPressPower.Config> test) {
      boolean[] hit = new boolean[]{false};
      forEachActive(entity, cfg -> {
         if (!hit[0] && test.test(cfg)) {
            hit[0] = true;
         }
      });
      return hit[0];
   }

   public record Config(Optional<List<Key>> keys, Optional<Boolean> scroll, boolean affectForced, boolean unpress) {
      public boolean blocksEverything() {
         return this.keys.isEmpty();
      }

      public boolean blocksScroll() {
         return (Boolean)this.scroll.orElseGet(this::blocksEverything);
      }

      public boolean blocks(String name) {
         if (this.keys.isEmpty()) {
            return true;
         }

         List<Key> list = (List<Key>)this.keys.get();

         for (int i = 0; i < list.size(); i++) {
            if (((Key)list.get(i)).key().equals(name)) {
               return true;
            }
         }

         return false;
      }
   }
}
