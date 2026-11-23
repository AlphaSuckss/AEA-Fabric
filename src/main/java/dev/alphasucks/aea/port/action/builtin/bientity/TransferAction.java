package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3542;

public final class TransferAction implements ActionType<BiEntityCtx, TransferAction.Cfg> {
   public static final class_2960 DEFAULT_SOURCE = Apoli.id("transferred");

   @Override
   public MapCodec<TransferAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TransferAction.Mode.CODEC.optionalFieldOf("mode", TransferAction.Mode.STEAL).forGetter(TransferAction.Cfg::mode),
               Codec.BOOL.optionalFieldOf("copy", false).forGetter(TransferAction.Cfg::copy),
               IdCodecs.ID.listOf().optionalFieldOf("sources").forGetter(TransferAction.Cfg::sources),
               SingleOrList.of(Codec.STRING).optionalFieldOf("tags").forGetter(TransferAction.Cfg::tags),
               SingleOrList.of(IdCodecs.ID).optionalFieldOf("powers").forGetter(TransferAction.Cfg::powers),
               IdCodecs.ID.optionalFieldOf("new_source", DEFAULT_SOURCE).forGetter(TransferAction.Cfg::newSource),
               Codec.BOOL.optionalFieldOf("preserve_source", false).forGetter(TransferAction.Cfg::preserveSource),
               LoggedOptionalField.of("actor_action", EntityAction.CODEC).forGetter(TransferAction.Cfg::actorAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(TransferAction.Cfg::targetAction)
            )
            .apply(i, TransferAction.Cfg::new)
      );
   }

   public void run(TransferAction.Cfg cfg, BiEntityCtx ctx) {
      class_1297 donor = cfg.mode == TransferAction.Mode.STEAL ? ctx.target() : ctx.actor();
      class_1297 recipient = cfg.mode == TransferAction.Mode.STEAL ? ctx.actor() : ctx.target();
      if (donor != recipient && donor != null && recipient != null) {
         PowerContainer donorContainer = PowerContainer.of(donor);
         PowerContainer recipientContainer = PowerContainerAttachment.getOrCreate(recipient);
         if (donorContainer != null && recipientContainer != null) {
            Map<class_2960, Set<class_2960>> toTransfer = new LinkedHashMap();

            for (class_2960 power : donorContainer.allPowers()) {
               if (!ApoliPowers.isSubPower(power)
                  && (!cfg.powers.isPresent() || ((List)cfg.powers.get()).contains(power))
                  && (!cfg.tags.isPresent() || ApoliPowers.hasAnyTag(power, (List<String>)cfg.tags.get()))) {
                  for (class_2960 source : donorContainer.sourcesOf(power)) {
                     if (!source.equals(cfg.newSource) && (!cfg.sources.isPresent() || ((List)cfg.sources.get()).contains(source))) {
                        ((Set)toTransfer.computeIfAbsent(power, k -> new LinkedHashSet())).add(source);
                     }
                  }
               }
            }

            if (toTransfer.isEmpty()) {
               runFollowUps(cfg, ctx);
            } else {
               for (Entry<class_2960, Set<class_2960>> entry : toTransfer.entrySet()) {
                  class_2960 power = (class_2960)entry.getKey();

                  for (class_2960 source : (Set<class_2960>)entry.getValue()) {
                     recipientContainer.addPower(power, cfg.preserveSource ? source : cfg.newSource);
                     if (!cfg.copy) {
                        donorContainer.removePower(power, source);
                     }
                  }
               }

               runFollowUps(cfg, ctx);
            }
         }
      }
   }

   private static void runFollowUps(TransferAction.Cfg cfg, BiEntityCtx ctx) {
      cfg.actorAction.ifPresent(a -> a.run(ctx.asActor()));
      cfg.targetAction.ifPresent(a -> a.run(ctx.asTarget()));
   }

   public record Cfg(
      TransferAction.Mode mode,
      boolean copy,
      Optional<List<class_2960>> sources,
      Optional<List<String>> tags,
      Optional<List<class_2960>> powers,
      class_2960 newSource,
      boolean preserveSource,
      Optional<EntityAction> actorAction,
      Optional<EntityAction> targetAction
   ) {
   }

   public enum Mode implements class_3542 {
      STEAL("steal"),
      GIVE("give");

      public static final Codec<TransferAction.Mode> CODEC = class_3542.method_28140(TransferAction.Mode::values);
      private final String name;

      Mode(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
