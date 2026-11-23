package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Key;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.keybind.KeyDispatch;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower.Config;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class RunStoredPowerAction implements ActionType<EntityCtx, RunStoredPowerAction.Cfg> {
   private static final int STORAGES = 0;
   private static final int CANDIDATES = 1;
   private static final int FIRED = 2;

   @Override
   public MapCodec<RunStoredPowerAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("storage", IdCodecs.ID).forGetter(RunStoredPowerAction.Cfg::storage),
               LoggedOptionalField.strict("key", Key.CODEC).forGetter(RunStoredPowerAction.Cfg::key),
               LoggedOptionalField.strict("power", IdCodecs.ID).forGetter(RunStoredPowerAction.Cfg::power),
               LoggedOptionalField.of("tags", SingleOrList.of(Codec.STRING), List.of()).forGetter(RunStoredPowerAction.Cfg::tags),
               Codec.INT.optionalFieldOf("index", -1).forGetter(RunStoredPowerAction.Cfg::index),
               Codec.BOOL.optionalFieldOf("all", false).forGetter(RunStoredPowerAction.Cfg::all)
            )
            .apply(i, RunStoredPowerAction.Cfg::new)
      );
   }

   public void run(RunStoredPowerAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      boolean dev = DevMode.any();
      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty() && !container.powersOfType(ApoliIds.POWER_STORAGE).isEmpty()) {
         String key = (String)cfg.key().map(Key::key).orElse(null);
         boolean[] done = new boolean[1];
         int[] stats = dev ? new int[3] : null;
         PowerLookup.forEachEntry(entity, ApoliIds.POWER_STORAGE, Config.class, (storageId, storage) -> {
            if (!done[0] || cfg.all()) {
               if (!cfg.storage().isPresent() || ((class_2960)cfg.storage().get()).equals(storageId)) {
                  if (stats != null) {
                     stats[0]++;
                  }

                  List<class_2960> powers = PowerStoragePower.live(container, storageId);
                  if (!powers.isEmpty()) {
                     if (cfg.index() >= 0) {
                        if (cfg.index() < powers.size()) {
                           fire(entity, (class_2960)powers.get(cfg.index()), cfg, key, done, stats);
                        }
                     } else {
                        for (int i = 0; i < powers.size(); i++) {
                           if (done[0] && !cfg.all()) {
                              return;
                           }

                           fire(entity, (class_2960)powers.get(i), cfg, key, done, stats);
                        }
                     }
                  }
               }
            }
         });
         if (stats != null && stats[2] == 0) {
            report(entity, cfg, stats);
         }
      } else {
         if (dev) {
            DevMode.report(entity, "run_stored_power: nothing here holds an apoli:power_storage power");
         }
      }
   }

   private static void fire(class_1297 entity, class_2960 powerId, RunStoredPowerAction.Cfg cfg, String key, boolean[] done, int[] stats) {
      if (!cfg.power().isPresent() || ((class_2960)cfg.power().get()).equals(powerId)) {
         if (cfg.tags().isEmpty() || ApoliPowers.hasAnyTag(powerId, cfg.tags())) {
            if (stats != null) {
               stats[1]++;
            }

            if (KeyDispatch.pressPower(entity, powerId, key)) {
               if (stats != null) {
                  stats[2]++;
               }

               done[0] = true;
            }
         }
      }
   }

   private static void report(class_1297 entity, RunStoredPowerAction.Cfg cfg, int[] stats) {
      if (stats[0] == 0) {
         DevMode.report(
            entity,
            "run_stored_power: no storage power here is called "
               + (String)cfg.storage().map(class_2960::toString).orElse("?")
               + " — a sub-power of an apoli:multiple is <power id>_<key>"
         );
      } else if (stats[1] == 0) {
         DevMode.report(
            entity,
            "run_stored_power: the storage holds nothing that matches"
               + (cfg.power().isPresent() ? " power " + cfg.power().get() : "")
               + (cfg.tags().isEmpty() ? "" : " tags " + cfg.tags())
               + (cfg.index() >= 0 ? " index " + cfg.index() : "")
         );
      } else {
         DevMode.report(
            entity,
            "run_stored_power: "
               + stats[1]
               + " stored power(s) matched but none fired — a stored power only answers this if it is key-driven (action_on_key_press, toggle, fire_projectile, inventory or a multiple of those), and its own condition, cooldown and prevent_key_press still apply"
         );
      }
   }

   public record Cfg(Optional<class_2960> storage, Optional<Key> key, Optional<class_2960> power, List<String> tags, int index, boolean all) {
   }
}
