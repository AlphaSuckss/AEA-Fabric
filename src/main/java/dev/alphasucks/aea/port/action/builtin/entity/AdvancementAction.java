package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_161;
import net.minecraft.class_2960;
import net.minecraft.class_2985;
import net.minecraft.class_2989;
import net.minecraft.class_3222;
import net.minecraft.class_3542;

public final class AdvancementAction implements ActionType<EntityCtx, AdvancementAction.Cfg> {
   private final boolean defaultRevoke;

   public AdvancementAction(boolean defaultRevoke) {
      this.defaultRevoke = defaultRevoke;
   }

   public AdvancementAction() {
      this(false);
   }

   @Override
   public MapCodec<AdvancementAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("advancement").forGetter(AdvancementAction.Cfg::advancement),
               Codec.list(Codec.STRING).optionalFieldOf("criteria").forGetter(AdvancementAction.Cfg::criteria),
               Codec.STRING.optionalFieldOf("criterion").forGetter(AdvancementAction.Cfg::criterion),
               AdvancementAction.Selection.CODEC.optionalFieldOf("selection", AdvancementAction.Selection.ONLY).forGetter(AdvancementAction.Cfg::selection),
               Codec.BOOL.optionalFieldOf("revoke", this.defaultRevoke).forGetter(AdvancementAction.Cfg::revoke)
            )
            .apply(i, AdvancementAction.Cfg::new)
      );
   }

   public void run(AdvancementAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         if (!cfg.advancement.isEmpty()) {
            class_2989 mgr = player.field_13995.method_3851();
            class_161 adv = mgr.method_12896((class_2960)cfg.advancement.get());
            if (adv != null) {
               class_2985 progress = player.method_14236();

               for (String c : resolveCriteria(cfg, adv)) {
                  if (cfg.revoke) {
                     progress.method_12883(adv, c);
                  } else {
                     progress.method_12878(adv, c);
                  }
               }
            }
         }
      }
   }

   private static Set<String> resolveCriteria(AdvancementAction.Cfg cfg, class_161 adv) {
      if (cfg.criteria.isPresent()) {
         return Set.copyOf((Collection)cfg.criteria.get());
      } else {
         return cfg.criterion.isPresent() ? Set.of((String)cfg.criterion.get()) : adv.method_682().keySet();
      }
   }

   public record Cfg(
      Optional<class_2960> advancement, Optional<List<String>> criteria, Optional<String> criterion, AdvancementAction.Selection selection, boolean revoke
   ) {
   }

   public enum Selection implements class_3542 {
      ONLY("only"),
      THROUGH("through"),
      FROM("from"),
      UNTIL("until"),
      EVERYTHING("everything");

      public static final Codec<AdvancementAction.Selection> CODEC = class_3542.method_28140(AdvancementAction.Selection::values);
      private final String name;

      Selection(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
