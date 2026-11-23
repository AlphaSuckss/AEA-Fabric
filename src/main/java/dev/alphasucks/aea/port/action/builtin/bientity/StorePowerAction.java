package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower.Config;
import dev.alphasucks.aea.port.power.builtin.PowerStoragePower.StoreResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

public final class StorePowerAction {
   private StorePowerAction() {
   }

   public static MapCodec<StorePowerAction.Cfg> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("storage", IdCodecs.ID).forGetter(StorePowerAction.Cfg::storage),
               LoggedOptionalField.strict("power", SingleOrList.of(IdCodecs.ID)).forGetter(StorePowerAction.Cfg::powers),
               LoggedOptionalField.of("tags", SingleOrList.of(Codec.STRING), List.of()).forGetter(StorePowerAction.Cfg::tags),
               Codec.BOOL.optionalFieldOf("from_held", false).forGetter(StorePowerAction.Cfg::fromHeld),
               StorePowerAction.Operation.CODEC.optionalFieldOf("operation", StorePowerAction.Operation.ADD).forGetter(StorePowerAction.Cfg::operation)
            )
            .apply(i, StorePowerAction.Cfg::new)
      );
   }

   public static void apply(StorePowerAction.Cfg cfg, @Nullable class_1297 source, @Nullable class_1297 holder) {
      if (holder != null) {
         boolean dev = DevMode.any();
         PowerContainer container = PowerContainer.of(holder);
         if (container != null && !container.powersOfType(ApoliIds.POWER_STORAGE).isEmpty()) {
            List<class_2960> wanted = cfg.operation() == StorePowerAction.Operation.CLEAR ? List.of() : collect(cfg, source);
            if (wanted.isEmpty() && cfg.operation() != StorePowerAction.Operation.CLEAR) {
               if (dev) {
                  reportNothingCollected(cfg, source, holder);
               }
            } else {
               boolean[] matched = new boolean[1];
               PowerLookup.forEachEntry(holder, ApoliIds.POWER_STORAGE, Config.class, (storageId, storage) -> {
                  if (!cfg.storage().isPresent() || ((class_2960)cfg.storage().get()).equals(storageId)) {
                     matched[0] = true;
                     switch (cfg.operation()) {
                        case ADD:
                           for (int i = 0; i < wanted.size(); i++) {
                              class_2960 powerId = (class_2960)wanted.get(i);
                              StoreResult result = PowerStoragePower.attempt(container, storageId, storage, powerId);
                              if (dev) {
                                 DevMode.report(holder, "store_power: " + storageId + " " + powerId + " " + describe(result, storage) + origin(cfg, source));
                              }
                           }
                           break;
                        case REMOVE:
                           for (int i = 0; i < wanted.size(); i++) {
                              boolean removed = PowerStoragePower.remove(container, storageId, (class_2960)wanted.get(i));
                              if (dev) {
                                 DevMode.report(holder, "store_power: " + storageId + " " + wanted.get(i) + (removed ? " removed" : " was not stored"));
                              }
                           }
                           break;
                        case CLEAR:
                           PowerStoragePower.clear(container, storageId);
                           if (dev) {
                              DevMode.report(holder, "store_power: cleared " + storageId);
                           }
                     }
                  }
               });
               if (dev && !matched[0]) {
                  DevMode.report(
                     holder,
                     "store_power: no storage power here is called "
                        + (String)cfg.storage().map(class_2960::toString).orElse("?")
                        + " — a sub-power of an apoli:multiple is <power id>_<key>"
                  );
               }
            }
         } else {
            if (dev) {
               DevMode.report(holder, "store_power: nothing here holds an apoli:power_storage power");
            }
         }
      }
   }

   private static String origin(StorePowerAction.Cfg cfg, @Nullable class_1297 source) {
      return !cfg.fromHeld() ? "" : " (read from " + (source == null ? "nothing" : source.method_5477().getString()) + ")";
   }

   private static String describe(StoreResult result, Config storage) {
      return switch (result) {
         case STORED -> "stored";
         case NO_CONTAINER -> "refused: the holder has no power container";
         case UNKNOWN_POWER -> "refused: no power is loaded with that id";
         case IS_STORAGE -> "refused: a power storage cannot be stored inside a power storage";
         case NOT_IN_POWERS -> "refused: it is not listed in this storage's 'powers'";
         case NO_MATCHING_TAG -> "refused: it carries none of this storage's tags " + storage.tags();
         case ALREADY_STORED -> "refused: it is already in this storage";
         case FULL -> "refused: all " + storage.slots() + " slot(s) are full and replace_oldest is off";
      };
   }

   private static void reportNothingCollected(StorePowerAction.Cfg cfg, @Nullable class_1297 source, class_1297 holder) {
      if (!cfg.fromHeld()) {
         DevMode.report(holder, "store_power: neither 'power' nor 'tags' named anything to store");
      } else if (source == null) {
         DevMode.report(holder, "store_power: from_held is set but there is no actor to take powers from");
      } else {
         PowerContainer from = PowerContainer.of(source);
         if (from != null && !from.isEmpty()) {
            DevMode.report(
               source,
               "store_power: from_held found no power on this entity matching"
                  + (cfg.tags().isEmpty() ? "" : " tags " + cfg.tags())
                  + (cfg.powers().isPresent() ? " power " + cfg.powers().get() : "")
                  + " — from_held only takes powers this entity actually holds"
            );
         } else {
            DevMode.report(source, "store_power: from_held found no powers at all on this entity");
         }
      }
   }

   private static List<class_2960> collect(StorePowerAction.Cfg cfg, @Nullable class_1297 source) {
      if (!cfg.fromHeld()) {
         List<class_2960> explicit = (List<class_2960>)cfg.powers().orElse(List.of());
         if (cfg.tags().isEmpty()) {
            return explicit;
         }

         List<class_2960> out = new ArrayList(explicit);

         for (String tag : cfg.tags()) {
            out.addAll(ApoliPowers.withTag(tag));
         }

         return out;
      } else {
         PowerContainer from = source == null ? null : PowerContainer.of(source);
         if (from == null) {
            return List.of();
         }

         List<class_2960> out = new ArrayList();

         for (class_2960 id : from.allPowers()) {
            if ((!cfg.powers().isPresent() || ((List)cfg.powers().get()).contains(id)) && (cfg.tags().isEmpty() || ApoliPowers.hasAnyTag(id, cfg.tags()))) {
               out.add(id);
            }
         }

         return out;
      }
   }

   public static final class BiEntity implements ActionType<BiEntityCtx, StorePowerAction.Cfg> {
      @Override
      public MapCodec<StorePowerAction.Cfg> codec() {
         return StorePowerAction.configCodec();
      }

      public void run(StorePowerAction.Cfg cfg, BiEntityCtx ctx) {
         StorePowerAction.apply(cfg, ctx.rawActor(), ctx.rawTarget());
      }
   }

   public record Cfg(Optional<class_2960> storage, Optional<List<class_2960>> powers, List<String> tags, boolean fromHeld, StorePowerAction.Operation operation) {
   }

   public enum Operation implements class_3542 {
      ADD("add"),
      REMOVE("remove"),
      CLEAR("clear");

      public static final Codec<StorePowerAction.Operation> CODEC = class_3542.method_28140(StorePowerAction.Operation::values);
      private final String name;

      Operation(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }

   public static final class Self implements ActionType<EntityCtx, StorePowerAction.Cfg> {
      @Override
      public MapCodec<StorePowerAction.Cfg> codec() {
         return StorePowerAction.configCodec();
      }

      public void run(StorePowerAction.Cfg cfg, EntityCtx ctx) {
         StorePowerAction.apply(cfg, ctx.raw(), ctx.raw());
      }
   }
}
